package com.softwarearchetypes.party.events;

import java.util.Set;

public record PhoneAddressUpdated(String addressId,
                                   String partyId,
                                   String phoneNumber,
                                   Set<String> useTypes) implements AddressUpdateSucceeded, PublishedEvent {

}
