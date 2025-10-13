package com.softwarearchetypes.accounting;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.softwarearchetypes.quantity.money.Money;

import static com.softwarearchetypes.common.Preconditions.checkArgument;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;

public final class Transaction {

    private final TransactionId id;
    private final TransactionId refId;
    private final TransactionType type;
    private final Instant occurredAt;
    private final Instant appliesAt;
    //transient
    private Map<Account, List<Entry>> entries;

    //package private
    Transaction(TransactionId id, TransactionId refId, TransactionType type, Instant occurredAt, Instant appliesAt,
            Map<Entry, Account> entries, TransactionEntriesConstraint transactionEntriesConstraint) {
        checkArgument(id != null, "Transaction must have its ID");
        checkArgument(type != null, "Transaction must have its type");
        checkArgument(occurredAt != null, "Transaction must have its occurrence time");
        checkArgument(appliesAt != null, "Transaction must have its application time");
        // these two constraints are usually true for on-balance transactions, but does not have to be true if off-balance
        // accounts are involved - these cases were moved to TransactionEntriesConstraint implementation
        // checkArgument(entries != null && entries.size() >= 2, "Transaction must have at least 2 entries");
        // checkArgument(new HashSet<>(entries.values()).size() >= 2, "Transaction must involve at least 2 accounts");
        checkArgument(transactionEntriesConstraint.test(entries), transactionEntriesConstraint.errorMessage());
        this.id = id;
        this.refId = refId;
        this.type = type;
        this.occurredAt = occurredAt;
        this.appliesAt = appliesAt;
        this.entries = entries.entrySet().stream().collect(groupingBy(Map.Entry::getValue, mapping(Map.Entry::getKey, Collectors.toList())));
    }

    public TransactionId id() {
        return id;
    }

    Optional<TransactionId> refId() {
        return Optional.ofNullable(refId);
    }

    Set<Account> accountsInvolved() {
        return entries.keySet();
    }

    Map<Account, List<Entry>> entries() {
        return Map.copyOf(entries);
    }

    Instant occurredAt() {
        return occurredAt;
    }

    Instant appliesAt() {
        return appliesAt;
    }

    TransactionType type() {
        return type;
    }

    //intentionally left non-public
    void execute() {
        entries.forEach(Account::addEntries);
    }

}

interface TransactionEntriesConstraint extends Predicate<Map<Entry, Account>> {

    String errorMessage();

    TransactionEntriesConstraint BALANCING_CONSTRAINT = new TransactionEntriesConstraint() {
        @Override
        public String errorMessage() {
            return "Entry balance within transaction must always be 0";
        }

        @Override
        public boolean test(Map<Entry, Account> entries) {
            Money balance = Optional.ofNullable(entries).orElse(Map.of())
                                    .entrySet()
                                    .stream()
                                    .filter(entry -> entry.getValue().category().isDoubleEntryBookingEnabled())
                                    .map(entry -> entry.getKey().amount())
                                    .reduce(Money.zeroPln(), Money::add);
            return balance.isZero();
        }
    };

    TransactionEntriesConstraint MIN_2_ENTRIES_CONSTRAINT = new TransactionEntriesConstraint() {
        @Override
        public String errorMessage() {
            return "Transaction must have at least 2 entries";
        }

        @Override
        public boolean test(Map<Entry, Account> entries) {
            return Optional.ofNullable(entries).orElse(Map.of()).size() >= 2;
        }
    };


    TransactionEntriesConstraint MIN_2_ACCOUNTS_INVOLVED_CONSTRAINT = new TransactionEntriesConstraint() {
        @Override
        public String errorMessage() {
            return "Transaction must involve at least 2 accounts";
        }

        @Override
        public boolean test(Map<Entry, Account> entries) {
            return new HashSet<>(Optional.ofNullable(entries).orElse(Map.of()).values()).size() >= 2;
        }
    };
}

