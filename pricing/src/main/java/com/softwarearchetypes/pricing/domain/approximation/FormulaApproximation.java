package com.softwarearchetypes.pricing.domain.approximation;

import com.softwarearchetypes.pricing.domain.PriceTable;

/**
 * Interface that represents different implementations of formula approximation based on price table
 */
public interface FormulaApproximation {

    String approximate(PriceTable priceTable);
}
