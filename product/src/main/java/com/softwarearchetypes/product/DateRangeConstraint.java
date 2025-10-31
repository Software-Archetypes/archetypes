package com.softwarearchetypes.product;

import java.time.LocalDate;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

/**
 * Restricts date values to a date range [from, to].
 * Example: expiry date between 2024-01-01 and 2024-12-31
 *
 * Persistence config example: {"from": "2024-01-01", "to": "2024-12-31"}
 */
class DateRangeConstraint implements FeatureValueConstraint {

    private final LocalDate from;
    private final LocalDate to;

    DateRangeConstraint(LocalDate from, LocalDate to) {
        checkArgument(from != null, "from date must be defined");
        checkArgument(to != null, "to date must be defined");
        checkArgument(!from.isAfter(to), "from must be before or equal to to");
        this.from = from;
        this.to = to;
    }

    static DateRangeConstraint between(String from, String to) {
        return new DateRangeConstraint(LocalDate.parse(from), LocalDate.parse(to));
    }

    @Override
    public FeatureValueType valueType() {
        return FeatureValueType.DATE;
    }

    @Override
    public String type() {
        return "DATE_RANGE";
    }

    @Override
    public boolean isValid(Object value) {
        if (!(value instanceof LocalDate)) {
            return false;
        }
        LocalDate dateValue = (LocalDate) value;
        return !dateValue.isBefore(from) && !dateValue.isAfter(to);
    }

    @Override
    public String desc() {
        return "date between %s and %s".formatted(from, to);
    }

    LocalDate from() {
        return from;
    }

    LocalDate to() {
        return to;
    }
}
