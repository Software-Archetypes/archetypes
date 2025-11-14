package com.softwarearchetypes.product;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Constraint that can be evaluated against ApplicabilityContext.
 * Supports full composition via AndConstraint, OrConstraint, NotConstraint.
 */
public sealed interface ApplicabilityConstraint permits
        EqualsConstraint,
        InConstraint,
        GreaterThanConstraint,
        LessThanConstraint,
        BetweenConstraint,
        AndConstraint,
        OrConstraint,
        NotConstraint,
        AlwaysTrueConstraint {

    boolean isSatisfiedBy(ApplicabilityContext context);

    // Factory methods for fluent API
    static ApplicabilityConstraint alwaysTrue() {
        return new AlwaysTrueConstraint();
    }

    static ApplicabilityConstraint equals(String parameterName, String expectedValue) {
        return new EqualsConstraint(parameterName, expectedValue);
    }

    static ApplicabilityConstraint in(String parameterName, Set<String> allowedValues) {
        return new InConstraint(parameterName, allowedValues);
    }

    static ApplicabilityConstraint in(String parameterName, String... allowedValues) {
        return new InConstraint(parameterName, Set.of(allowedValues));
    }

    static ApplicabilityConstraint greaterThan(String parameterName, int threshold) {
        return new GreaterThanConstraint(parameterName, threshold);
    }

    static ApplicabilityConstraint lessThan(String parameterName, int threshold) {
        return new LessThanConstraint(parameterName, threshold);
    }

    static ApplicabilityConstraint between(String parameterName, int min, int max) {
        return new BetweenConstraint(parameterName, min, max);
    }

    static ApplicabilityConstraint and(ApplicabilityConstraint... constraints) {
        return new AndConstraint(Arrays.asList(constraints));
    }

    static ApplicabilityConstraint or(ApplicabilityConstraint... constraints) {
        return new OrConstraint(Arrays.asList(constraints));
    }

    static ApplicabilityConstraint not(ApplicabilityConstraint constraint) {
        return new NotConstraint(constraint);
    }
}

record EqualsConstraint(String parameterName, String expectedValue) implements ApplicabilityConstraint {
    @Override
    public boolean isSatisfiedBy(ApplicabilityContext context) {
        return context.get(parameterName)
                .map(value -> value.equals(expectedValue))
                .orElse(false);
    }
}

record InConstraint(String parameterName, Set<String> allowedValues) implements ApplicabilityConstraint {
    @Override
    public boolean isSatisfiedBy(ApplicabilityContext context) {
        return context.get(parameterName)
                .map(allowedValues::contains)
                .orElse(false);
    }
}

record GreaterThanConstraint(String parameterName, int threshold) implements ApplicabilityConstraint {
    @Override
    public boolean isSatisfiedBy(ApplicabilityContext context) {
        return context.get(parameterName)
                .map(value -> {
                    try {
                        return Integer.parseInt(value) > threshold;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                })
                .orElse(false);
    }
}

record LessThanConstraint(String parameterName, int threshold) implements ApplicabilityConstraint {
    @Override
    public boolean isSatisfiedBy(ApplicabilityContext context) {
        return context.get(parameterName)
                .map(value -> {
                    try {
                        return Integer.parseInt(value) < threshold;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                })
                .orElse(false);
    }
}

record BetweenConstraint(String parameterName, int min, int max) implements ApplicabilityConstraint {
    @Override
    public boolean isSatisfiedBy(ApplicabilityContext context) {
        return context.get(parameterName)
                .map(value -> {
                    try {
                        int numValue = Integer.parseInt(value);
                        return numValue >= min && numValue <= max;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                })
                .orElse(false);
    }
}

record AndConstraint(List<ApplicabilityConstraint> constraints) implements ApplicabilityConstraint {
    @Override
    public boolean isSatisfiedBy(ApplicabilityContext context) {
        return constraints.stream().allMatch(c -> c.isSatisfiedBy(context));
    }
}

record OrConstraint(List<ApplicabilityConstraint> constraints) implements ApplicabilityConstraint {
    @Override
    public boolean isSatisfiedBy(ApplicabilityContext context) {
        return constraints.stream().anyMatch(c -> c.isSatisfiedBy(context));
    }
}

record NotConstraint(ApplicabilityConstraint constraint) implements ApplicabilityConstraint {
    @Override
    public boolean isSatisfiedBy(ApplicabilityContext context) {
        return !constraint.isSatisfiedBy(context);
    }
}

record AlwaysTrueConstraint() implements ApplicabilityConstraint {
    @Override
    public boolean isSatisfiedBy(ApplicabilityContext context) {
        return true;
    }
}
