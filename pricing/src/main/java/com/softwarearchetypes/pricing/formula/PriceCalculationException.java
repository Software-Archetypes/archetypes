package com.softwarearchetypes.pricing.formula;

public class PriceCalculationException extends Exception {

    public PriceCalculationException(String message) {
        super(message);
    }

    public PriceCalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}
