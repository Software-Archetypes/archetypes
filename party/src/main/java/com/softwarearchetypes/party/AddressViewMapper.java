package com.softwarearchetypes.party;

import java.util.stream.Collectors;

class AddressViewMapper {

    static AddressView toView(Address address) {
        if (address instanceof GeoAddress geoAddress && address.addressDetails() instanceof GeoAddress.GeoAddressDetails details) {
            return new GeoAddressView(
                    geoAddress.id(),
                    geoAddress.partyId(),
                    details.name(),
                    details.street(),
                    details.building(),
                    details.flat(),
                    details.city(),
                    details.zip().asString(),
                    details.locale(),
                    geoAddress.useTypes().stream().map(Enum::name).collect(Collectors.toSet()),
                    geoAddress.validity()
            );
        }
        if (address instanceof EmailAddress emailAddress && address.addressDetails() instanceof EmailAddressDetails details) {
            return new EmailAddressView(
                    emailAddress.id(),
                    emailAddress.partyId(),
                    details.email(),
                    emailAddress.useTypes().stream().map(Enum::name).collect(Collectors.toSet()),
                    emailAddress.validity()
            );
        }
        if (address instanceof PhoneAddress phoneAddress && address.addressDetails() instanceof PhoneAddressDetails details) {
            return new PhoneAddressView(
                    phoneAddress.id(),
                    phoneAddress.partyId(),
                    details.phoneNumber(),
                    phoneAddress.useTypes().stream().map(Enum::name).collect(Collectors.toSet()),
                    phoneAddress.validity()
            );
        }
        if (address instanceof WebAddress webAddress && address.addressDetails() instanceof WebAddressDetails details) {
            return new WebAddressView(
                    webAddress.id(),
                    webAddress.partyId(),
                    details.url(),
                    webAddress.useTypes().stream().map(Enum::name).collect(Collectors.toSet()),
                    webAddress.validity()
            );
        }
        throw new IllegalArgumentException("Unsupported address type: " + address.getClass());
    }
}
