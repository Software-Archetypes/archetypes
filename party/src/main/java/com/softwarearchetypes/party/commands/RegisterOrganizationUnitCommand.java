package com.softwarearchetypes.party.commands;

import com.softwarearchetypes.party.RegisteredIdentifier;
import java.util.Set;

public record RegisterOrganizationUnitCommand(
        String organizationName,
        Set<String> roles,
        Set<RegisteredIdentifier> registeredIdentifiers) {
}
