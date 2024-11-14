package com.softwarearchetypes.events;

public record RegisteredIdentifierAdded(String type, String value) implements RegisteredIdentifierAdditionSucceeded {
}
