package com.softwarearchetypes.party.events;

import java.util.Set;

public record EmailAddressDefined(String addressId,
                                   String partyId,
                                   String email,
                                   Set<String> useTypes) implements AddressDefinitionSucceeded, PublishedEvent {

}
