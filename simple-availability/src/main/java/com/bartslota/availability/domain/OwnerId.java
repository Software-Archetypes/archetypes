package com.bartslota.availability.domain;

public record OwnerId(String value) {

    public static OwnerId of(String value) {
        return new OwnerId(value);
    }

    public String asString() {
        return value;
    }
}
