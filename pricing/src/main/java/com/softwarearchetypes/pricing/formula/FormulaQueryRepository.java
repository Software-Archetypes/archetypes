package com.softwarearchetypes.pricing.formula;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface FormulaQueryRepository extends Repository<FormulaPricingEntity, UUID> {

    @Query("""
            select new com.softwarearchetypes.pricing.formula.BasicFormula(
            fp.formula,
            fp.inputDataClass
            )
            from FormulaPricingEntity fp
            where fp.id = :id
            """)
    FormulaPricing getFormulaById(UUID id);

}
