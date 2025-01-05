package com.softwarearchetypes.party.events;

import java.util.Set;

public record GeoAddressDefined(String addressId,
                                String partyId,
                                String name,
                                String street,
                                String building,
                                String flat,
                                String city,
                                String zip,
                                String locale,
                                Set<String> useTypes) implements AddressDefinitionSucceeded, PublishedEvent {

}
