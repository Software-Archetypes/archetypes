package com.bartslota.availability.domain


import java.time.Duration

import static java.time.temporal.ChronoUnit.MINUTES
import static org.apache.commons.lang3.RandomUtils.nextLong

class DurationFixture {

    static Duration someValidDuration() {
        Duration.of(nextLong(1, 15), MINUTES)
    }
}
