package com.bartslota.availability.commands;

public record Lock(String assetId, Integer durationInMinutes) implements Command {

    static final String TYPE = "LOCK";

    @Override
    public String getType() {
        return TYPE;
    }
}
