package com.softwarearchetypes.waitlist;

import java.util.UUID;

record WaitListId(UUID value) {

    static WaitListId random() {
        return new WaitListId(UUID.randomUUID());
    }
}
