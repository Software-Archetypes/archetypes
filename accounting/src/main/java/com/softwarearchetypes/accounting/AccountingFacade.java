package com.softwarearchetypes.accounting;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.softwarearchetypes.accounting.events.AccountingEvent;
import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.common.Result.CompositeSetResult;
import com.softwarearchetypes.common.Version;
import com.softwarearchetypes.common.events.EventPublisher;
import com.softwarearchetypes.quantity.money.Money;

import static com.softwarearchetypes.accounting.TransactionType.INITIALIZATION;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

//todo wszedzie
//db transacions
public class AccountingFacade {

    private final Clock clock;
    private final AccountRepository accountRepository;
    private final AccountViewQueries accountViewQueries;
    private final TransactionRepository transactionRepository;
    private final TransactionBuilderFactory transactionBuilderFactory;
    private final EventPublisher eventPublisher;

    AccountingFacade(Clock clock, AccountRepository accountRepository, AccountViewQueries accountViewQueries, TransactionRepository transactionRepository, TransactionBuilderFactory transactionBuilderFactory, EventPublisher eventPublisher) {
        this.clock = clock;
        this.accountRepository = accountRepository;
        this.accountViewQueries = accountViewQueries;
        this.transactionRepository = transactionRepository;
        this.transactionBuilderFactory = transactionBuilderFactory;
        this.eventPublisher = eventPublisher;
    }

    public Result<String, Set<AccountId>> createAccounts(Set<CreateAccount> requests) {
        Set<AccountId> ids = requests.stream().map(CreateAccount::accountId).collect(toSet());
        if (!accountRepository.find(ids).isEmpty()) {
            return Result.failure(format("Some accounts already exists: %s", ids));
        }
        Set<AccountId> createdAccounts = new HashSet<>();
        requests.forEach(req -> {
            createAccount(req.accountId(), AccountType.valueOf(req.type()), AccountName.of(req.name()));
        });
        return Result.success(createdAccounts);
    }

    public Result<String, AccountId> createAccount(CreateAccount request) {
        return createAccount(request.accountId(), AccountType.valueOf(request.type()), AccountName.of(request.name()));
    }

    private Result<String, AccountId> createAccount(AccountId accountId, AccountType type, AccountName name) {
        if (accountRepository.find(accountId).isPresent()) {
            return Result.failure("Account with id " + accountId + " already exists");
        }
        Account account = new Account(accountId, type, name, Version.initial());
        accountRepository.save(account);
        return Result.success(account.id());
    }

    public Optional<Money> balance(AccountId accountId) {
        return accountRepository.find(accountId).map(Account::balance);
    }

    public Optional<Money> balanceAsOf(AccountId accountId, Instant when) {
        return accountViewQueries.find(accountId).map(acc -> acc.balanceAsOf(when));
    }

    public Balances balancesAsOf(Set<AccountId> accounts, Instant when) {
        Map<AccountId, AccountView> entriesByAccount = accountViewQueries.find(accounts);
        Map<AccountId, Money> balances = new HashMap<>();
        for (Map.Entry<AccountId, AccountView> entry : entriesByAccount.entrySet()) {
            balances.put(entry.getKey(), entry.getValue().balanceAsOf(when));
        }
        return new Balances(balances);
    }

    public Balances balances(Set<AccountId> accounts) {
        return balancesAsOf(accounts, clock.instant());
    }

    public Result<String, Set<AccountId>> createAccountsWithInitialBalances(Set<CreateAccount> requests, AccountAmounts accountAmounts) {
        Result<String, Set<AccountId>> creation = createAccounts(requests);
        Result<String, TransactionId> txResult = creation.flatMap(it -> {
            Transaction transaction = transactionBuilderFactory.transaction()
                                                               .withTypeOf(INITIALIZATION)
                                                               .occurredAt(clock.instant())
                                                               .appliesAt(clock.instant())
                                                               .executing()
                                                               .entriesFor(accountAmounts)
                                                               .build();
            return execute(transaction);
        });
        if (txResult.success()) {
            return creation;
        } else {
            return Result.failure(txResult.getFailure());
        }
    }

    public TransactionBuilder transaction() {
        return transactionBuilderFactory.transaction();
    }

