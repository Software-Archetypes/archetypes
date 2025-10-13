package com.softwarearchetypes.accounting;

import java.time.Instant;

import com.softwarearchetypes.quantity.money.Money;

public record EntryView(EntryId entryId,
                        EntryType type,
                        Money amount,
                        TransactionId transactionId,
                        AccountId accountId,
                        Instant occurredAt,
                        Instant appliesAt) {

    //intentionally left package-scoped
    static EntryView from(Entry entry) {
        return new EntryView(entry.id(),
                EntryType.from(entry),
                entry.amount(),
                entry.transactionId(),
                entry.accountId(),
                entry.occurredAt(),
                entry.appliesAt());
    }

    public enum EntryType {
        CREDIT,
        DEBIT;

        static EntryType from(Entry entry) {
            return switch (entry) {
                case AccountCredited c -> CREDIT;
                case AccountDebited d -> DEBIT;
            };
        }
    }
}
