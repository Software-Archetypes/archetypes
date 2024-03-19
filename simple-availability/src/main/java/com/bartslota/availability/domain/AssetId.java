package com.bartslota.availability.domain;

public record AssetId(String value) {

    public static AssetId of(String value) {
        return new AssetId(value);
    }

    public String asString() {
        return value;
    }
}
