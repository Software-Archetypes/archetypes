package com.bartslota.availability.events;

import java.time.Instant;
import java.util.UUID;

import com.bartslota.availability.domain.AssetId;
import com.bartslota.availability.domain.OwnerId;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class AssetLockRejected extends BaseDomainEvent {

    @JsonIgnore
    static final String TYPE = "AssetLockRejected";

    private final String assetId;
    private final String ownerId;
    private final String reason;

    private AssetLockRejected(String assetId, String ownerId, String reason) {
        super();
        this.assetId = assetId;
        this.ownerId = ownerId;
        this.reason = reason;
    }

    AssetLockRejected(UUID id, Instant occurredAt, String assetId, String ownerId, String reason) {
        super(id, occurredAt);
        this.assetId = assetId;
        this.ownerId = ownerId;
        this.reason = reason;
    }

    public static AssetLockRejected dueToMissingAssetWith(AssetId assetId, OwnerId ownerId) {
        return new AssetLockRejected(assetId.asString(), ownerId.asString(), "ASSET_IS_MISSING");
    }

    public static AssetLockRejected from(AssetId assetId, OwnerId ownerId, String reason) {
        return new AssetLockRejected(assetId.asString(), ownerId.asString(), reason);
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
