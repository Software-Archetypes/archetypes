package com.softwarearchetypes.party;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.party.events.AddressDefinitionFailed;
import com.softwarearchetypes.party.events.AddressRemovalFailed;
import com.softwarearchetypes.party.events.EventPublisher;

public class AddressesFacade {

    private final AddressesRepository repository;
    private final EventPublisher publisher;

    AddressesFacade(AddressesRepository repository, EventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    //can be enhanced with check against missing party (here we accept addresses for any partyId)
    Result<AddressDefinitionFailed, AddressId> addOrUpdate(PartyId partyId, Address address) {
        Addresses addresses = repository.findFor(partyId).orElse(Addresses.emptyAddressesFor(partyId));
        return addresses.addOrUpdate(address)
                        .peekSuccess(repository::save)
                        .peekSuccess(it -> publisher.publish(it.publishedEvents()))
                        .map(ignored -> address.id());
    }

    //can be enhanced with check against missing party (here we accept addresses for any partyId)
    Result<AddressRemovalFailed, AddressId> remove(PartyId partyId, AddressId addressId) {
        Addresses addresses = repository.findFor(partyId).orElse(Addresses.emptyAddressesFor(partyId));
        return addresses.removeAddressWith(addressId)
                        .peekSuccess(repository::save)
                        .peekSuccess(it -> publisher.publish(it.publishedEvents()))
                        .map(ignored -> addressId);
    }

}
