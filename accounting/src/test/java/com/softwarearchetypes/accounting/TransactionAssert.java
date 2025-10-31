package com.softwarearchetypes.accounting;

import java.time.Instant;
import java.util.List;

import org.assertj.core.api.AbstractAssert;

import com.softwarearchetypes.quantity.money.Money;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransactionAssert extends AbstractAssert<TransactionAssert, Transaction> {

    public TransactionAssert(Transaction actual) {
        super(actual, TransactionAssert.class);
    }

    public static TransactionAssert assertThat(Transaction actual) {
        return new TransactionAssert(actual);
    }

    public TransactionAssert occurredAt(Instant time) {
        assertEquals(time, actual.occurredAt());
        return this;
    }

    public TransactionAssert appliesAt(Instant time) {
        assertEquals(time, actual.appliesAt());
        return this;
    }

    TransactionAssert hasTypeEqualTo(TransactionType transactionType) {
        assertEquals(transactionType, actual.type());
        return this;
    }

    TransactionAssert hasIdEqualTo(TransactionId transactionId) {
        assertEquals(transactionId, actual.id());
        return this;
    }

    TransactionAssert hasReferenceTo(TransactionId transactionId) {
        assertTrue(actual.refId().filter(it -> it.equals(transactionId)).isPresent());
        return this;
    }

    TransactionAssert hasExactlyOneCreditEntryFor(Account account, Money amount) {
        assertEquals(1, actual.entries().get(account).stream()
                              .filter(AccountCredited.class::isInstance)
                              .filter(it -> it.amount().equals(amount)).toList().size());
        return this;
    }

    TransactionAssert hasExactlyOneCreditEntryFor(Account account, Money amount, Validity validity) {
        assertEquals(1, actual.entries().get(account).stream()
                              .filter(AccountCredited.class::isInstance)
                              .filter(it -> it.amount().equals(amount))
                              .filter(it -> it.validity().equals(validity)).toList().size());
        return this;
    }

    TransactionAssert hasExactlyOneCreditEntryFor(Account account, Money amount, EntryId appliedTo) {
        assertEquals(1, actual.entries().get(account).stream()
                              .filter(AccountCredited.class::isInstance)
                              .filter(it -> it.amount().equals(amount))
                              .filter(it -> it.appliedTo().filter(id -> id.equals(appliedTo)).isPresent()).toList().size());
        return this;
    }

    TransactionAssert hasExactlyOneCreditEntryFor(Account account, Money amount, Validity validity, EntryId appliedTo) {
        assertEquals(1, actual.entries().get(account).stream()
                              .filter(AccountCredited.class::isInstance)
                              .filter(it -> it.amount().equals(amount))
                              .filter(it -> it.validity().equals(validity))
                              .filter(it -> it.appliedTo().filter(id -> id.equals(appliedTo)).isPresent()).toList().size());
        return this;
    }

    TransactionAssert hasExactlyOneDebitEntryFor(Account account, Money amount) {
        assertEquals(1, actual.entries().get(account).stream()
                              .filter(AccountDebited.class::isInstance)
                              .filter(it -> it.amount().equals(amount.negate())).toList().size());
        return this;
    }

    TransactionAssert hasExactlyOneDebitEntryFor(Account account, Money amount, Validity validity) {
        assertEquals(1, actual.entries().get(account).stream()
                              .filter(AccountDebited.class::isInstance)
                              .filter(it -> it.amount().equals(amount.negate()))
                              .filter(it -> it.validity().equals(validity)).toList().size());
        return this;
    }

    TransactionAssert hasExactlyOneDebitEntryFor(Account account, Money amount, EntryId appliedTo) {
        assertEquals(1, actual.entries().get(account).stream()
                              .filter(AccountDebited.class::isInstance)
                              .filter(it -> it.amount().equals(amount.negate()))
                              .filter(it -> it.appliedTo().filter(id -> id.equals(appliedTo)).isPresent()).toList().size());
        return this;
    }

    TransactionAssert hasExactlyOneDebitEntryFor(Account account, Money amount, Validity validity, EntryId appliedTo) {
        assertEquals(1, actual.entries().get(account).stream()
                              .filter(AccountDebited.class::isInstance)
                              .filter(it -> it.amount().equals(amount.negate()))
                              .filter(it -> it.validity().equals(validity))
                              .filter(it -> it.appliedTo().filter(id -> id.equals(appliedTo)).isPresent()).toList().size());
        return this;
    }

    TransactionEntriesAssert containsEntries() {
        return new TransactionEntriesAssert(actual.entries().values().stream().flatMap(List::stream).collect(toList()));
    }
}

class TransactionEntriesAssert extends AbstractAssert<TransactionEntriesAssert, List<Entry>> {

    TransactionEntriesAssert(List<Entry> actual) {
        super(actual, TransactionEntriesAssert.class);
    }

    TransactionEntriesAssert allOccurredAt(Instant time) {
        assertTrue(actual.stream().allMatch(it -> time.equals(it.occurredAt())));
        return this;
    }

    TransactionEntriesAssert allAppliesAt(Instant time) {
        assertTrue(actual.stream().allMatch(it -> time.equals(it.appliesAt())));
        return this;
    }

    TransactionEntriesAssert allReferencedTo(TransactionId transactionId) {
        assertTrue(actual.stream().allMatch(it -> transactionId.equals(it.transactionId())));
        return this;
    }

    TransactionEntriesAssert allHavingMetadata(MetaData metadata) {
        assertTrue(actual.stream().allMatch(it -> metadata.equals(it.metadata())));
        return this;
    }


}

