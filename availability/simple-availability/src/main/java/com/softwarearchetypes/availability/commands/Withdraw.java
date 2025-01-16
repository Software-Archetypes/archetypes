package com.softwarearchetypes.availability.commands;

public record Withdraw(String assetId) implements Command {

    static final String TYPE = "WITHDRAW";

    @Override
    public String getType() {
        return TYPE;
    }
}
