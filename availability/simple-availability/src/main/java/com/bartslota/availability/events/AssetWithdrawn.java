package com.bartslota.availability.events;

import java.time.Instant;
import java.util.UUID;

import com.bartslota.availability.domain.AssetId;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class AssetWithdrawn extends BaseDomainEvent {

    @JsonIgnore
    static final String TYPE = "AssetWithdrawn";

    private final String assetId;

    private AssetWithdrawn(String assetId) {
        super();
        this.assetId = assetId;
    }

    @JsonCreator
    private AssetWithdrawn(UUID id, Instant occurredAt, String assetId) {
        super(id, occurredAt);
        this.assetId = assetId;
    }

    public static AssetWithdrawn from(AssetId assetId) {
        return new AssetWithdrawn(assetId.asString());
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public String getAssetId() {
        return assetId;
    }
}
