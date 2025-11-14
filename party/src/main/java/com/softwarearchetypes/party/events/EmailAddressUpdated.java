package com.softwarearchetypes.party.events;

import java.util.Set;

public record EmailAddressUpdated(String addressId,
                                   String partyId,
                                   String email,
                                   Set<String> useTypes) implements AddressUpdateSucceeded, PublishedEvent {

}
