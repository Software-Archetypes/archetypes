package com.softwarearchetypes.waitlist;

import java.util.Optional;

interface WaitListRepository {

    Optional<WaitList> findBy(WaitListId waitListId);

    void save(WaitList waitList);

    default WaitList findIfExistsBy(WaitListId waitListId) {
        return findBy(waitListId).orElseThrow(() -> new IllegalStateException("Wait list of given ID does not exist"));
    }

}
