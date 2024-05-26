package com.bartslota.availability.domain


import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric

class OwnerIdFixture {

    static OwnerId someOwnerId() {
        OwnerId.of(randomAlphanumeric(10))
    }
}
