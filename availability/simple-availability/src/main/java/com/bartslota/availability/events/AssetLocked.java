package com.bartslota.availability.events;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import com.bartslota.availability.domain.AssetId;
import com.bartslota.availability.domain.OwnerId;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class AssetLocked extends BaseDomainEvent {

    @JsonIgnore
    static final String TYPE = "AssetLocked";

    private final String assetId;
    private final String ownerId;
    private final LocalDateTime validUntil;

    private AssetLocked(String assetId, String ownerId, LocalDateTime from) {
        super();
        this.assetId = assetId;
        this.ownerId = ownerId;
        this.validUntil = from;
    }

    @JsonCreator
    private AssetLocked(UUID id, Instant occurredAt, String assetId, String ownerId, LocalDateTime validUntil) {
        super(id, occurredAt);
        this.assetId = assetId;
        this.ownerId = ownerId;
        this.validUntil = validUntil;
    }

    public static AssetLocked from(AssetId assetId, OwnerId ownerId, LocalDateTime from) {
        return new AssetLocked(assetId.asString(), ownerId.asString(), from);
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

    public LocalDateTime getValidUntil() {
        return validUntil;
    }
}
