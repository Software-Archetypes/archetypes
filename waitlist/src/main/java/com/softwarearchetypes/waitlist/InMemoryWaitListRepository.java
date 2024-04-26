package com.softwarearchetypes.waitlist;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

@Repository
class InMemoryWaitListRepository implements WaitListRepository {

    private final ConcurrentHashMap<WaitListId, WaitList> map = new ConcurrentHashMap<>();

    @Override
    public Optional<WaitList> findBy(WaitListId waitListId) {
        return Optional.ofNullable(map.get(waitListId));
    }

    @Override
    public void save(WaitList waitList) {
        map.put(waitList.id(), waitList);
    }
}
