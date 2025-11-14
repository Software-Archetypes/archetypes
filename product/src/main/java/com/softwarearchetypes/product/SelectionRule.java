package com.softwarearchetypes.product;

import java.util.Arrays;
import java.util.List;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

/**
 * SelectionRule defines constraints about how products can be selected in a package.
 * While ProductSet defines WHAT products are available, SelectionRule defines
 * HOW MANY can be selected (constraints).
 * <p>
 * Supports composition through AND, OR, and conditional (IF-THEN) logic.
 */
interface SelectionRule {

    boolean isSatisfiedBy(List<SelectedProduct> selection);

    // Basic constraint: select between min and max products from sourceSet
    static SelectionRule isSubsetOf(ProductSet sourceSet, int min, int max) {
        return new IsSubsetOf(sourceSet, min, max);
    }

    // Helper: select exactly one product from sourceSet (required choice)
    static SelectionRule single(ProductSet sourceSet) {
        return new IsSubsetOf(sourceSet, 1, 1);
    }

    // Helper: select zero or one product from sourceSet (optional choice)
    static SelectionRule optional(ProductSet sourceSet) {
        return new IsSubsetOf(sourceSet, 0, 1);
    }

    // Helper: select at least one product from sourceSet
    static SelectionRule required(ProductSet sourceSet) {
        return new IsSubsetOf(sourceSet, 1, Integer.MAX_VALUE);
    }

    // Composition: all rules must be satisfied (AND)
    static SelectionRule and(SelectionRule... rules) {
        return new AndRule(Arrays.asList(rules));
    }

    // Composition: at least one rule must be satisfied (OR)
    static SelectionRule or(SelectionRule... rules) {
        return new OrRule(Arrays.asList(rules));
    }

    // Conditional: if condition is true, then all thenRules must be satisfied
    static SelectionRule ifThen(SelectionRule condition,
            SelectionRule... thenRules) {
        return new ConditionalRule(condition, Arrays.asList(thenRules));
    }

    // Negation: rule must NOT be satisfied
    static SelectionRule not(SelectionRule rule) {
        return new NotRule(rule);
    }

    /**
     * Basic selection rule: customer selection must contain between min and max products from sourceSet.
     * <p>
     * Example: "Select 1 to 3 accessories" means min=1, max=3, sourceSet contains all available accessories.
     */
    record IsSubsetOf(ProductSet sourceSet, int min, int max) implements SelectionRule {

        public IsSubsetOf {
            checkArgument(sourceSet != null, "ProductSet must be defined");
            checkArgument(min >= 0, "Min must be >= 0");
            checkArgument(max >= min, "Max must be >= min");
        }

        @Override
        public boolean isSatisfiedBy(List<SelectedProduct> selection) {
            long count = selection.stream()
                                  .filter(s -> sourceSet.contains(s.productId()))
                                  .mapToInt(SelectedProduct::quantity)
                                  .sum();

            return count >= min && count <= max;
        }

        @Override
        public String toString() {
            return "IsSubsetOf{set='%s', min=%d, max=%d}".formatted(sourceSet.name(), min, max);
        }
    }

    /**
     * AND composition: all rules must be satisfied.
     * <p>
     * Example: Customer must select memory AND storage AND operating system.
     */
    record AndRule(List<SelectionRule> rules) implements SelectionRule {

        public AndRule {
            checkArgument(rules != null && !rules.isEmpty(), "Rules cannot be empty");
        }

        @Override
        public boolean isSatisfiedBy(List<SelectedProduct> selection) {
            return rules.stream()
                        .allMatch(r -> r.isSatisfiedBy(selection));
        }

        @Override
        public String toString() {
            return "AND(%d rules)".formatted(rules.size());
        }
    }

    /**
     * OR composition: at least one rule must be satisfied.
     * <p>
     * Example: Customer must select either Windows OR macOS OR Linux.
     */
    record OrRule(List<SelectionRule> rules) implements SelectionRule {

        public OrRule {
            checkArgument(rules != null && !rules.isEmpty(), "Rules cannot be empty");
        }

        @Override
        public boolean isSatisfiedBy(List<SelectedProduct> selection) {
            return rules.stream()
                        .anyMatch(r -> r.isSatisfiedBy(selection));
        }

        @Override
        public String toString() {
            return "OR(%d rules)".formatted(rules.size());
        }
    }

    /**
     * NOT composition: rule must NOT be satisfied.
     * <p>
     * Example: Customer must NOT select ExtraData add-on when they have unlimited plan.
     * <p>
     * Useful in conditional rules: IF unlimited plan THEN NOT extra data.
     */
    record NotRule(SelectionRule rule) implements SelectionRule {

        public NotRule {
            checkArgument(rule != null, "Rule must be defined");
        }

        @Override
        public boolean isSatisfiedBy(List<SelectedProduct> selection) {
            return !rule.isSatisfiedBy(selection);
        }

        @Override
        public String toString() {
            return "NOT(%s)".formatted(rule);
        }
    }

    /**
     * Conditional rule: if condition is satisfied, then all thenRules must be satisfied.
     * If condition is not satisfied, the rule passes automatically.
     * <p>
     * This allows modeling dependencies between product selections.
     * Example: "If customer selects gaming laptop, then they must also select dedicated graphics card."
     * <p>
     * Without conditionals, you would need separate packages for each combination.
     * With conditionals, you can have one flexible package where choices determine requirements.
     */
    record ConditionalRule(
            SelectionRule condition,
            List<SelectionRule> thenRules
    ) implements SelectionRule {

        public ConditionalRule {
            checkArgument(condition != null, "Condition must be defined");
            checkArgument(thenRules != null && !thenRules.isEmpty(),
                    "Then rules cannot be empty");
        }

        @Override
        public boolean isSatisfiedBy(List<SelectedProduct> selection) {
            if (condition.isSatisfiedBy(selection)) {
                // If condition is true, all then-rules must be satisfied
                return thenRules.stream()
                                .allMatch(r -> r.isSatisfiedBy(selection));
            }
            // If condition is false, rule passes automatically
            return true;
        }

        @Override
        public String toString() {
            return "IF(%s) THEN(%d rules)".formatted(condition, thenRules.size());
        }
    }
}
