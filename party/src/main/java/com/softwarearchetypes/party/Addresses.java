package com.softwarearchetypes.party;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.common.Version;
import com.softwarearchetypes.party.events.AddressAdditionFailed;
import com.softwarearchetypes.party.events.AddressDefinitionFailed;
import com.softwarearchetypes.party.events.AddressRelatedEvent;
import com.softwarearchetypes.party.events.AddressRemovalFailed;
import com.softwarearchetypes.party.events.AddressRemovalSkipped;
import com.softwarearchetypes.party.events.AddressUpdateFailed;
import com.softwarearchetypes.party.events.AddressUpdateSkipped;
import com.softwarearchetypes.party.events.PublishedEvent;

import static java.util.stream.Collectors.toMap;

public class Addresses {

    private static final AddressDefiningPolicy DEFAULT_ADDRESS_DEFINING_POLICY = new AlwaysAllowAddressDefiningPolicy();
    private final PartyId partyId;
    private final Map<AddressId, Address> addresses;
    private final List<AddressRelatedEvent> events = new LinkedList<>();
    private final Version version;
    private final AddressDefiningPolicy addressDefiningPolicy;

    private Addresses(PartyId partyId, Set<Address> addresses, Version version, AddressDefiningPolicy addressDefiningPolicy) {
        this.partyId = partyId;
        this.addresses = mapFrom(addresses);
        this.version = version;
        this.addressDefiningPolicy = addressDefiningPolicy;
    }

    public static Addresses emptyAddressesFor(PartyId partyId) {
        return emptyAddressesFor(partyId, DEFAULT_ADDRESS_DEFINING_POLICY);
    }

    public static Addresses emptyAddressesFor(PartyId partyId, AddressDefiningPolicy addressDefiningPolicy) {
        return new Addresses(partyId, Set.of(), Version.initial(), addressDefiningPolicy);
    }

    PartyId partyId() {
        return partyId;
    }

    Version version() {
        return version;
    }

    List<AddressRelatedEvent> events() {
        return List.copyOf(events);
    }

    Set<Address> asSet() {
        return new HashSet<>(addresses.values());
    }

    public Result<AddressDefinitionFailed, Addresses> addOrUpdate(Address address) {
        if (addresses.containsKey(address.id())) {
            return updateWithDataFrom(addresses.get(address.id()), address);
        } else if (addressDefiningPolicy.isAddressDefinitionAllowedFor(this, address)) {
            addresses.put(address.id(), address);
            events.add(address.toAddressDefinitionSucceededEvent());
            return Result.success(this);
        } else {
            return Result.failure(AddressAdditionFailed.dueToPolicyNotMetFor(address.id().asString(), address.partyId().asString()));
        }
    }

    public Result<AddressRemovalFailed, Addresses> removeAddressWith(AddressId addressId) {
        Optional<Address> address = Optional.ofNullable(addresses.get(addressId));
        address.ifPresentOrElse(it -> {
                    addresses.remove(addressId);
                    events.add(it.toAddressRemovalSucceededEvent());
                },
                () -> events.add(AddressRemovalSkipped.dueToAddressNotFoundFor(addressId.asString(), partyId.asString())));
        return Result.success(this);
    }

    private Result<AddressDefinitionFailed, Addresses> updateWithDataFrom(Address addressToBeUpdated, Address newAddress) {
        if (addressToBeUpdated.getClass().isAssignableFrom(newAddress.getClass())) {
            if (!addressToBeUpdated.equals(newAddress)) {
                this.addresses.put(newAddress.id(), newAddress);
                this.events.add(newAddress.toAddressUpdateSucceededEvent());
            } else {
                this.events.add(addressUpdateSkippedDueToNoChangesIdentifiedFor(addressToBeUpdated));
            }
            return Result.success(this);
        } else {
            return Result.failure(AddressUpdateFailed.dueToNotMatchingAddressType());
        }
    }

    private AddressUpdateSkipped addressUpdateSkippedDueToNoChangesIdentifiedFor(Address address) {
        return AddressUpdateSkipped.dueToNoChangesIdentifiedFor(address.id().asString(), partyId.asString());
    }

    private static Map<AddressId, Address> mapFrom(Set<Address> addresses) {
        return Optional.ofNullable(addresses).orElse(new HashSet<>()).stream().collect(toMap(Address::id, it -> it));
    }

    List<PublishedEvent> publishedEvents() {
        return events.stream().filter(PublishedEvent.class::isInstance).map(PublishedEvent.class::cast).collect(Collectors.toList());
    }
}
