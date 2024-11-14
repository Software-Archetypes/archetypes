package com.softwarearchetypes.events;

import java.util.Objects;

public final class RegisteredIdentifierAdditionSkipped implements RegisteredIdentifierAdditionSucceeded {

    private static final String DUPLICATION_REASON = "DUPLICATION";

    private final String type;
    private final String value;
    private final String reason;

    private RegisteredIdentifierAdditionSkipped(String type, String value, String reason) {
        this.type = type;
        this.value = value;
        this.reason = reason;
    }

    public static RegisteredIdentifierAdditionSkipped dueToDataDuplicationFor(String type, String value) {
        return new RegisteredIdentifierAdditionSkipped(type, value, DUPLICATION_REASON);
    }

    String type() {
        return type;
    }

    String value() {
        return value;
    }

    String reason() {
        return reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RegisteredIdentifierAdditionSkipped that)) {
            return false;
        }
        return Objects.equals(type, that.type) && Objects.equals(value, that.value) && Objects.equals(reason, that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value, reason);
    }
}
