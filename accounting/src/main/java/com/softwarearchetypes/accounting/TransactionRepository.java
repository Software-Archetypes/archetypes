package com.softwarearchetypes.accounting;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

interface TransactionRepository {

    Optional<Transaction> find(TransactionId transactionId);

    Transaction save(Transaction transaction);

}

class InMemoryTransactionRepo implements TransactionRepository {

    private final Map<TransactionId, Transaction> transactions = new HashMap<>();

    @Override
    public Optional<Transaction> find(TransactionId transactionId) {
        return Optional.ofNullable(transactions.get(transactionId));
    }

    @Override
    public Transaction save(Transaction transaction) {
        return transactions.put(transaction.id(), transaction);
    }

}
