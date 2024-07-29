package com.softwarearchetypes.pricing.formula;


import com.softwarearchetypes.pricing.shared.AbstractBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Clock;


@Entity
@Table(name = "formula_pricing")
class FormulaPricingEntity extends AbstractBaseEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "function_logic")
    private String formula;

    @Column(name = "input_data_type")
    private String inputDataClass;

    @Column(name = "input_data_json")
    private String inputDataJson;

    FormulaPricingEntity(
            String name,
            String formula,
            Class<?> inputDataClass,
            String inputDataJson,
            Clock clock) {

        super(clock);
        this.name = name;
        this.formula = formula;
        this.inputDataClass = ClassConverter.convertToDatabaseColumn(inputDataClass);
        this.inputDataJson = inputDataJson;
    }

    protected FormulaPricingEntity() {
        super();
    }

}
