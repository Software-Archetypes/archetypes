package com.softwarearchetypes.party.commands;

import com.softwarearchetypes.party.RegisteredIdentifier;
import java.util.Set;

public record RegisterPersonCommand(
        String firstName,
        String lastName,
        Set<String> roles,
        Set<RegisteredIdentifier> registeredIdentifiers) {
}
