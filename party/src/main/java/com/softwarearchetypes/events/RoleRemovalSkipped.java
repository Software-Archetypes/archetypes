package com.softwarearchetypes.events;

import java.util.Objects;

public final class RoleRemovalSkipped implements RoleRemovalSucceeded {

    private static final String MISSING_ROLE_REASON = "MISSING_ROLE";
    private final String name;
    private final String reason;

    private RoleRemovalSkipped(String name, String reason) {
        this.name = name;
        this.reason = reason;
    }

    public static RoleRemovalSkipped dueToMissingRoleFor(String name) {
        return new RoleRemovalSkipped(name, MISSING_ROLE_REASON);
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
        if (!(o instanceof RoleRemovalSkipped that)) {
            return false;
        }
        return Objects.equals(name, that.name) && Objects.equals(reason, that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, reason);
    }
}
