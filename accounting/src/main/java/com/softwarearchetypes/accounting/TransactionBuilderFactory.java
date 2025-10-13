package com.softwarearchetypes.accounting;

import java.time.Clock;

public class TransactionBuilderFactory {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final EntryAllocations entryAllocations;
    private final EntryRepository entryRepository;
    private final Clock clock;

    TransactionBuilderFactory(AccountRepository accountRepository, TransactionRepository transactionRepository, EntryAllocations entryAllocations, EntryRepository entryRepository, Clock clock) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.entryAllocations = entryAllocations;
        this.entryRepository = entryRepository;
        this.clock = clock;
    }

    public TransactionBuilder transaction() {
        return new TransactionBuilder(accountRepository, transactionRepository, this.entryAllocations, this.entryRepository, this.clock);
    }
}
