package com.softwarearchetypes.accounting;

import java.time.Instant;
import java.util.List;

import com.softwarearchetypes.quantity.money.Money;

import static java.util.stream.Collectors.toList;

public record AccountView(AccountId id, String name, String category, Money balance, List<EntryView> entries) {

    //intentionally left package private
    static AccountView from(Account account) {
        List<EntryView> entryViews = account.entries().stream().map(EntryView::from).collect(toList());
        return new AccountView(account.id(), account.name(), account.category().name(), account.balance(), entryViews);
    }

    Money balanceAsOf(Instant time) {
        return entries
                .stream()
                .filter(e -> !e.appliesAt().isAfter(time))
                .map(EntryView::amount)
                .reduce(Money.zeroPln(), Money::add);
    }
}