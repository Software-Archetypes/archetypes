package com.bartslota.availability.domain;

import java.util.Objects;

public class AssetId {

    private final String value;

    private AssetId(String value) {
        this.value = value;
    }

    public static AssetId of(String value) {
        return new AssetId(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AssetId)) {
            return false;
        }
        AssetId ownerId = (AssetId) o;
        return Objects.equals(value, ownerId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public String asString() {
        return value;
    }
}
