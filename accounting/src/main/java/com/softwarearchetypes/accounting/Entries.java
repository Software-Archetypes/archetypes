package com.softwarearchetypes.accounting;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.softwarearchetypes.quantity.money.Money;

class Entries {

    private List<Entry> entries;

    static Entries empty() {
        return new Entries(new ArrayList<>());
    }

    Entries(List<Entry> entries) {
        this.entries = entries;
    }


    //TODO: powino zostać przeniesione do widoku - ta klasa słuzy tylko do przechowywania nowych wpisów - nie wszystkich
    Money balanceAsOf(Instant when) {
        return entries
                .stream()
                .filter(e -> !e.appliesAt().isAfter(when))
                .map(Entry::amount)
                .reduce(Money.zeroPln(), Money::add);
    }

    Entries add(Entry entry) {
        entries.add(entry);
        return this;
    }

    Entries addAll(List<Entry> newEntries) {
        this.entries.addAll(newEntries);
        return this;
    }

    List<Entry> toList() {
        return new ArrayList<>(entries);
    }

    List<Money> amounts() {
        return entries.stream().map(Entry::amount).toList();
    }

    Entries copy() {
        return new Entries(entries);
    }

    Stream<Entry> stream() {
        return entries.stream();
    }

}
