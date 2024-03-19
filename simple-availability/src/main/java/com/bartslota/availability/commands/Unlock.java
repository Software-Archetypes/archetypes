package com.bartslota.availability.commands;

public record Unlock(String assetId) implements Command {

    static final String TYPE = "UNLOCK";

    @Override
    public String getType() {
        return TYPE;
    }
}
