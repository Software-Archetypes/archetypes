package com.softwarearchetypes.events;

public record RoleAdded(String name) implements RoleAdditionSucceeded {
}
