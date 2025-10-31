package com.softwarearchetypes.pricing;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class PricingConfiguration {

    public static PricingFacade pricingFacade() {
        InMemoryCalculatorsRepository repository = new InMemoryCalculatorsRepository();
        PricingFacade facade = new PricingFacade(repository);
        facade.addCalculator("simple-fixed-20", CalculatorType.SIMPLE_FIXED, new Parameters(Map.of("amount", BigDecimal.valueOf(20))));
        facade.addCalculator("simple-interest-6", CalculatorType.SIMPLE_INTEREST, new Parameters(Map.of("annualRate", BigDecimal.valueOf(6))));
        return facade;
    }
}

interface CalculatorRepository {
    void save(Calculator calculator);
    Optional<Calculator> findByName(String name);
    Collection<Calculator> findAll();
}

class InMemoryCalculatorsRepository implements CalculatorRepository {
    private final Set<Calculator> calculators = new HashSet<>();

    @Override
    public void save(Calculator calculator) {
        calculators.add(calculator);
    }

    @Override
    public Optional<Calculator> findByName(String name) {
        return calculators
                .stream()
                .filter(c -> c.name().equals(name))
                .findFirst();
    }

    @Override
    public Collection<Calculator> findAll() {
        return new HashSet<>(calculators);
    }
}