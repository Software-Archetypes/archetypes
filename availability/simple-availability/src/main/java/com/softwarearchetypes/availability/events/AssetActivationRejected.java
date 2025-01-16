package com.softwarearchetypes.availability.events;

import java.time.Instant;
import java.util.UUID;

import com.softwarearchetypes.availability.domain.AssetId;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class AssetActivationRejected extends BaseDomainEvent {

    @JsonIgnore
    static final String TYPE = "AssetActivationRejected";

    private final String assetId;
    private final String reason;

    private AssetActivationRejected(String assetId, String reason) {
        super();
        this.assetId = assetId;
        this.reason = reason;
    }

    @JsonCreator
    private AssetActivationRejected(UUID id, Instant occurredAt, String assetId, String reason) {
        super(id, occurredAt);
        this.assetId = assetId;
        this.reason = reason;
    }

    public static AssetActivationRejected dueToMissingAssetWith(AssetId assetId) {
        return new AssetActivationRejected(assetId.asString(), "ASSET_IS_MISSING");
    }

    public static AssetActivationRejected from(AssetId assetId, String reason) {
        return new AssetActivationRejected(assetId.asString(), reason);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    String getAssetId() {
        return assetId;
    }

    String getReason() {
        return reason;
    }
}
