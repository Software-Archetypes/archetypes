package com.bartslota.availability.events;

import java.time.Instant;
import java.util.UUID;

import com.bartslota.availability.domain.AssetId;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class AssetWithdrawalRejected extends BaseDomainEvent {

    @JsonIgnore
    static final String TYPE = "AssetWithdrawalRejected";

    private final String assetId;
    private final String reason;

    private AssetWithdrawalRejected(String assetId, String reason) {
        super();
        this.assetId = assetId;
        this.reason = reason;
    }

    @JsonCreator
    private AssetWithdrawalRejected(UUID id, Instant occurredAt, String assetId, String reason) {
        super(id, occurredAt);
        this.assetId = assetId;
        this.reason = reason;
    }

    public static AssetWithdrawalRejected dueToMissingAssetWith(AssetId assetId) {
        return new AssetWithdrawalRejected(assetId.asString(), "ASSET_IS_MISSING");
    }

    public static AssetWithdrawalRejected from(AssetId assetId, String reason) {
        return new AssetWithdrawalRejected(assetId.asString(), reason);
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
