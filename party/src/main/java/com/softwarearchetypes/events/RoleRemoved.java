package com.softwarearchetypes.events;

public record RoleRemoved(String name) implements RoleRemovalSucceeded {
}
