package com.softwarearchetypes.accounting;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// it is an alternative to searching for entries through accounts
interface EntryRepository {

    Optional<Entry> find(EntryId entryId);

    void save(Entry entry);

    List<Entry> findAllFor(AccountId accountId);

    Optional<Entry> findMatching(Predicate<Entry> predicate, Comparator<Entry> comparator);

    List<Entry> findAllMatching(Predicate<Entry> predicate);

    List<Entry> findEntriesReferencing(Entry entry);
}

class InMemoryEntryRepository implements EntryRepository {

    private final Map<EntryId, Entry> entries = new HashMap<>();

    @Override
    public Optional<Entry> find(EntryId entryId) {
        return Optional.ofNullable(entries.get(entryId));
    }

    @Override
    public void save(Entry entry) {
        entries.put(entry.id(), entry);
    }

    @Override
    public List<Entry> findAllFor(AccountId accountId) {
        return findAllMatching(it -> it.accountId().equals(accountId));
    }

    @Override
    public Optional<Entry> findMatching(Predicate<Entry> predicate, Comparator<Entry> comparator) {
        Stream<Entry> stream = entries.values().stream();
        if (predicate != null) {
            stream = stream.filter(predicate);
        }
        if (comparator != null) {
            stream = stream.sorted(comparator);
        }
        return stream.findFirst();
    }

    @Override
    public List<Entry> findEntriesReferencing(Entry entry) {
        return findAllMatching(
                e -> e.appliedTo().map(id -> id.equals(entry.id())).orElse(false)
        );
    }

    @Override
    public List<Entry> findAllMatching(Predicate<Entry> predicate) {
        Stream<Entry> stream = entries.values().stream();
        if (predicate != null) {
            stream = stream.filter(predicate);
        }
        return stream.collect(Collectors.toList());
    }
}