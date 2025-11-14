package com.softwarearchetypes.party.events;

import java.util.Set;

public record WebAddressDefined(String addressId,
                                 String partyId,
                                 String url,
                                 Set<String> useTypes) implements AddressDefinitionSucceeded, PublishedEvent {

}