    public Result<String, TransactionId> handle(ExecuteTransactionCommand command) {
        try {
            TransactionBuilder.TransactionEntriesBuilder entriesBuilder = transactionBuilderFactory.transaction()
                    .occurredAt(command.occurredAt())
                    .appliesAt(command.appliesAt())
                    .withTypeOf(command.transactionType())
                    .withMetadata(MetaData.of(command.metadata()))
                    .executing();

            for (ExecuteTransactionCommand.Entry entry : command.entries()) {
                Validity validity = Validity.between(entry.validFrom(), entry.validTo());
                AccountId accountId = AccountId.of(entry.accountId());
                Money amount = entry.amount();

                switch (entry.entryType()) {
                    case CREDIT -> entriesBuilder.creditTo(accountId, amount, validity);
                    case DEBIT -> entriesBuilder.debitFrom(accountId, amount, validity);
                }
            }

            Transaction transaction = entriesBuilder.build();
            return execute(transaction);
        } catch (Exception ex) {
            return Result.failure(ex.getMessage());
        }
    }

    public Result<String, TransactionId> handle(ReverseTransactionCommand command) {
        try {
            Transaction transaction = transactionBuilderFactory.transaction()
                    .occurredAt(command.occurredAt())
                    .appliesAt(command.appliesAt())
                    .reverting(TransactionId.of(command.refTransactionId()))
                    .build();
            return execute(transaction);
        } catch (Exception ex) {
            return Result.failure(ex.getMessage());
        }
    }

    public Result<String, TransactionId> transfer(AccountId from, AccountId to, Money amount, Instant occurredAt, Instant appliesAt) {
        return transfer(from, to, amount, occurredAt, appliesAt, MetaData.empty());
    }

    //transactional
    public Result<String, TransactionId> transfer(AccountId from, AccountId to, Money amount, Instant occurredAt, Instant appliesAt, MetaData metaData) {
        try {
            Transaction transaction = transactionBuilderFactory.transaction()
                                                               .occurredAt(occurredAt)
                                                               .appliesAt(appliesAt)
                                                               .withTypeOf("transfer")
                                                               .withMetadata(metaData)
                                                               .executing()
                                                               .debitFrom(from, amount)
                                                               .creditTo(to, amount)
                                                               .build();
            transaction.execute();
            transactionRepository.save(transaction);
            //optimistic locking on every account involved in transaction
            saveAccountsAndPublishEvents(transaction.accountsInvolved());
            return Result.success(transaction.id());
        } catch (Exception ex) {
            return Result.failure(ex.getMessage());
        }
    }

    //db transaction
    public Result<String, Set<TransactionId>> execute(Transaction... transactions) {
        CompositeSetResult<String, TransactionId> result = Result.compositeSet();
        for (Transaction transaction : transactions) {
            result.accumulate(execute(transaction));
            if (result.failure()) {
                return result.toResult();
            }
        }
        return result.toResult();
    }

    //db transaction
    public Result<String, TransactionId> execute(Transaction transaction) {
        try {
            transaction.execute();
            transactionRepository.save(transaction);
            //optimistic locking on every account involved in transaction
            saveAccountsAndPublishEvents(transaction.accountsInvolved());
        } catch (Exception ex) {
            return Result.failure(ex.getMessage());
        }
        return Result.success(transaction.id());
    }

    public Result<String, AccountId> createProjectingAccount(AccountId projecting, AccountEntryFilter accountEntryFilter, String description) {
        return createProjectingAccount(projecting, accountEntryFilter.toFilter(), description);
    }

    Result<String, AccountId> createProjectingAccount(AccountId projecting, Filter filter, String name) {
        accountRepository.save(new ProjectionAccount(projecting, filter, name));
        return Result.success(projecting);
    }

    public Optional<AccountView> findAccount(AccountId accountId) {
        return accountRepository.find(accountId).map(AccountView::from);
    }

    public List<AccountView> findAccounts(Set<AccountId> accountIds) {
        return accountRepository.find(accountIds)
                                .values()
                                .stream()
                                .map(AccountView::from)
                                .toList();
    }

    public List<AccountView> findAll() {
        return accountRepository
                .findAll()
                .stream()
                .map(AccountView::from)
                .toList();
    }

    public Optional<TransactionView> findTransactionBy(TransactionId transactionId) {
        //transactions should not contain entries in db - entries are assigned to accounts
        //therefore, to get transaction entries a SQL query is required to find entries with
        //matching transactionId
        return transactionRepository.find(transactionId)
                                    .map(transaction -> new TransactionView(
                                            transaction.id(),
                                            transaction.refId().orElse(null),
                                            transaction.type(),
                                            transaction.occurredAt(),
                                            transaction.appliesAt(),
                                            entriesViewsFrom(transaction)));
    }

    public List<TransactionId> findTransactionIdsFor(AccountId accountId) {
        //transactions should not contain entries in db - entries are assigned to accounts
        //therefore, to get transaction entries a SQL query is required to find entries with
        //matching transactionId
        return accountRepository.find(accountId)
                                .stream()
                                .flatMap(acc -> acc.entries().stream())
                                .map(Entry::transactionId)
                                .collect(toList());
    }

