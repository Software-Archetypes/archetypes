package com.softwarearchetypes.party;

@FunctionalInterface
public interface AddressDefiningPolicy {

    boolean isAddressDefinitionAllowedFor(Addresses addresses, Address address);
}

final class AlwaysAllowAddressDefiningPolicy implements AddressDefiningPolicy {

    @Override
    public boolean isAddressDefinitionAllowedFor(Addresses addresses, Address address) {
        return true;
    }
}
