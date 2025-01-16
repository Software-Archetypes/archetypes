package com.softwarearchetypes.availability.commands;

public record Activate(String assetId) implements Command {

    static final String TYPE = "ACTIVATE";

    @Override
    public String getType() {
        return TYPE;
    }
}
