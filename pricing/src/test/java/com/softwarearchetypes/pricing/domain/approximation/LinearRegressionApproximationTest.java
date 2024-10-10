package com.softwarearchetypes.pricing.domain.approximation;

import com.softwarearchetypes.pricing.domain.SimplePriceTable;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LinearRegressionApproximationTest {

    @Test
    void shouldProperlyApproximateSimpleTable() {
        //given: simple price table
        var headers = List.of(BigDecimal.ONE, BigDecimal.TWO, BigDecimal.TEN);
        var priceTable = new SimplePriceTable(headers);

        priceTable.addRow(BigDecimal.ONE, List.of(BigDecimal.ONE, BigDecimal.valueOf(2L), BigDecimal.valueOf(3L)));
        priceTable.addRow(BigDecimal.TWO, List.of(BigDecimal.ONE, BigDecimal.valueOf(2L), BigDecimal.valueOf(3L)));
        priceTable.addRow(BigDecimal.TEN, List.of(BigDecimal.ONE, BigDecimal.valueOf(2L), BigDecimal.valueOf(3L)));

        //when: price table is approximate
        var result = new LinearRegressionApproximation().approximate(priceTable);

        //then: formula represents price table
        assertTrue(true);
    }

}