package com.softwarearchetypes.accounting;

import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import com.softwarearchetypes.quantity.money.Money;

import static com.softwarearchetypes.accounting.TransactionEntriesConstraint.BALANCING_CONSTRAINT;
import static com.softwarearchetypes.accounting.TransactionType.EXPIRATION_COMPENSATION;
import static com.softwarearchetypes.accounting.TransactionType.REVERSAL;
import static com.softwarearchetypes.common.Preconditions.checkArgument;
import static io.pillopl.common.CollectionTransformations.keyValueMapFrom;
import static io.pillopl.common.CollectionTransformations.subtract;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

class TransactionBuilder {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final EntryAllocations entryAllocations;
    private final EntryRepository entryRepository;
    private final Clock clock;
    private TransactionId transactionId = TransactionId.generate();
    private Instant occurredAt;
    private Instant appliesAt;
    private TransactionType type;
    private MetaData metadata = MetaData.empty();
    private TransactionEntriesConstraint transactionEntriesConstraint = BALANCING_CONSTRAINT;

    TransactionBuilder(AccountRepository accountRepository, TransactionRepository transactionRepository, EntryAllocations entryAllocations, EntryRepository entryRepository, Clock clock) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.entryAllocations = entryAllocations;
        this.entryRepository = entryRepository;
        this.clock = clock;
    }

    public TransactionBuilder occurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
        return this;
    }

    public TransactionBuilder appliesAt(Instant appliesAt) {
        this.appliesAt = appliesAt;
        return this;
    }

    public TransactionBuilder id(TransactionId transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public TransactionBuilder withMetadata(String... metadata) {
        this.metadata = new MetaData(keyValueMapFrom(metadata));
        return this;
    }

    public TransactionBuilder withMetadata(MetaData metadata) {
        this.metadata = metadata;
        return this;
    }

    public TransactionBuilder withTypeOf(String type) {
        return withTypeOf(TransactionType.of(type));
    }

    public TransactionBuilder withTypeOf(TransactionType type) {
        this.type = type;
        return this;
    }

    public TransactionBuilder withTransactionEntriesConstraint(TransactionEntriesConstraint constraint) {
        this.transactionEntriesConstraint = constraint;
        return this;
    }

    public TransactionEntriesBuilder executing() {
        return new TransactionEntriesBuilder();
    }

    public ReverseTransactionEntriesBuilder reverting(Transaction refTransaction) {
        return new ReverseTransactionEntriesBuilder(refTransaction);
    }

    public ReverseTransactionEntriesBuilder reverting(TransactionId refTransactionId) {
        Transaction refTransaction = transactionRepository.find(refTransactionId)
                                                          .orElseThrow(() -> new IllegalArgumentException(String.format("Transaction %s does not exist", refTransactionId.toString())));
        return new ReverseTransactionEntriesBuilder(refTransaction);
    }

    ExpirationCompensationTransactionEntriesBuilder compensatingExpired(EntryId entryId) {
        Entry entry = entryRepository.find(entryId).orElseThrow(() -> new IllegalArgumentException(String.format("Entry %s does not exist", entryId)));
        return compensatingExpired(entry);
    }

    ExpirationCompensationTransactionEntriesBuilder compensatingExpired(Entry entry) {
        return new ExpirationCompensationTransactionEntriesBuilder(entry);
    }

    public class TransactionEntriesBuilder {

        private final List<Entry> entries = new LinkedList<>();
        private final Set<AccountId> involvedAccountsIds = new HashSet<>();

        TransactionEntriesBuilder transfer(AccountId from, AccountId to, Money amount) {
            return debitFrom(from, amount).creditTo(to, amount);
        }

        public TransactionEntriesBuilder creditTo(AccountId accountId, Money amount) {
            AccountCredited entry = new AccountCredited(accountId, transactionId, amount, appliesAt, occurredAt, metadata);
            this.entries.add(entry);
            this.involvedAccountsIds.add(accountId);
            return this;
        }

        public TransactionEntriesBuilder creditTo(AccountId accountId, Money amount, Validity validity) {
            return creditTo(accountId, amount, validity, EntryAllocationFilter.NONE);
        }

        public TransactionEntriesBuilder creditTo(AccountId accountId, Money amount, EntryId appliedTo) {
            return creditTo(accountId, amount, EntryAllocationFilterBuilder.manual(appliedTo).build());
        }

        public TransactionEntriesBuilder creditTo(AccountId accountId, Money amount, Validity validity, EntryId appliedTo) {
            return creditTo(accountId, amount, validity, EntryAllocationFilterBuilder.manual(appliedTo).build());
        }

        public TransactionEntriesBuilder creditTo(AccountId accountId, Money amount, EntryAllocationFilter filter) {
            return creditTo(accountId, amount, Validity.always(), filter);
        }

        public TransactionEntriesBuilder creditTo(AccountId accountId, Money amount, Validity validity, EntryAllocationFilter filter) {
            EntryId refEntryId = null;
            if (!filter.isEmpty()) {
                refEntryId = findEntryAllocation(filter);
            }

            AccountCredited entry = new AccountCredited(accountId, transactionId, amount, appliesAt, occurredAt, metadata, validity, refEntryId);
            this.entries.add(entry);
            this.involvedAccountsIds.add(accountId);
            return this;
        }

        public TransactionEntriesBuilder creditTo(Map<AccountId, Money> creditsToExecute) {
            creditsToExecute.forEach((accountId, amount) -> {
                AccountCredited entry = new AccountCredited(accountId, transactionId, amount, appliesAt, occurredAt, metadata);
                this.entries.add(entry);
                this.involvedAccountsIds.add(accountId);
            });
            return this;
        }

        public TransactionEntriesBuilder debitFrom(AccountId accountId, Money amount) {
            return debitFrom(accountId, amount, Validity.always(), EntryAllocationFilter.NONE);
        }

        public TransactionEntriesBuilder debitFrom(AccountId accountId, Money amount, Validity validity) {
            return debitFrom(accountId, amount, validity, EntryAllocationFilter.NONE);
        }

        public TransactionEntriesBuilder debitFrom(AccountId accountId, Money amount, EntryId appliedTo) {
            return debitFrom(accountId, amount, EntryAllocationFilterBuilder.manual(appliedTo).build());
        }

        public TransactionEntriesBuilder debitFrom(AccountId accountId, Money amount, Validity validity, EntryId appliedTo) {
            return debitFrom(accountId, amount, validity, EntryAllocationFilterBuilder.manual(appliedTo).build());
        }

        public TransactionEntriesBuilder debitFrom(AccountId accountId, Money amount, EntryAllocationFilter filter) {
            return debitFrom(accountId, amount, Validity.always(), filter);
        }

        public TransactionEntriesBuilder debitFrom(AccountId accountId, Money amount, Validity validity, EntryAllocationFilter filter) {
            EntryId refEntryId = null;
            if (!filter.isEmpty()) {
                refEntryId = findEntryAllocation(filter);
            }

            AccountDebited entry = new AccountDebited(accountId, transactionId, amount, appliesAt, occurredAt, metadata, validity, refEntryId);
            this.entries.add(entry);
            this.involvedAccountsIds.add(accountId);
            return this;
        }

        public TransactionEntriesBuilder debitFrom(Supplier<Map<AccountId, Money>> debitSupplier) {
            return debitFrom(debitSupplier.get());
        }

        public TransactionEntriesBuilder debitFrom(Map<AccountId, Money> debitSupplier) {
            debitSupplier.forEach((accountId, amount) -> {
                AccountDebited entry = new AccountDebited(accountId, transactionId, amount, appliesAt, occurredAt, metadata);
                this.entries.add(entry);
                this.involvedAccountsIds.add(accountId);
            });
            return this;
        }

        TransactionEntriesBuilder entriesFor(AccountAmounts accountAmounts) {
            accountAmounts.all().forEach((accountId, amount) -> {
                if (amount.isNegative()) {
                    this.debitFrom(accountId, amount.abs());
                } else {
                    this.creditTo(accountId, amount);
                }
            });
            return this;
        }

        public Transaction build() {
            Map<AccountId, Account> accountsInvolved = accountRepository.find(involvedAccountsIds);
            if (accountsInvolved.size() == involvedAccountsIds.size()) {
                Map<Entry, Account> entriesWithAccounts = entries.stream().collect(toMap(it -> it, it -> accountsInvolved.get(it.accountId())));
                return new Transaction(transactionId, null, type, occurredAt, appliesAt, entriesWithAccounts, transactionEntriesConstraint);
            } else {
                String missingAccountIds = subtract(involvedAccountsIds, accountsInvolved.keySet()).stream()
                                                                                                   .map(AccountId::uuid)
                                                                                                   .map(UUID::toString)
                                                                                                   .collect(joining(","));
                throw new IllegalArgumentException(String.format("Accounts %s does not exist", missingAccountIds));
            }
        }

        private EntryId findEntryAllocation(EntryAllocationFilter filter) {
            EntryId refEntryId;
            Entry foundEntry = entryAllocations.findAllocationFor(filter)
                                               .orElseThrow(() -> new IllegalArgumentException("No matching entry found for allocation"));

            validateValidityOf(foundEntry);
            refEntryId = foundEntry.id();
            return refEntryId;
        }

        private void validateValidityOf(Entry referencedEntry) {
            if (!referencedEntry.validity().isValidAt(appliesAt)) {
                throw new IllegalArgumentException(String.format("Referenced entry %s is not valid at %s", referencedEntry.id()
                                                                                                                          .toString(), appliesAt.toString()));
            }
        }
    }

    public class ReverseTransactionEntriesBuilder {

        private final Transaction refTransaction;
        private final List<Entry> entries = new LinkedList<>();
        private final Set<AccountId> involvedAccountsIds = new HashSet<>();

        ReverseTransactionEntriesBuilder(Transaction refTransaction) {
            this.refTransaction = refTransaction;
            refTransaction.entries()
                          .entrySet()
                          .stream()
                          .flatMap(it -> it.getValue().stream())
                          .forEach(this::revert);
        }

        public Transaction build() {
            Map<AccountId, Account> accountsInvolved = accountRepository.find(involvedAccountsIds);
            if (accountsInvolved.size() == involvedAccountsIds.size()) {
                Map<Entry, Account> entriesWithAccounts = entries.stream().collect(toMap(it -> it, it -> accountsInvolved.get(it.accountId())));
                return new Transaction(transactionId, refTransaction.id(), REVERSAL, occurredAt, appliesAt, entriesWithAccounts, transactionEntriesConstraint);
            } else {
                String missingAccountIds = subtract(involvedAccountsIds, accountsInvolved.keySet()).stream()
                                                                                                   .map(AccountId::uuid)
                                                                                                   .map(UUID::toString)
                                                                                                   .collect(joining(","));
                throw new IllegalArgumentException(String.format("Accounts %s does not exist", missingAccountIds));
            }
        }

        private void revert(Entry entry) {
            Entry reverted = switch (entry) {
                case AccountCredited e -> new AccountDebited(e.accountId(), transactionId, e.amount(), appliesAt, occurredAt, e.id());
                case AccountDebited e -> new AccountCredited(e.accountId(), transactionId, e.amount().negate(), appliesAt, occurredAt, e.id());
            };
            this.entries.add(reverted);
            involvedAccountsIds.add(entry.accountId());
        }
    }

    /**
     * Builder for creating expiration compensation transactions.
     * <p>
     * Note: A scheduler service could be implemented to automatically find expired entries
     * using EntryRepository.findAllMatching() with validity.hasExpired() predicate,
     * and then call this builder to create compensation transactions via AccountingFacade.
     */
    public class ExpirationCompensationTransactionEntriesBuilder {

        private final Entry refEntry;
        private final Account account;
        private final Map<Entry, Account> entries = new HashMap<>();
        private Account compensationAccount;

        ExpirationCompensationTransactionEntriesBuilder(Entry refEntry) {
            checkArgument(refEntry.validity().hasExpired(clock.instant()), String.format("Entry %s has not expired yet", refEntry.id()));
            this.refEntry = refEntry;
            this.account = accountRepository.find(refEntry.accountId())
                                            .orElseThrow(() -> new IllegalArgumentException(String.format("Account %s does not exist", refEntry.accountId())));
        }

        ExpirationCompensationTransactionEntriesBuilder withCompensationAccount(AccountId accountId) {
            this.compensationAccount = accountRepository.find(accountId)
                                                        .orElseThrow(() -> new IllegalArgumentException(String.format("Compensation account %s does not exist", refEntry.accountId())));
            return this;
        }

        public Optional<Transaction> build() {
            Money remainingAmount = calculateRemainingAmount();
            if (remainingAmount.isZero()) {
                return Optional.empty();
            } else {
                if (refEntry instanceof AccountCredited) {
                    entries.put(new AccountDebited(refEntry.accountId(), transactionId, remainingAmount.abs(), appliesAt, occurredAt, metadata, refEntry.id()), account);
                    if (compensationAccount != null) {
                        entries.put(new AccountCredited(compensationAccount.id(), transactionId, remainingAmount.abs(), appliesAt, occurredAt, metadata, refEntry.id()), compensationAccount);
                    }
                } else {
                    entries.put(new AccountCredited(refEntry.accountId(), transactionId, remainingAmount.abs(), appliesAt, occurredAt, metadata, refEntry.id()), account);
                    if (compensationAccount != null) {
                        entries.put(new AccountDebited(compensationAccount.id(), transactionId, remainingAmount.abs(), appliesAt, occurredAt, metadata, refEntry.id()), compensationAccount);
                    }
                }
                return Optional.of(
                        new Transaction(transactionId, null, EXPIRATION_COMPENSATION, occurredAt, appliesAt, entries, transactionEntriesConstraint)
                );
            }
        }

        private Money calculateRemainingAmount() {
            Money referencingEntriesTotalAmount = entryRepository.findEntriesReferencing(refEntry)
                                                                 .stream()
                                                                 .map(Entry::amount)
                                                                 .reduce(Money.zeroPln(), Money::add);

            return refEntry.amount().add(referencingEntriesTotalAmount);
        }
    }
}
