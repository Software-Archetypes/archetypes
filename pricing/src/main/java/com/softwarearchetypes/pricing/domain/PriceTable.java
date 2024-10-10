package com.softwarearchetypes.pricing.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * Represents any kind on price table, tariff, matrix, grid
 * that is used to determine price.
 */
public interface PriceTable {

    List<BigDecimal> getHeader();

    List<PriceTableRow> getRows();


    interface PriceTableRow {
        BigDecimal getValue();

        List<BigDecimal> getCosts();
    }
}
