package com.bartslota.availability.events;

import java.time.Instant;
import java.util.UUID;

import com.bartslota.availability.domain.AssetId;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class AssetRegistered extends BaseDomainEvent {

    @JsonIgnore
    static final String TYPE = "AssetRegistered";

    private final String assetId;

    private AssetRegistered(String assetId) {
        super();
        this.assetId = assetId;
    }

    @JsonCreator
    private AssetRegistered(UUID id, Instant occurredAt, String assetId) {
        super(id, occurredAt);
        this.assetId = assetId;
    }

    public static AssetRegistered from(AssetId assetId) {
        return new AssetRegistered(assetId.asString());
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public String getAssetId() {
        return assetId;
    }
}
