package com.softwarearchetypes.party;

@FunctionalInterface
public interface AddressDefiningPolicy {

    boolean isAddressDefinitionAllowedFor(Addresses addresses, Address address);

    static AddressDefiningPolicy noDuplicateAddresses() {
        return new NoDuplicateAddressesPolicy();
    }

    static AddressDefiningPolicy noOverlappingValidityForSameType() {
        return new NoOverlappingValidityForSameTypePolicy();
    }

    static AddressDefiningPolicy composite(AddressDefiningPolicy... policies) {
        return new CompositeAddressDefiningPolicy(policies);
    }

    static AddressDefiningPolicy all() {
        return composite(
                noDuplicateAddresses(),
                noOverlappingValidityForSameType()
        );
    }

    static AddressDefiningPolicy alwaysAllow() {
        return new AlwaysAllowAddressDefiningPolicy();
    }

    AddressDefiningPolicy DEFAULT = all();
}

final class AlwaysAllowAddressDefiningPolicy implements AddressDefiningPolicy {

    @Override
    public boolean isAddressDefinitionAllowedFor(Addresses addresses, Address address) {
        return true;
    }
}

final class NoDuplicateAddressesPolicy implements AddressDefiningPolicy {

    @Override
    public boolean isAddressDefinitionAllowedFor(Addresses addresses, Address newAddress) {
        boolean duplicateExists = addresses.asSet().stream()
                .filter(addr -> addr.getClass().equals(newAddress.getClass()))
                .anyMatch(addr -> addr.addressDetails().equals(newAddress.addressDetails()));

        return !duplicateExists;
    }
}

final class NoOverlappingValidityForSameTypePolicy implements AddressDefiningPolicy {

    @Override
    public boolean isAddressDefinitionAllowedFor(Addresses addresses, Address newAddress) {
        boolean hasOverlap = addresses.asSet().stream()
                .filter(addr -> addr.getClass().equals(newAddress.getClass()))
                .filter(addr -> addr.useTypes().stream()
                        .anyMatch(useType -> newAddress.useTypes().contains(useType)))
                .anyMatch(addr -> addr.validity().overlaps(newAddress.validity()));

        return !hasOverlap;
    }
}

final class CompositeAddressDefiningPolicy implements AddressDefiningPolicy {

    private final AddressDefiningPolicy[] policies;

    CompositeAddressDefiningPolicy(AddressDefiningPolicy... policies) {
        this.policies = policies;
    }

    @Override
    public boolean isAddressDefinitionAllowedFor(Addresses addresses, Address address) {
        for (AddressDefiningPolicy policy : policies) {
            if (!policy.isAddressDefinitionAllowedFor(addresses, address)) {
                return false;
            }
        }
        return true;
    }
}
