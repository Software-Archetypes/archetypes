package com.softwarearchetypes.accounting;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.softwarearchetypes.accounting.EntryAllocationStrategy.MANUAL;

class EntryAllocations {

    private final EntryRepository entryRepository;

    EntryAllocations(EntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    Optional<Entry> findAllocationFor(EntryAllocationFilter filter) {
        return entryRepository.findMatching(filter.predicate(), filter.comparator());
    }

}

enum EntryAllocationStrategy {
    FIFO,
    LIFO,
    MANUAL
}

record EntryAllocationFilter(Predicate<Entry> predicate, Comparator<Entry> comparator) {

    static final EntryAllocationFilter NONE = new EntryAllocationFilter(null, null);

    boolean isEmpty() {
        return NONE.equals(this);
    }
}

class EntryAllocationFilterBuilder {

    private EntryAllocationStrategy strategy;
    private Class<? extends Entry> entryType;
    private AccountId accountId;
    private EntryId entryId;
    private Instant time;
    private List<Predicate<EntryId>> predicates;

    EntryAllocationFilterBuilder(EntryAllocationStrategy strategy, AccountId accountId) {
        this.strategy = strategy;
        this.accountId = accountId;
    }

    EntryAllocationFilterBuilder(EntryAllocationStrategy strategy, EntryId entryId) {
        this.strategy = strategy;
        this.entryId = entryId;
    }

    static EntryAllocationFilterBuilder fifo(AccountId accountId) {
        return new EntryAllocationFilterBuilder(EntryAllocationStrategy.FIFO, accountId);
    }

    static EntryAllocationFilterBuilder lifo(AccountId accountId) {
        return new EntryAllocationFilterBuilder(EntryAllocationStrategy.LIFO, accountId);
    }

    static EntryAllocationFilterBuilder Al(EntryId entryId) {
        return new EntryAllocationFilterBuilder(MANUAL, entryId);
    }

    EntryAllocationFilterBuilder withTypeOf(Class<? extends Entry> entryType) {
        this.entryType = entryType;
        return this;
    }

    EntryAllocationFilterBuilder withValidityContaining(Instant time) {
        this.time = time;
        return this;
    }

    //TODO: other filters possible

    EntryAllocationFilter build() {
        return new EntryAllocationFilter(buildEntryPredicate(), buildComparator());
    }

    // Helper method to create entry predicate based on filters
    Predicate<Entry> buildEntryPredicate() {
        Predicate<Entry> predicate = entry -> true;

        if (entryType != null) {
            predicate = predicate.and(entryType::isInstance);
        }

        if (entryId != null) {
            predicate = predicate.and(entry -> entry.id().equals(entryId));
        }

        if (accountId != null) {
            predicate = predicate.and(entry -> entry.accountId().equals(accountId));
        }

        if (time != null) {
            predicate = predicate.and(entry -> entry.validity().isValidAt(time));
        }

        return predicate;
    }

    Comparator<Entry> buildComparator() {
        return switch (strategy) {
            case FIFO -> Comparator.comparing(Entry::appliesAt);
            case LIFO -> Comparator.comparing(Entry::appliesAt).reversed();
            case MANUAL -> null;
        };
    }

}
