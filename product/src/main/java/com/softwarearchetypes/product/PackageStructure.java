package com.softwarearchetypes.product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

/**
 * PackageStructure defines what products can be in a package and how they can be combined.
 * <p>
 * It consists of two parts:
 * 1. ProductSets - collections of available products (the "raw material")
 * 2. SelectionRules - rules defining valid selections (the "constraints")
 * <p>
 * Think of ProductSets as ingredients available in a kitchen, and SelectionRules
 * as the recipe rules: "use 1 meat", "add 2-3 vegetables", "if chicken then must add sauce".
 */
class PackageStructure {
    private final Map<String, ProductSet> productSets;
    private final List<SelectionRule> selectionRules;

    PackageStructure(Map<String, ProductSet> productSets,
                    List<SelectionRule> selectionRules) {
        checkArgument(productSets != null && !productSets.isEmpty(), "ProductSets must be defined");
        checkArgument(selectionRules != null && !selectionRules.isEmpty(), "Selection rules must be defined");
        this.productSets = Map.copyOf(productSets);
        this.selectionRules = List.copyOf(selectionRules);
    }

    Map<String, ProductSet> productSets() {
        return productSets;
    }

    List<SelectionRule> selectionRules() {
        return selectionRules;
    }

    /**
     * Validates if customer's product selection satisfies all package rules.
     */
    PackageValidationResult validate(List<SelectedProduct> selection) {
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < selectionRules.size(); i++) {
            SelectionRule rule = selectionRules.get(i);
            if (!rule.isSatisfiedBy(selection)) {
                errors.add("Rule %d not satisfied: %s".formatted(i + 1, rule));
            }
        }

        return errors.isEmpty()
            ? PackageValidationResult.success()
            : PackageValidationResult.failure(errors);
    }

    @Override
    public String toString() {
        return "PackageStructure{sets=%d, rules=%d}".formatted(productSets.size(), selectionRules.size());
    }
}
