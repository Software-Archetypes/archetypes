package com.softwarearchetypes.party.events;

import java.util.Set;

public record CompanyRegistered(String partyId, String organizationName, Set<String> registeredIdentifiers, Set<String> roles) implements PartyRegistered {

}
