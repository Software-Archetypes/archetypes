package com.softwarearchetypes.accounting;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.softwarearchetypes.accounting.events.AccountingEvent;
import com.softwarearchetypes.accounting.events.CreditEntryRegistered;
import com.softwarearchetypes.accounting.events.DebitEntryRegistered;
import com.softwarearchetypes.common.Version;
import com.softwarearchetypes.quantity.money.Money;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

//TODO: add asset type (Unit)
class Account {

    private final AccountId accountId;
    private final AccountType type;
    private final AccountName name;
    private final Money balance;
    //for optimistic locking
    private final Version version;
    //we do not load all accounts' entries when retrieving them from DB
    //adding new entries requires optimistic lock
    private final Entries newEntries;
    private final List<AccountingEvent> pendingEvents = new LinkedList<>();

    Account(AccountId accountId, AccountType type, AccountName name) {
        this(accountId, type, name, Version.initial());
    }

    Account(AccountId accountId, AccountType type, AccountName name, Version version) {
        this(accountId, type, name, Money.zeroPln(), version);
    }

    Account(AccountId accountId, AccountType type, AccountName name, Money balance, Version version) {
        checkArgument(accountId != null, "Account ID must be defined");
        checkArgument(type != null, "Account type must be defined");
        checkArgument(name != null, "Account name must be defined");
        checkArgument(version != null, "Account version must be defined");
        this.accountId = accountId;
        this.type = type;
        this.name = name;
        this.version = version;
        this.balance = Optional.ofNullable(balance).orElse(Money.zeroPln());
        this.newEntries = Entries.empty();
    }

    void addEntry(Entry entry) {
        newEntries.add(entry);
        balance.add(entry.amount());
        recordEntryEvent(entry);
    }

    void addEntries(List<Entry> newEntries) {
        this.newEntries.addAll(newEntries);
        newEntries.forEach(entry -> {
            balance.add(entry.amount());
            recordEntryEvent(entry);
        });
    }

    String name() {
        return name.value();
    }

    private void recordEntryEvent(Entry entry) {
        AccountingEvent event = switch (entry) {
            case AccountCredited credited -> new CreditEntryRegistered(
                    UUID.randomUUID(),
                    entry.occurredAt(),
                    entry.appliesAt(),
                    entry.id().value(),
                    entry.accountId().uuid(),
                    entry.transactionId().value(),
                    entry.amount()
            );
            case AccountDebited debited -> new DebitEntryRegistered(
                    UUID.randomUUID(),
                    entry.occurredAt(),
                    entry.appliesAt(),
                    entry.id().value(),
                    entry.accountId().uuid(),
                    entry.transactionId().value(),
                    entry.amount()
            );
        };
        pendingEvents.add(event);
    }

    List<AccountingEvent> getPendingEvents() {
        return List.copyOf(pendingEvents);
    }

    void clearPendingEvents() {
        pendingEvents.clear();
    }

    AccountId id() {
        return accountId;
    }

    Entries entries() {
        return newEntries.copy();
    }

    AccountType type() {
        return type;
    }

    Money balance() {
        return balance;
    }

    Version version() {
        return version;
    }

    // List<Money> amounts() {
    //     return newEntries.amounts();
    // }

}


