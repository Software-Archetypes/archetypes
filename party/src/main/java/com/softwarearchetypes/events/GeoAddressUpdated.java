package com.softwarearchetypes.events;

import java.util.Locale;
import java.util.Set;

import com.softwarearchetypes.party.ZipCode;

public record GeoAddressUpdated(String addressId,
                                String partyId,
                                String name,
                                String companyName,
                                String street,
                                String building,
                                String flat,
                                String city,
                                String zip,
                                String locale,
                                Set<String> useTypes) implements AddressUpdateSucceeded {

}
