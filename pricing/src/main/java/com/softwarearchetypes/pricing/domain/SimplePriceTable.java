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

    private record SimplePriceTableRow(BigDecimal value, List<BigDecimal> costs) implements PriceTableRow {

        private SimplePriceTableRow {
            Objects.requireNonNull(value, "value must not be null");
            Objects.requireNonNull(costs, "costs must not be null");
        }

        @Override
        public List<BigDecimal> costs() {
            return Collections.unmodifiableList(costs);
        }
    }
}
