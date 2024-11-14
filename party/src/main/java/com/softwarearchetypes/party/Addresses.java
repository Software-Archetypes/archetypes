package com.softwarearchetypes.party;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.common.Version;
import com.softwarearchetypes.events.AddressRelatedEvent;
import com.softwarearchetypes.events.AddressRemovalFailed;
import com.softwarearchetypes.events.AddressUpdateFailed;

import static java.util.stream.Collectors.toMap;

public class Addresses {

    private final PartyId partyId;
    private final Map<AddressId, Address> addresses;

    private final List<AddressRelatedEvent> events = new LinkedList<>();
    private final Version version;

    private Addresses(PartyId partyId, Set<Address> addresses, Version version) {
        this.partyId = partyId;
        this.addresses = mapFrom(addresses);
        this.version = version;
    }

    public Result<AddressUpdateFailed, Addresses> define(Address address) {
        if (addresses.containsKey(address.id())) {
            return addresses.get(address.id()).updateWith(address).map(event -> {
                events.add(event);
                return this;
            });
        } else {
            addresses.put(address.id(), address);
            events.add(address.toAddressDefinitionSucceededEvent());
            return Result.success(this);
        }
    }

    public Result<AddressRemovalFailed, Addresses> removeAddressWith(AddressId addressId) {
        Optional<Address> address = Optional.ofNullable(addresses.get(addressId));
        address.ifPresent(it -> {
            addresses.remove(addressId);
            events.add(it.toAddressRemovalSucceededEvent());
        });
        return Result.success(this);
    }

    private static Map<AddressId, Address> mapFrom(Set<Address> addresses) {
        return Optional.ofNullable(addresses).orElse(new HashSet<>()).stream().collect(toMap(Address::id, it -> it));
    }
}
