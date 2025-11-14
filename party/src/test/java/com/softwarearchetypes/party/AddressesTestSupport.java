package com.softwarearchetypes.party;

import com.softwarearchetypes.party.commands.AddOrUpdateGeoAddressCommand;
import com.softwarearchetypes.party.commands.GeoAddressDTO;

import java.util.stream.Collectors;

class AddressesTestSupport {

    private final AddressesFacade facade;

    AddressesTestSupport(AddressesFacade facade) {
        this.facade = facade;
    }

    void thereIsAddressOf(Address address) {
        if (address instanceof GeoAddress geoAddress && address.addressDetails() instanceof GeoAddress.GeoAddressDetails details) {
            GeoAddressDTO dto = new GeoAddressDTO(
                    geoAddress.id(),
                    geoAddress.partyId(),
                    details.name(),
                    details.street(),
                    details.building(),
                    details.flat(),
                    details.city(),
                    details.zip().asString(),
                    details.locale(),
                    geoAddress.useTypes().stream().map(Enum::name).collect(Collectors.toSet())
            );
            AddOrUpdateGeoAddressCommand command = new AddOrUpdateGeoAddressCommand(geoAddress.partyId(), dto);
            facade.handle(command);
        }
    }
}
