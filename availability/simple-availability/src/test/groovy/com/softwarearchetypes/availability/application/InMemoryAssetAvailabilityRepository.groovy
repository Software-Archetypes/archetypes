package com.softwarearchetypes.availability.application

import com.softwarearchetypes.availability.domain.AssetAvailability
import com.softwarearchetypes.availability.domain.AssetAvailabilityRepository
import com.softwarearchetypes.availability.domain.AssetId

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
