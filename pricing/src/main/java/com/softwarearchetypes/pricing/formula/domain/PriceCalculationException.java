package com.softwarearchetypes.pricing.formula.domain;

public class PriceCalculationException extends Exception {

    public PriceCalculationException(String message) {
        super(message);
    }

    public PriceCalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}
