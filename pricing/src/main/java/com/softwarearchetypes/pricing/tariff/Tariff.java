package com.softwarearchetypes.pricing.tariff;

import java.util.*;

public class Tariff<R, C extends Comparable<C>, V> {

    private final C[] ranges;
    private final Map<TariffKey<R, C>, V> tariffMap;


    public Tariff(C[] ranges) {
        Objects.requireNonNull(ranges, "ranges must not be null");

        if (ranges.length == 0) {
            throw new IllegalArgumentException("ranges must not be empty");
        }

        this.ranges = ranges.clone();
        Arrays.sort(this.ranges);

        this.tariffMap = new HashMap<>();

    }

    public void addRow(R rowKey, List<V> values) {
        Objects.requireNonNull(values, "values must not be null");
        if (values.size() > this.ranges.length) {
            throw new IllegalArgumentException("values must not exceed " + this.ranges.length + " elements");
        }

        Objects.requireNonNull(rowKey, "rowKey must not be null");

        for (int i = 0; i < ranges.length; i++) {
            var tariffKey = new TariffKey<>(rowKey, this.ranges[i]);
            tariffMap.put(tariffKey, i < values.size() ? values.get(i) : null);
        }
    }

    public Optional<V> getValue(R rowKey, C columnKey) {
        if (rowKey == null) {
            throw new IllegalArgumentException("rowKey must not be null");
        }

        if (columnKey.compareTo(ranges[ranges.length - 1]) > 0) {
            throw new IllegalArgumentException("columnKey must not be greater than ranges.length");
        }

        var column = getColumnInRange(columnKey);
        var tariffKey = new TariffKey<>(rowKey, column);
        var tariffValue = tariffMap.get(tariffKey);

        return Optional.ofNullable(tariffValue);
    }

    private C getColumnInRange(C columnKey) {
        C column;

        if (columnKey.compareTo(ranges[0]) < 0) {
            return ranges[0];
        } else {
            var result = Arrays.binarySearch(ranges, columnKey);
            column = result >= 0 ? ranges[result] : ranges[(result + 1) * -1];
        }

        return column;
    }


    private record TariffKey<R, C>(
            R row,
            C column
    ) {
    }
}
