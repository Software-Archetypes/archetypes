package com.softwarearchetypes.pricing.formula.domain;

import com.softwarearchetypes.pricing.common.BigDecimalAssert;
import org.junit.jupiter.api.Assertions;

public class FormulaPricingAssert {

    private final FormulaPricing formulaPricing;

    public FormulaPricingAssert(FormulaPricing formulaPricing) {
        this.formulaPricing = formulaPricing;
    }


    public FormulaPricingAssert formulaExists() {
        Assertions.assertNotNull(formulaPricing);
        return this;
    }

    public BigDecimalAssert formulaResult(Object data) throws PriceCalculationException {
        return new BigDecimalAssert(formulaPricing.calculatePrice(data));
    }

}
