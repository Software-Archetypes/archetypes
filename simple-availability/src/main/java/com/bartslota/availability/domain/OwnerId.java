package com.bartslota.availability.domain;

import java.util.Objects;

public class OwnerId {

    private final String value;

    private OwnerId(String value) {
        this.value = value;
    }

    public static OwnerId of(String value) {
        return new OwnerId(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OwnerId)) {
            return false;
        }
        OwnerId ownerId = (OwnerId) o;
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
