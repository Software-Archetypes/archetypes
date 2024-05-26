package com.bartslota.availability.commands;

public record Register(String assetId) implements Command {

    static final String TYPE = "REGISTER";

    @Override
    public String getType() {
        return TYPE;
    }
}
