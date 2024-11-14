package com.softwarearchetypes.events;

public record RegisteredIdentifierRemoved(String type, String value) implements RegisteredIdentifierRemovalSucceeded {
}
