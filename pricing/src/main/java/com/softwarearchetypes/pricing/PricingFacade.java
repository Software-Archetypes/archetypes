package com.softwarearchetypes.pricing;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.softwarearchetypes.quantity.money.Money;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;

public class PricingFacade {

    private final CalculatorRepository repository;

    PricingFacade(CalculatorRepository repository) {
        this.repository = repository;
    }

    public List<CalculatorView> availableCalculators() {
        return repository.findAll().stream().map(CalculatorView::from).toList();
    }

    public void addCalculator(String name, CalculatorType type, Parameters parameters) {
        Calculator calculator = createCalculator(name, type, parameters);
        repository.save(calculator);
    }

    public Money calculate(String calculatorName, Parameters parameters) {
        return repository
                .findByName(calculatorName)
                .map(c -> c.calculate(parameters))
                .orElseThrow(() -> new IllegalArgumentException("could not find calculator %s".formatted(calculatorName)));
    }

    public Map<CalculatorType, List<CalculatorView>> listCalculatorsWithDescriptions() {
        return repository
                .findAll()
                .stream()
                .collect(groupingBy(Calculator::getType, mapping(CalculatorView::from, Collectors.toList())));
    }

    public List<CalculatorType> availableCalculatorTypes() {
        return Arrays.asList(CalculatorType.values());
    }

    private Calculator createCalculator(String name, CalculatorType type, Parameters parameters) {
        if (!parameters.containsAll(type.requiredCreationFields())) {
            throw new IllegalArgumentException("Calculator %s requiredPreviousKeys field %s, but passed only %s".formatted(type, type.requiredCreationFields(), parameters.keys()));
        }
        return switch (type) {
            case SIMPLE_FIXED -> new SimpleFixedCalculator(name, parameters.getBigDecimal("amount"));
            case SIMPLE_INTEREST -> new SimpleInterestCalculator(name, parameters.getBigDecimal("annualRate"));
        };
    }
}