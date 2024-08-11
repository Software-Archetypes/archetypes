package com.softwarearchetypes.pricing.formula.domain;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class FormulaJpaInMemoryRepository implements FormulaRepository {

    private final Map<UUID, FormulaPricingEntity> map = new ConcurrentHashMap<>();

    @Override
    public UUID save(FormulaPricingEntity formulaPricingEntity) {
        var uuid = UUID.randomUUID();
        map.put(uuid, formulaPricingEntity);
        return uuid;
    }

    @Override
    public Optional<FormulaPricingEntity> findById(UUID id) {
        return Optional.ofNullable(map.get(id));
    }
}
