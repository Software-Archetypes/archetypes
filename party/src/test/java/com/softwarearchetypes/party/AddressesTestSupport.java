package com.softwarearchetypes.party;

class AddressesTestSupport {

    private final AddressesFacade facade;

    AddressesTestSupport(AddressesFacade facade) {
        this.facade = facade;
    }

    void thereIsAddressOf(Address address) {
        facade.addOrUpdate(address.partyId(), address);
    }
}
