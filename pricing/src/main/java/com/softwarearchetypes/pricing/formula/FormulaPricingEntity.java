package com.softwarearchetypes.pricing.formula;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwarearchetypes.pricing.shared.AbstractBaseEntity;
import io.github.jamsesso.jsonlogic.JsonLogic;
import io.github.jamsesso.jsonlogic.JsonLogicException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Map;

import static org.springframework.util.StringUtils.hasText;


@Entity
@Table(name = "formula_pricing")
class FormulaPricingEntity extends AbstractBaseEntity implements FormulaPricing {


    @Column(name = "function_logic")
    private final String functionLogic;

    @Column(name = "input_data_type")
    private final Class<?> inputDataClass;

    FormulaPricingEntity(
            String functionLogic,
            Class<?> inputDataClass,
            Clock clock) {

        super(clock);

        if (!hasText(functionLogic)) {
            throw new IllegalArgumentException("Function logic cannot be empty");
        }

        this.functionLogic = functionLogic;
        this.inputDataClass = inputDataClass;
    }

    protected FormulaPricingEntity() {
        super();
        this.functionLogic = null;
        this.inputDataClass = null;
    }

    @Override
    public BigDecimal calculatePrice(Object data) {

        if (data.getClass() != inputDataClass) {
            throw new IllegalArgumentException("The data provided has a different structure than expected. Expected data structure: " + inputDataClass);
        }

        var objectMapper = new ObjectMapper();

        try {
            var dataJson = objectMapper.convertValue(data, Map.class);
            var functionResult = new JsonLogic().apply(functionLogic, dataJson);

            return new BigDecimal(functionResult.toString());

        } catch (JsonLogicException e) {
            throw new IllegalArgumentException("Cannot execute formula", e);
        }
    }


}
