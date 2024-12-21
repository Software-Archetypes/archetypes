package com.softwarearchetypes.pricing.domain;

import com.softwarearchetypes.pricing.common.Result;
import io.github.jamsesso.jsonlogic.JsonLogic;
import io.github.jamsesso.jsonlogic.JsonLogicException;
import org.javamoney.moneta.Money;
import org.springframework.util.StringUtils;

import javax.money.CurrencyUnit;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;

public class Pricing {

    private final String name; //For now name is optional
    private final String formula;
    private final CurrencyUnit currency;
    private final OffsetDateTime validFrom;
    private OffsetDateTime validTo;

    public Pricing(
            String name,
            String formula,
            CurrencyUnit currency,
            OffsetDateTime validFrom,
            OffsetDateTime validTo) {

        if (!StringUtils.hasText(formula)) {
            throw new IllegalArgumentException("Formula can not be empty");
        }

        if (!validTo.isAfter(validFrom)) {
            throw new IllegalArgumentException("Valid from can not be after valid to");
        }

        Objects.requireNonNull(currency, "Currency can not be null");
        Objects.requireNonNull(validFrom, "Valid from can not be null");


        this.name = name;
        this.formula = formula;
        this.currency = currency;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    public Result<PriceCalculationFailure, Optional<Price>> calculatePrice(RuleContext ruleContext) {

        try {
            return Result.success(Optional.ofNullable(new JsonLogic().apply(formula, ruleContext.getContextVariables()))
                    .map(functionResult -> Money.of(new BigDecimal(functionResult.toString()), currency))
                    .map(money -> new Price(money, validFrom, validTo))
            );

        } catch (JsonLogicException e) {
            return Result.failure(new PriceCalculationFailure(e));
        }
    }

    public void disableAtDate(OffsetDateTime disableDate) {
        if (!disableDate.isAfter(validFrom)) {
            throw new IllegalArgumentException("Disable date must be after valid from");
        }

        this.validTo = disableDate;
    }

    public boolean isActiveAt(OffsetDateTime checkedDate) {
        return checkedDate.isAfter(validFrom) && checkedDate.isBefore(validTo);
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public String getFormula() {
        return formula;
    }

    public CurrencyUnit getCurrency() {
        return currency;
    }

    public OffsetDateTime getValidFrom() {
        return validFrom;
    }

    public Optional<OffsetDateTime> getValidTo() {
        return Optional.ofNullable(validTo);
    }
}
