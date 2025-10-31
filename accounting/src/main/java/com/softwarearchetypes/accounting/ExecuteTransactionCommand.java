package com.softwarearchetypes.accounting;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.softwarearchetypes.quantity.money.Money;

/**
 * Command for executing a new transaction with credit and debit entries.
 * This is the primary way to create and execute transactions through AccountingFacade.
 */
public record ExecuteTransactionCommand(
        Instant occurredAt,
        Instant appliesAt,
        String transactionType,
        Map<String, String> metadata,  // nullable
        List<Entry> entries
) {

    public record Entry(
            EntryType entryType,
            UUID accountId,
            Money amount,
            Instant validFrom,         // nullable - no start limit if null
            Instant validTo,           // nullable - no expiration if null
            UUID appliedToEntryId      // nullable - no allocation if null
    ) {
        public static Entry credit(UUID accountId, Money amount) {
            return new Entry(EntryType.CREDIT, accountId, amount, null, null, null);
        }

        public static Entry debit(UUID accountId, Money amount) {
            return new Entry(EntryType.DEBIT, accountId, amount, null, null, null);
        }
    }

    public enum EntryType {
        CREDIT, DEBIT
    }
}
