package com.softwarearchetypes.pricing.domain;

import org.javamoney.moneta.Money;

import java.time.OffsetDateTime;

public record Price(
        Money amount,
        OffsetDateTime validFrom,
        OffsetDateTime validTo
) {

}
