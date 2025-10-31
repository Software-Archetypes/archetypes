package com.softwarearchetypes.accounting;

import java.time.Instant;
import java.util.List;

import org.assertj.core.api.AbstractAssert;

import io.pillopl.common.Money;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransactionViewAssert extends AbstractAssert<TransactionViewAssert, TransactionView> {

    public TransactionViewAssert(TransactionView actual) {
        super(actual, TransactionViewAssert.class);
    }

    public static TransactionViewAssert assertThat(TransactionView actual) {
        return new TransactionViewAssert(actual);
    }

    public TransactionViewAssert hasId(TransactionId id) {
        assertEquals(id, actual.id());
        return this;
    }

    public TransactionViewAssert hasType(TransactionType type) {
        assertEquals(type, actual.type());
        return this;
    }

    public TransactionViewAssert occurredAt(Instant time) {
        assertEquals(time, actual.occurredAt());
        return this;
    }

    public TransactionViewAssert appliesAt(Instant time) {
        assertEquals(time, actual.appliesAt());
        return this;
    }

    public TransactionViewAssert hasReferenceTo(TransactionId transactionId) {
        assertTrue(actual.refId() != null && actual.refId().equals(transactionId));
        return this;
    }

    public TransactionEntriesViewAssert containsEntries() {
        return new TransactionEntriesViewAssert(actual.entries());
    }
}


class TransactionEntriesViewAssert extends AbstractAssert<TransactionEntriesViewAssert, List<TransactionAccountEntriesView>> {

    public TransactionEntriesViewAssert(List<TransactionAccountEntriesView> actual) {
        super(actual, TransactionEntriesViewAssert.class);
    }

    public TransactionEntriesViewAssert allOccurredAt(Instant time) {
        assertTrue(allEntries().stream().allMatch(e -> time.equals(e.occurredAt())),
                "Not all entries occurred at: " + time);
        return this;
    }

    public TransactionEntriesViewAssert allHaveTransactionId(TransactionId id) {
        assertTrue(allEntries().stream().allMatch(e -> id.equals(e.transactionId())),
                "Not all entries have transaction ID: " + id);
        return this;
    }

    public TransactionEntriesViewAssert containExactly(int expectedCount) {
        assertEquals(expectedCount, allEntries().size(),
                String.format("Expected %d entries, but got %d", expectedCount, allEntries().size()));
        return this;
    }

    public TransactionEntriesViewAssert containExactlyOneEntry(AccountId accountId, EntryView.EntryType type, Money amount) {
        List<EntryView> entries = entriesFor(accountId).stream()
                                                       .filter(e -> e.type() == type && e.amount().equals(amount))
                                                       .toList();

        assertEquals(1, entries.size(),
                String.format("Expected exactly one %s entry with amount %s for account %s but got %d",
                        type, amount, accountId, entries.size()));
        return this;
    }

    public TransactionEntriesViewAssert containEntry(AccountId accountId, EntryView.EntryType type, Money amount) {
        boolean found = entriesFor(accountId).stream()
                                             .anyMatch(e -> e.type() == type && e.amount().equals(amount));

        assertTrue(found,
                String.format("Expected %s entry with amount %s for account %s not found",
                        type, amount, accountId));
        return this;
    }

    private List<EntryView> entriesFor(AccountId accountId) {
        return actual.stream()
                     .filter(e -> e.account().id().equals(accountId))
                     .flatMap(e -> e.entries().stream())
                     .toList();
    }

    private List<EntryView> allEntries() {
        return actual.stream()
                     .flatMap(e -> e.entries().stream())
                     .toList();
    }
}



