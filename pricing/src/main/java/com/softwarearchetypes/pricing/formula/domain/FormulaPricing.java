package com.softwarearchetypes.pricing.formula.domain;

import java.math.BigDecimal;

/**
 * Pricing based on defined mathematics formula
 */
public interface FormulaPricing {

    BigDecimal calculatePrice(Object data) throws PriceCalculationException;

}
