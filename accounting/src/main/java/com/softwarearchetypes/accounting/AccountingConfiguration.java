package com.softwarearchetypes.accounting;

public class AccountingConfiguration {

    // private final AccountRepository accountRepository;
    // private final TransactionBuilderFactory transactionBuilderFactory;
    // private final AccountingFacade accountingFacade;
    //
    // AccountingConfiguration(AccountRepository accountRepository, TransactionBuilderFactory transactionBuilderFactory, AccountingFacade accountingFacade) {
    //     this.accountRepository = accountRepository;
    //     this.transactionBuilderFactory = transactionBuilderFactory;
    //     this.accountingFacade = accountingFacade;
    // }
    //
    // public static AccountingConfiguration inMemory(Clock clock) {
    //     return inMemory(clock, EventPublishingConfiguration.inMemory().publisher());
    // }
    //
    // public static AccountingConfiguration inMemory(Clock clock, EventPublisher eventPublisher) {
    //     InMemoryEntryRepository entryRepository = new InMemoryEntryRepository();
    //     InMemoryAccountRepo accountRepository = new InMemoryAccountRepo(entryRepository);
    //     InMemoryTransactionRepo transactionRepository = new InMemoryTransactionRepo();
    //     EntryAllocations entryAllocations = new EntryAllocations(entryRepository);
    //     TransactionBuilderFactory transactionBuilderFactory = new TransactionBuilderFactory(accountRepository, transactionRepository, entryAllocations, entryRepository, clock);
    //     AccountingFacade accountingFacade = new AccountingFacade(clock, accountRepository, transactionRepository, transactionBuilderFactory, eventPublisher);
    //     return new AccountingConfiguration(accountRepository, transactionBuilderFactory, accountingFacade);
    // }
    //
    // public AccountingFacade facade() {
    //     return accountingFacade;
    // }
    //
    // public AccountRepository repository() {
    //     return accountRepository;
    // }
    //
    // public TransactionBuilderFactory transactionBuilderFactory() {
    //     return transactionBuilderFactory;
    // }
}
