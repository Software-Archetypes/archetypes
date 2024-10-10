package com.softwarearchetypes.pricing.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SimplePriceTable implements PriceTable {

    private final List<BigDecimal> headers;
    private final List<PriceTableRow> rows;

    public SimplePriceTable(List<BigDecimal> headers) {
        this.headers = List.copyOf(headers);
        this.rows = new ArrayList<>();
    }

    public void addRow(BigDecimal value, List<BigDecimal> costs) {
        rows.add(new SimplePriceTableRow(value, costs));
    }

    @Override
    public List<BigDecimal> getHeader() {
        return headers;
    }

    @Override
    public List<PriceTableRow> getRows() {
        return Collections.unmodifiableList(rows);
    }

    private static class SimplePriceTableRow implements PriceTableRow {

        private final BigDecimal value;
        private final List<BigDecimal> costs;

        public SimplePriceTableRow(BigDecimal value, List<BigDecimal> costs) {
            Objects.requireNonNull(value, "value must not be null");
            Objects.requireNonNull(costs, "costs must not be null");
            this.value = value;
            this.costs = costs;
        }

        @Override
        public BigDecimal getValue() {
            return value;
        }

        @Override
        public List<BigDecimal> getCosts() {
            return Collections.unmodifiableList(costs);
        }
    }
}
