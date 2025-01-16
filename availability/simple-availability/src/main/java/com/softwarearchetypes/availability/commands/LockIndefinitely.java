package com.softwarearchetypes.availability.commands;

public record LockIndefinitely(String assetId) implements Command {

    static final String TYPE = "LOCK_INDEFINITELY";

    @Override
    public String getType() {
        return TYPE;
    }
}
