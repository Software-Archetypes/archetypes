package com.bartslota.availability.events;

import java.time.Instant;
import java.util.UUID;

import com.bartslota.availability.domain.AssetId;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class AssetRegistrationRejected extends BaseDomainEvent {

    @JsonIgnore
    static final String TYPE = "AssetRegistrationRejected";

    private final String assetId;
    private final String reason;

    private AssetRegistrationRejected(String assetId, String reason) {
        super();
        this.assetId = assetId;
        this.reason = reason;
    }

    @JsonCreator
    private AssetRegistrationRejected(UUID id, Instant occurredAt, String assetId, String reason) {
        super(id, occurredAt);
        this.assetId = assetId;
        this.reason = reason;
    }

    public static AssetRegistrationRejected dueToAlreadyExistingAssetWith(AssetId assetId) {
        return new AssetRegistrationRejected(assetId.asString(), "ASSET_ALREADY_EXISTS");
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
