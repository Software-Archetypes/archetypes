package com.softwarearchetypes.accounting;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.softwarearchetypes.quantity.money.Money;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntryAllocationsTest {

    static final Instant TUESDAY_09_00 = LocalDateTime.of(2022, 2, 2, 9, 0).atZone(ZoneId.systemDefault()).toInstant();
    static final Instant TUESDAY_09_10 = LocalDateTime.of(2022, 2, 2, 9, 10).atZone(ZoneId.systemDefault()).toInstant();
    static final Instant TUESDAY_09_20 = LocalDateTime.of(2022, 2, 2, 9, 20).atZone(ZoneId.systemDefault()).toInstant();
    static final Instant TUESDAY_10_00 = LocalDateTime.of(2022, 2, 2, 10, 0).atZone(ZoneId.systemDefault()).toInstant();
    static final Instant TUESDAY_11_00 = LocalDateTime.of(2022, 2, 2, 11, 0).atZone(ZoneId.systemDefault()).toInstant();
    static final Instant TUESDAY_12_00 = LocalDateTime.of(2022, 2, 2, 12, 0).atZone(ZoneId.systemDefault()).toInstant();

    EntryRepository entryRepository = new InMemoryEntryRepository();
    EntryAllocations entryAllocations = new EntryAllocations(entryRepository);

    @Test
    void should_find_oldest_entry_with_fifo_strategy() {
        //given
        AccountId accountId = AccountId.generate();
        Entry entry1 = persisted(new AccountCredited(accountId, TransactionId.generate(), Money.pln(100), TUESDAY_09_00, TUESDAY_09_00));
        Entry entry2 = persisted(new AccountCredited(accountId, TransactionId.generate(), Money.pln(200), TUESDAY_10_00, TUESDAY_10_00));
        Entry entry3 = persisted(new AccountCredited(accountId, TransactionId.generate(), Money.pln(300), TUESDAY_11_00, TUESDAY_11_00));
        Entry entry4 = persisted(new AccountCredited(accountId, TransactionId.generate(), Money.pln(400), TUESDAY_12_00, TUESDAY_12_00));

        //when
        EntryAllocationFilter filter = EntryAllocationFilterBuilder.fifo(accountId).build();
        Optional<Entry> result = entryAllocations.findAllocationFor(filter);

        //then
        assertTrue(result.isPresent());
        assertEquals(entry1.id(), result.get().id());
    }

    @Test
    void should_find_newest_entry_with_lifo_strategy() {
        //given
        AccountId accountId = AccountId.generate();
        Entry entry1 = persisted(new AccountCredited(accountId, TransactionId.generate(), Money.pln(100), TUESDAY_09_00, TUESDAY_09_00));
        Entry entry2 = persisted(new AccountCredited(accountId, TransactionId.generate(), Money.pln(200), TUESDAY_10_00, TUESDAY_10_00));
        Entry entry3 = persisted(new AccountCredited(accountId, TransactionId.generate(), Money.pln(300), TUESDAY_11_00, TUESDAY_11_00));
        Entry entry4 = persisted(new AccountCredited(accountId, TransactionId.generate(), Money.pln(400), TUESDAY_12_00, TUESDAY_12_00));

        //when
        EntryAllocationFilter filter = EntryAllocationFilterBuilder.lifo(accountId).build();
        Optional<Entry> result = entryAllocations.findAllocationFor(filter);

        //then
        assertTrue(result.isPresent());
        assertEquals(entry4.id(), result.get().id());
    }

    @Test
    void should_find_specific_entry_with_manual_strategy() {
        //given
        AccountId accountId = AccountId.generate();
        Entry entry1 = persisted(new AccountCredited(accountId, TransactionId.generate(), Money.pln(100), TUESDAY_09_00, TUESDAY_09_00));
        Entry entry2 = persisted(new AccountCredited(accountId, TransactionId.generate(), Money.pln(200), TUESDAY_10_00, TUESDAY_10_00));
        Entry entry3 = persisted(new AccountCredited(accountId, TransactionId.generate(), Money.pln(300), TUESDAY_11_00, TUESDAY_11_00));
        Entry entry4 = persisted(new AccountCredited(accountId, TransactionId.generate(), Money.pln(400), TUESDAY_12_00, TUESDAY_12_00));

        //when
        EntryAllocationFilter filter = EntryAllocationFilterBuilder.manual(entry3.id()).build();
        Optional<Entry> result = entryAllocations.findAllocationFor(filter);

        //then
        assertTrue(result.isPresent());
        assertEquals(entry3.id(), result.get().id());
    }

    @Test
    void should_filter_by_entry_type() {
        //given
        AccountId accountId = AccountId.generate();
        Entry entry1 = persisted(new AccountCredited(accountId, TransactionId.generate(), Money.pln(100), TUESDAY_09_00, TUESDAY_09_00));
        Entry entry2 = persisted(new AccountDebited(accountId, TransactionId.generate(), Money.pln(200), TUESDAY_10_00, TUESDAY_10_00));
        Entry entry3 = persisted(new AccountCredited(accountId, TransactionId.generate(), Money.pln(300), TUESDAY_11_00, TUESDAY_11_00));
        Entry entry4 = persisted(new AccountDebited(accountId, TransactionId.generate(), Money.pln(400), TUESDAY_12_00, TUESDAY_12_00));

        //when
        EntryAllocationFilter filter = EntryAllocationFilterBuilder.fifo(accountId)
                .withTypeOf(AccountDebited.class)
                .build();
        Optional<Entry> result = entryAllocations.findAllocationFor(filter);

        //then
        assertTrue(result.isPresent());
        assertEquals(entry2.id(), result.get().id());
        assertInstanceOf(AccountDebited.class, result.get());
    }

    @Test
    void should_filter_by_validity_time() {
        //given
        AccountId accountId = AccountId.generate();
        Validity validUntil10 = Validity.until(TUESDAY_10_00);
        Validity validUntil11 = Validity.until(TUESDAY_11_00);
        Validity validUntil12 = Validity.until(TUESDAY_12_00);
        Validity alwaysValid = Validity.always();

        //and
        Entry entry1 = persisted(new AccountCredited(accountId, TransactionId.generate(), Money.pln(100), TUESDAY_09_00, TUESDAY_09_00,
                MetaData.empty(), validUntil10, null));
        Entry entry2 = persisted(new AccountCredited(accountId, TransactionId.generate(), Money.pln(200), TUESDAY_09_10, TUESDAY_09_10,
                MetaData.empty(), validUntil11, null));
        Entry entry3 = persisted(new AccountCredited(accountId, TransactionId.generate(), Money.pln(300), TUESDAY_09_20, TUESDAY_09_20,
                MetaData.empty(), validUntil12, null));
        Entry entry4 = persisted(new AccountCredited(accountId, TransactionId.generate(), Money.pln(400), TUESDAY_10_00, TUESDAY_10_00,
                MetaData.empty(), alwaysValid, null));

        //when
        EntryAllocationFilter filter = EntryAllocationFilterBuilder.fifo(accountId)
                .withValidityContaining(TUESDAY_10_00.plusSeconds(30))
                .build();
        Optional<Entry> result = entryAllocations.findAllocationFor(filter);

        //then
        assertTrue(result.isPresent());
        assertEquals(entry2.id(), result.get().id());
    }

    @Test
    void should_return_empty_when_no_matching_entry() {
        //given
        AccountId accountId = AccountId.generate();
        AccountId differentAccountId = AccountId.generate();
        Entry entry1 = persisted(new AccountCredited(differentAccountId, TransactionId.generate(), Money.pln(100), TUESDAY_09_00, TUESDAY_09_00));
        Entry entry2 = persisted(new AccountCredited(differentAccountId, TransactionId.generate(), Money.pln(200), TUESDAY_10_00, TUESDAY_10_00));
        Entry entry3 = persisted(new AccountCredited(differentAccountId, TransactionId.generate(), Money.pln(300), TUESDAY_11_00, TUESDAY_11_00));
        Entry entry4 = persisted(new AccountCredited(differentAccountId, TransactionId.generate(), Money.pln(400), TUESDAY_12_00, TUESDAY_12_00));

        //when
        EntryAllocationFilter filter = EntryAllocationFilterBuilder.fifo(accountId).build();
        Optional<Entry> result = entryAllocations.findAllocationFor(filter);

        //then
        assertTrue(result.isEmpty());
    }

    private Entry persisted(Entry entry) {
        entryRepository.save(entry);
        return entry;
    }
}