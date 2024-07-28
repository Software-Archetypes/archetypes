package com.softwarearchetypes.pricing.shared;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public abstract class AbstractUnitTest {

    protected Clock clock = Clock.systemUTC();

    protected OffsetDateTime setClock(int year, int month, int day) {
        var offsetDateTime = OffsetDateTime.of(year, month, day, 0, 0, 0, 0, ZoneOffset.UTC);
        this.clock = Clock.fixed(offsetDateTime.toInstant(), ZoneOffset.UTC);
        return offsetDateTime;
    }

}
