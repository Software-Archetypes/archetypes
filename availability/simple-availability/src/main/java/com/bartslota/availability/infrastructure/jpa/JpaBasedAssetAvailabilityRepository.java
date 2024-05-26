package com.bartslota.availability.infrastructure.jpa;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bartslota.availability.domain.AssetAvailability;
import com.bartslota.availability.domain.AssetAvailabilityRepository;
import com.bartslota.availability.domain.AssetId;
import com.bartslota.availability.domain.OwnerId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
class JpaBasedAssetAvailabilityRepository implements AssetAvailabilityRepository {

    private final JpaAssetAvailabilityRepository jpaRepository;
    private final ObjectMapper objectMapper;

    JpaBasedAssetAvailabilityRepository(JpaAssetAvailabilityRepository jpaRepository, ObjectMapper objectMapper) {
        this.jpaRepository = jpaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(AssetAvailability assetAvailability) {
        jpaRepository.save(entityFrom(assetAvailability));
    }

    @Override
    public Optional<AssetAvailability> findBy(AssetId assetId) {
        return jpaRepository
                .findById(assetId.asString())
                .map(entity -> AssetAvailability
                        .of(AssetId.of(entity.assetId))
                        .with(deserialize(entity.getLock())));

    }

    @Override
    public Stream<AssetAvailability> findOverdue() {
        return null;
    }

    private AssetAvailabilityEntity entityFrom(AssetAvailability assetAvailability) {
        return new AssetAvailabilityEntity(idFrom(assetAvailability), lockFrom(assetAvailability));
    }

    private String idFrom(AssetAvailability assetAvailability) {
        return assetAvailability.id().asString();
    }

    private String lockFrom(AssetAvailability assetAvailability) {
        return assetAvailability
                .currentLock()
                .map(AssetAvailabilityEntity.LockRepresentation::from)
                .map(serialize())
                .orElse(null);
    }

    private Function<AssetAvailabilityEntity.LockRepresentation, String> serialize() {
        return value -> {
            try {
                return objectMapper.writeValueAsString(value);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException(e);
            }
        };
    }

    private AssetAvailability.Lock deserialize(String value) {
        if (value == null) return null;
        try {
            return switch (objectMapper.readValue(value, AssetAvailabilityEntity.LockRepresentation.class)) {
                case AssetAvailabilityEntity.WithdrawalLockRepresentation ignored -> new AssetAvailability.WithdrawalLock();
                case AssetAvailabilityEntity.MaintenanceLockRepresentation ignored -> new AssetAvailability.MaintenanceLock();
                case AssetAvailabilityEntity.OwnerLockRepresentation l -> new AssetAvailability.OwnerLock(OwnerId.of(l.ownerId()), l.until());
            };
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}

interface JpaAssetAvailabilityRepository extends JpaRepository<AssetAvailabilityEntity, String> {

}
