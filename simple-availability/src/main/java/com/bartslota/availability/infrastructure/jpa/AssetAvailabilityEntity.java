package com.bartslota.availability.infrastructure.jpa;

import java.time.LocalDateTime;

import com.bartslota.availability.domain.AssetAvailability;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

@Entity
@Table(name = "asset_availability", schema = "availability")
class AssetAvailabilityEntity {

    @Column
    @Id
    String assetId;

    @Column
    String lock;

    protected AssetAvailabilityEntity() {
    }

    AssetAvailabilityEntity(String assetId, String lock) {
        this.assetId = assetId;
        this.lock = lock;
    }

    String getAssetId() {
        return assetId;
    }

    String getLock() {
        return lock;
    }

    void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    void setLock(String lock) {
        this.lock = lock;
    }

    @JsonTypeInfo(property = "type", use = NAME)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = WithdrawalLockRepresentation.class, name = WithdrawalLockRepresentation.TYPE),
            @JsonSubTypes.Type(value = MaintenanceLockRepresentation.class, name = MaintenanceLockRepresentation.TYPE),
            @JsonSubTypes.Type(value = OwnerLockRepresentation.class, name = OwnerLockRepresentation.TYPE)
    })
    sealed interface LockRepresentation
            permits WithdrawalLockRepresentation, MaintenanceLockRepresentation, OwnerLockRepresentation {

        static LockRepresentation from(AssetAvailability.Lock lock) {
            return switch (lock) {
                case AssetAvailability.WithdrawalLock l -> WithdrawalLockRepresentation.from(l);
                case AssetAvailability.MaintenanceLock l -> MaintenanceLockRepresentation.from(l);
                case AssetAvailability.OwnerLock l -> OwnerLockRepresentation.from(l);
            };
        }
    }

    final static record WithdrawalLockRepresentation(String ownerId) implements LockRepresentation {

        static final String TYPE = "WITHDRAWAL_LOCK";

        static WithdrawalLockRepresentation from(AssetAvailability.WithdrawalLock lock) {
            return new WithdrawalLockRepresentation(lock.ownerId().asString());
        }
    }

    final static record MaintenanceLockRepresentation(String ownerId) implements LockRepresentation {

        static final String TYPE = "MAINTENANCE_LOCK";

        static MaintenanceLockRepresentation from(AssetAvailability.MaintenanceLock lock) {
            return new MaintenanceLockRepresentation(lock.ownerId().asString());
        }
    }

    final static record OwnerLockRepresentation(String ownerId, LocalDateTime until) implements LockRepresentation {

        static final String TYPE = "OWNER_LOCK";

        static OwnerLockRepresentation from(AssetAvailability.OwnerLock lock) {
            return new OwnerLockRepresentation(lock.ownerId().asString(), lock.getUntil());
        }
    }
}
