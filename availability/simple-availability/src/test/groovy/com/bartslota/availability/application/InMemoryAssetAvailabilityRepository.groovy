package com.bartslota.availability.application

import com.bartslota.availability.domain.AssetAvailability
import com.bartslota.availability.domain.AssetAvailabilityRepository
import com.bartslota.availability.domain.AssetId

import java.util.stream.Stream

class InMemoryAssetAvailabilityRepository implements AssetAvailabilityRepository {

    Map<AssetId, AssetAvailability> content = [:]

    @Override
    void save(AssetAvailability assetAvailability) {
        content.put(assetAvailability.id(), assetAvailability)
    }

    @Override
    Optional<AssetAvailability> findBy(AssetId assetId) {
        Optional.ofNullable(content[assetId])
    }

    @Override
    Stream<AssetAvailability> findOverdue() {
        null
    }
}
