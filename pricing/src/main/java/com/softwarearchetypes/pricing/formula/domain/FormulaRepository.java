package com.softwarearchetypes.pricing.formula.domain;

import java.util.Optional;
import java.util.UUID;

public interface FormulaRepository {

    UUID save(FormulaPricingEntity formulaPricingEntity);

    Optional<FormulaPricingEntity> findById(UUID id);
}
