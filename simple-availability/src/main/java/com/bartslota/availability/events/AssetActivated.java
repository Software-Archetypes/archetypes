package com.bartslota.availability.events;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.bartslota.availability.domain.AssetId;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class AssetActivated extends BaseDomainEvent {

    @JsonIgnore
    static final String TYPE = "AssetActivated";

    private final String assetId;

    private AssetActivated(String assetId) {
        super();
        this.assetId = assetId;
    }

    @JsonCreator
    private AssetActivated(UUID id, Instant occurredAt, String assetId) {
        super(id, occurredAt);
        this.assetId = assetId;
    }

    public static AssetActivated from(AssetId assetId) {
        return new AssetActivated(assetId.asString());
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public String getAssetId() {
        return assetId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AssetActivated that)) {
            return false;
        }
        return Objects.equals(assetId, that.assetId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetId);
    }
}
