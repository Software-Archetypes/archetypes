package com.softwarearchetypes.party.events;

import java.util.Set;

public record WebAddressUpdated(String addressId,
                                 String partyId,
                                 String url,
                                 Set<String> useTypes) implements AddressUpdateSucceeded, PublishedEvent {

}
