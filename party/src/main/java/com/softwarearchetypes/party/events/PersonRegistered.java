package com.softwarearchetypes.party.events;

import java.util.Set;

public record PersonRegistered(String partyId, String firstName, String lastName, Set<String> registeredIdentifiers,
                               Set<String> roles) implements PartyRegistered {

}
