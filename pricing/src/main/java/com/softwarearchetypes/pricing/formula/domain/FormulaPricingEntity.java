package com.softwarearchetypes.pricing.formula.domain;

import java.time.OffsetDateTime;

public record FormulaPricingEntity(
        String name,
        String formula,
        Class<?> inputDataClass,
        String inputDataJson,
        OffsetDateTime creationDate
) {
}
