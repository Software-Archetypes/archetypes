package com.softwarearchetypes.availability.events;

import java.time.Instant;
import java.util.UUID;

import com.softwarearchetypes.availability.domain.AssetId;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class AssetLockExpired extends BaseDomainEvent {

    @JsonIgnore
    static final String TYPE = "AssetLockExpired";

    private final String assetId;

    private AssetLockExpired(String assetId) {
        super();
        this.assetId = assetId;
    }

    @JsonCreator
    private AssetLockExpired(UUID id, Instant occurredAt, String assetId) {
        super(id, occurredAt);
        this.assetId = assetId;
    }

    public static AssetLockExpired from(AssetId assetId) {
        return new AssetLockExpired(assetId.asString());
    }

    @Override
    public String getType() {
        return TYPE;
    }

    String getAssetId() {
        return assetId;
    }
}
