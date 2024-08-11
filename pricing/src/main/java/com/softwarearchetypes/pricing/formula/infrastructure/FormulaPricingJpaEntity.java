package com.softwarearchetypes.pricing.formula.infrastructure;

import com.softwarearchetypes.pricing.common.AbstractBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "formula_pricing")
class FormulaPricingJpaEntity extends AbstractBaseEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "function_logic")
    private String formula;

    @Convert(converter = ClassConverter.class)
    @Column(name = "input_data_type")
    private Class<?> inputDataClass;

    @Column(name = "input_data_json")
    private String inputDataJson;

    protected FormulaPricingJpaEntity() {
        super(OffsetDateTime.now());
    }

    public FormulaPricingJpaEntity(
            OffsetDateTime creationDate,
            String name,
            String formula,
            Class<?> inputDataClass,
            String inputDataJson) {

        super(creationDate);
        this.name = name;
        this.formula = formula;
        this.inputDataClass = inputDataClass;
        this.inputDataJson = inputDataJson;
    }

    public String getName() {
        return name;
    }

    public String getFormula() {
        return formula;
    }

    public Class<?> getInputDataClass() {
        return inputDataClass;
    }

    public String getInputDataJson() {
        return inputDataJson;
    }
}

