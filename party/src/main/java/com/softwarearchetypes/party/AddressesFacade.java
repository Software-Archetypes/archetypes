package com.softwarearchetypes.party;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.party.commands.AddOrUpdateGeoAddressCommand;
import com.softwarearchetypes.party.commands.GeoAddressDTO;
import com.softwarearchetypes.party.commands.RemoveAddressCommand;
import com.softwarearchetypes.party.events.AddressDefinitionFailed;
import com.softwarearchetypes.party.events.AddressRemovalFailed;
import com.softwarearchetypes.party.events.EventPublisher;

import java.util.stream.Collectors;

public class AddressesFacade {

    private final AddressesRepository repository;
    private final EventPublisher publisher;

    AddressesFacade(AddressesRepository repository, EventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    //can be enhanced with check against missing party (here we accept addresses for any partyId)
    public Result<AddressDefinitionFailed, AddressId> handle(AddOrUpdateGeoAddressCommand command) {
        GeoAddressDTO dto = command.address();
        GeoAddress geoAddress = new GeoAddress(
                dto.addressId(),
                dto.partyId(),
                GeoAddress.GeoAddressDetails.from(
                        dto.name(),
                        dto.street(),
                        dto.building(),
                        dto.flat(),
                        dto.city(),
                        ZipCode.of(dto.zipCode()),
                        dto.locale()
                ),
                dto.useTypes().stream().map(AddressUseType::valueOf).collect(Collectors.toSet())
        );

        Addresses addresses = repository.findFor(command.partyId()).orElse(Addresses.emptyAddressesFor(command.partyId()));
        return addresses.addOrUpdate(geoAddress)
                        .peekSuccess(repository::save)
                        .peekSuccess(it -> publisher.publish(it.publishedEvents()))
                        .map(ignored -> geoAddress.id());
    }

    //can be enhanced with check against missing party (here we accept addresses for any partyId)
    public Result<AddressRemovalFailed, AddressId> handle(RemoveAddressCommand command) {
        Addresses addresses = repository.findFor(command.partyId()).orElse(Addresses.emptyAddressesFor(command.partyId()));
        return addresses.removeAddressWith(command.addressId())
                        .peekSuccess(repository::save)
                        .peekSuccess(it -> publisher.publish(it.publishedEvents()))
                        .map(ignored -> command.addressId());
    }

}
