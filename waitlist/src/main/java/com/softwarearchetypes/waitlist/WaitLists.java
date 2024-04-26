package com.softwarearchetypes.waitlist;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
class WaitLists {

    private final WaitListRepository repository;

    WaitLists(WaitListRepository repository) {
        this.repository = repository;
    }

    WaitList registerFIFOWaitListFor(int capacity) {
        WaitList waitList = WaitList.waitListWithCapacityOf(capacity);
        repository.save(waitList);
        return waitList;
    }

    WaitList registerPrioritizedWaitListFor(int capacity) {
        WaitList waitList = WaitList.prioritizedWaitListWithCapacityOf(capacity);
        repository.save(waitList);
        return waitList;
    }

    void addToWaitList(WaitListId waitListId, UUID value) {
        WaitList waitList = repository.findIfExistsBy(waitListId);
        waitList.add(Element.of(value));
        repository.save(waitList);
    }

    void addToWaitList(WaitListId waitListId, UUID value, int priority) {
        WaitList waitList = repository.findIfExistsBy(waitListId);
        waitList.add(Element.of(value, priority));
        repository.save(waitList);
    }

    boolean removeFromWaitList(WaitListId waitListId, UUID value) {
        WaitList waitList = repository.findIfExistsBy(waitListId);
        return waitList.removeByValue(value);
    }

    WaitList loadWaitListBy(WaitListId waitListId) {
        return repository.findBy(waitListId).orElse(null);
    }

}
