package com.softwarearchetypes.events;

import java.util.Objects;

public final class RegisteredIdentifierRemovalSkipped implements RegisteredIdentifierRemovalSucceeded {

    private static final String MISSING_IDENTIFIER_REASON = "MISSING_IDENTIFIER";

    private final String type;
    private final String value;
    private final String reason;

    private RegisteredIdentifierRemovalSkipped(String type, String value, String reason) {
        this.type = type;
        this.value = value;
        this.reason = reason;
    }

    public static RegisteredIdentifierRemovalSkipped dueToMissingIdentifierFor(String type, String value) {
        return new RegisteredIdentifierRemovalSkipped(type, value, MISSING_IDENTIFIER_REASON);
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
        if (!(o instanceof RegisteredIdentifierRemovalSkipped that)) {
            return false;
        }
        return Objects.equals(type, that.type) && Objects.equals(value, that.value) && Objects.equals(reason, that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value, reason);
    }
}
