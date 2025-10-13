package com.softwarearchetypes.accounting;

import java.time.Instant;
import java.util.Optional;

import com.softwarearchetypes.quantity.money.Money;

sealed interface Entry permits AccountDebited, AccountCredited {

    EntryId id();

    TransactionId transactionId();

    Instant occurredAt();

    Instant appliesAt();

    AccountId accountId();

    Money amount();

    MetaData metadata();

    Validity validity();

    Optional<EntryId> appliedTo();
}

record AccountDebited(EntryId id, TransactionId transactionId, AccountId accountId, Money amount, Instant appliesAt, Instant occurredAt,
                      MetaData metadata, Validity validity, Optional<EntryId> appliedTo) implements Entry {

    AccountDebited(AccountId accountId, TransactionId transactionId, Money amount, Instant appliesAt, Instant occurredAt) {
        this(EntryId.generate(), transactionId, accountId, amount, appliesAt, occurredAt, MetaData.empty(), Validity.always(), Optional.empty());
    }

    AccountDebited(AccountId accountId, TransactionId transactionId, Money amount, Instant appliesAt, Instant occurredAt, MetaData metadata) {
        this(EntryId.generate(), transactionId, accountId, amount, appliesAt, occurredAt, metadata, Validity.always(), Optional.empty());
    }

    AccountDebited(AccountId accountId, TransactionId transactionId, Money amount, Instant appliesAt, Instant occurredAt, MetaData metadata, Validity validity, EntryId appliedToEntryId) {
        this(EntryId.generate(), transactionId, accountId, amount, appliesAt, occurredAt, metadata, validity, Optional.ofNullable(appliedToEntryId));
    }

    AccountDebited(AccountId accountId, TransactionId transactionId, Money amount, Instant appliesAt, Instant occurredAt, EntryId appliedToEntryId) {
        this(EntryId.generate(), transactionId, accountId, amount, appliesAt, occurredAt, MetaData.empty(), Validity.always(), Optional.ofNullable(appliedToEntryId));
    }

    AccountDebited(AccountId accountId, TransactionId transactionId, Money amount, Instant appliesAt, Instant occurredAt, MetaData metadata, EntryId appliedToEntryId) {
        this(EntryId.generate(), transactionId, accountId, amount, appliesAt, occurredAt, metadata, Validity.always(), Optional.ofNullable(appliedToEntryId));
    }

    @Override
    public Money amount() {
        return amount.negate();
    }
}

record AccountCredited(EntryId id, TransactionId transactionId, AccountId accountId, Money amount, Instant appliesAt, Instant occurredAt,
                       MetaData metadata, Validity validity, Optional<EntryId> appliedTo) implements Entry {

    AccountCredited(AccountId accountId, TransactionId transactionId, Money amount, Instant appliesAt, Instant occurredAt) {
        this(EntryId.generate(), transactionId, accountId, amount, appliesAt, occurredAt, MetaData.empty(), Validity.always(), Optional.empty());
    }

    AccountCredited(AccountId accountId, TransactionId transactionId, Money amount, Instant appliesAt, Instant occurredAt, MetaData metadata) {
        this(EntryId.generate(), transactionId, accountId, amount, appliesAt, occurredAt, metadata, Validity.always(), Optional.empty());
    }

    AccountCredited(AccountId accountId, TransactionId transactionId, Money amount, Instant appliesAt, Instant occurredAt, MetaData metadata, Validity validity, EntryId appliedToEntryId) {
        this(EntryId.generate(), transactionId, accountId, amount, appliesAt, occurredAt, metadata, validity, Optional.ofNullable(appliedToEntryId));
    }

    AccountCredited(AccountId accountId, TransactionId transactionId, Money amount, Instant appliesAt, Instant occurredAt, MetaData metadata, EntryId appliedToEntryId) {
        this(EntryId.generate(), transactionId, accountId, amount, appliesAt, occurredAt, metadata, Validity.always(), Optional.ofNullable(appliedToEntryId));
    }

    AccountCredited(AccountId accountId, TransactionId transactionId, Money amount, Instant appliesAt, Instant occurredAt, EntryId appliedToEntryId) {
        this(EntryId.generate(), transactionId, accountId, amount, appliesAt, occurredAt, MetaData.empty(), Validity.always(), Optional.ofNullable(appliedToEntryId));
    }
}

