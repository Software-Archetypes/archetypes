package com.softwarearchetypes.party.events;

import java.util.Set;

public record PhoneAddressDefined(String addressId,
                                   String partyId,
                                   String phoneNumber,
                                   Set<String> useTypes) implements AddressDefinitionSucceeded, PublishedEvent {

}
