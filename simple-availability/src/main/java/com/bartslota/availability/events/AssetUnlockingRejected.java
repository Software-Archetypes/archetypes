package com.bartslota.availability.events;

import java.time.Instant;
import java.util.UUID;

import com.bartslota.availability.domain.AssetId;
import com.bartslota.availability.domain.OwnerId;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class AssetUnlockingRejected extends BaseDomainEvent {

    @JsonIgnore
    static final String TYPE = "AssetUnlockingRejected";

    private final String assetId;
    private final String ownerId;
    private final String reason;

    private AssetUnlockingRejected(String assetId, String ownerId, String reason) {
        super();
        this.assetId = assetId;
        this.ownerId = ownerId;
        this.reason = reason;
    }

    @JsonCreator
    private AssetUnlockingRejected(UUID id, Instant occurredAt, String assetId, String ownerId, String reason) {
        super(id, occurredAt);
        this.assetId = assetId;
        this.ownerId = ownerId;
        this.reason = reason;
    }

    public static AssetUnlockingRejected dueToMissingAssetWith(AssetId assetId, OwnerId ownerId) {
        return new AssetUnlockingRejected(assetId.asString(), ownerId.asString(), "ASSET_IS_MISSING");
    }

    public static AssetUnlockingRejected from(AssetId assetId, OwnerId ownerId, String reason) {
        return new AssetUnlockingRejected(assetId.asString(), ownerId.asString(), reason);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    String getAssetId() {
        return assetId;
    }

    String getOwnerId() {
        return ownerId;
    }

    String getReason() {
        return reason;
    }
}
