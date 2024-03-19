package com.bartslota.availability.domain;

import java.util.Optional;
import java.util.stream.Stream;

public interface AssetAvailabilityRepository {

    void save(AssetAvailability assetAvailability);

    Optional<AssetAvailability> findBy(AssetId assetId);

    Stream<AssetAvailability> findOverdue();
}
