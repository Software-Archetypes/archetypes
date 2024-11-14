package com.softwarearchetypes.events;

import java.util.Objects;

public final class RoleAdditionSkipped implements RoleAdditionSucceeded {

    private static final String DUPLICATION_REASON = "DUPLICATION";
    private final String name;
    private final String reason;

    private RoleAdditionSkipped(String name, String reason) {
        this.name = name;
        this.reason = reason;
    }

    public static RoleAdditionSkipped dueToDuplicationFor(String name) {
        return new RoleAdditionSkipped(name, DUPLICATION_REASON);
    }

    String name() {
        return name;
    }

    String reason() {
        return reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RoleAdditionSkipped that)) {
            return false;
        }
        return Objects.equals(name, that.name) && Objects.equals(reason, that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, reason);
    }
}
