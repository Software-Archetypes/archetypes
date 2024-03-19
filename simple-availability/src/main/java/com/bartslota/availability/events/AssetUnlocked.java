package com.bartslota.availability.events;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import com.bartslota.availability.domain.AssetId;
import com.bartslota.availability.domain.OwnerId;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class AssetUnlocked extends BaseDomainEvent {

    @JsonIgnore
    static final String TYPE = "AssetUnlocked";

    private final String assetId;
    private final String ownerId;
    private final LocalDateTime unlockedAt;

    private AssetUnlocked(String assetId, String ownerId, LocalDateTime unlockedAt) {
        super();
        this.assetId = assetId;
        this.ownerId = ownerId;
        this.unlockedAt = unlockedAt;
    }

    @JsonCreator
    private AssetUnlocked(UUID id, Instant occurredAt, String assetId, String ownerId, LocalDateTime unlockedAt) {
        super(id, occurredAt);
        this.assetId = assetId;
        this.ownerId = ownerId;
        this.unlockedAt = unlockedAt;
    }

    public static AssetUnlocked from(AssetId assetId, OwnerId ownerId, LocalDateTime from) {
        return new AssetUnlocked(assetId.asString(), ownerId.asString(), from);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public String getAssetId() {
        return assetId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public LocalDateTime getUnlockedAt() {
        return unlockedAt;
    }
}
