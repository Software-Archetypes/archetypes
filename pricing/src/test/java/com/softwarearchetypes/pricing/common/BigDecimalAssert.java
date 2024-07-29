package com.softwarearchetypes.pricing.common;

import org.junit.jupiter.api.Assertions;

import java.math.BigDecimal;

public class BigDecimalAssert {

    private final BigDecimal value;

    public BigDecimalAssert(BigDecimal value) {
        this.value = value;
    }

    public BigDecimalAssert hasValue(BigDecimal expectedValue) {
        Assertions.assertEquals(0, value.compareTo(expectedValue));
        return this;
    }
}