    @NotNull
    private static List<TransactionAccountEntriesView> entriesViewsFrom(Transaction transaction) {
        return transaction.entries().entrySet().stream().map(
                entry -> {
                    Account account = entry.getKey();
                    AccountMetadataView accountView = new AccountMetadataView(account.id(), account.name(), account.type().name());
                    List<EntryView> entries = entry.getValue().stream().map(EntryView::from).toList();
                    return new TransactionAccountEntriesView(accountView, entries);
                }
        ).collect(toList());
    }

    private void saveAccountsAndPublishEvents(Collection<Account> accounts) {
        List<AccountingEvent> allEvents = new ArrayList<>();

        for (Account account : accounts) {
            allEvents.addAll(account.getPendingEvents());
            account.clearPendingEvents();
        }

        accountRepository.save(accounts);
        eventPublisher.publish(allEvents);
    }
}

class AccountViewQueries {

    private final AccountRepository accountRepository;
    private final EntryRepository entryRepository;

    AccountViewQueries(AccountRepository accountRepository, EntryRepository entryRepository) {
        this.accountRepository = accountRepository;
        this.entryRepository = entryRepository;
    }

    //can be changed with SQL
    Optional<AccountView> find(AccountId accountId) {
        return accountRepository.find(accountId)
                                .map(this::accountViewFrom);
    }

    //can be changed with SQL
    Map<AccountId, AccountView> find(Set<AccountId> accountIds) {
        return accountRepository.find(accountIds)
                                .values()
                                .stream()
                                .map(this::accountViewFrom)
                                .collect(toMap(AccountView::id, it -> it));
    }

    private AccountView accountViewFrom(Account acc) {
        List<EntryView> entries = entryRepository.findAllFor(acc.id()).stream().map(EntryView::from).collect(toList());
        return new AccountView(acc.id(), acc.name(), acc.type().name(), acc.balance(), entries);
    }

}

interface AccountRepository {

    Optional<Account> find(AccountId accountId);

    Account save(Account account);

    void save(Collection<Account> accounts);

    ProjectionAccount save(ProjectionAccount account);

    List<Account> findAll();

    Map<AccountId, Account> find(Set<AccountId> accounts);
}

class InMemoryAccountRepo implements AccountRepository {

    private final Map<AccountId, Account> accounts = new HashMap<>();
    private final Map<AccountId, ProjectionAccount> projectionAccounts = new HashMap<>();
    private final EntryRepository entryRepository;

    InMemoryAccountRepo(EntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    @Override
    public Optional<Account> find(AccountId accountId) {
        return Optional.ofNullable(getAccount(accountId));
    }

    @Override
    public Account save(Account account) {
        account.entries().stream().forEach(entryRepository::save);
        return accounts.put(account.id(), account);
    }

    @Override
    public void save(Collection<Account> accounts) {
        accounts.forEach(acc -> {
            acc.entries().stream().forEach(entryRepository::save);
            this.accounts.put(acc.id(), acc);
        });
    }

    @Override
    public ProjectionAccount save(ProjectionAccount projection) {
        return projectionAccounts.put(projection.id(), projection);
    }

    @Override
    public Map<AccountId, Account> find(Set<AccountId> accounts) {
        return accounts.stream()
                       .filter(key -> this.accounts.containsKey(key) || this.projectionAccounts.containsKey(key))
                       .collect(toMap(
                               accountId -> accountId,
                               this::getAccount)
                       );
    }

    @Override
    public List<Account> findAll() {
        List<Account> allAccounts = new ArrayList<>(accounts.values());
        // Add projection accounts
        for (AccountId projectionId : projectionAccounts.keySet()) {
            allAccounts.add(getProjection(projectionId));
        }
        return allAccounts;
    }

    private Account getAccount(AccountId accountId) {
        if (!accounts.containsKey(accountId)) {
            if (!projectionAccounts.containsKey(accountId)) {
                return null;
            }
            return getProjection(accountId);
        }
        return accounts.get(accountId);
    }

    //TODO: przerobić na accountView
    private Account getProjection(AccountId accountId) {
        ProjectionAccount projectionAccount = projectionAccounts.get(accountId);
        //sql z bazy - nie wyciagamy wszystkich kont do pamieci
        Set<Account> filteredAccounts = accounts.values().stream().filter(account -> projectionAccount.filter().accountFilter().test(account)).collect(toSet());
        Entries filteredEntries =
                new Entries(filteredAccounts.stream().map(Account::entries)
                                            .map(Entries::toList)
                                            .flatMap(Collection::stream)
                                            .filter(entry -> projectionAccount.filter().entryFilter().test(entry))
                                            .toList());
        return new Account(accountId, null, null, projectionAccount.version());
    }

}
