package com.softwarearchetypes.pricing.domain;

import org.javamoney.moneta.Money;

import javax.money.CurrencyUnit;
import java.time.OffsetDateTime;
import java.util.UUID;

public class PricingService {

    private final PricingRepository repository;

    public PricingService(PricingRepository repository) {
        this.repository = repository;
    }


    public UUID createFixedPricing(String name, Money fixedPrice, OffsetDateTime validFrom, OffsetDateTime validTo) {
        //We use jsonLogic log operation to simulate constant value
        var formula = "{\"log\":\"" + fixedPrice.getNumberStripped() + "\"}";

        var pricing = new Pricing(
                name,
                formula,
                fixedPrice.getCurrency(),
                validFrom,
                validTo
        );

        return repository.save(pricing);
    }

    public UUID createFormulaBasedPricing(String name, String formula, CurrencyUnit currency, OffsetDateTime validFrom, OffsetDateTime validTo) {
        var pricing = new Pricing(
                name,
                formula,
                currency,
                validFrom,
                validTo
        );

        return repository.save(pricing);
    }

//    public UUID createPriceTableBasedPricing(String name, PriceTable table, CurrencyUnit currency, OffsetDateTime validFrom, OffsetDateTime validTo) {
//
//    }


}
