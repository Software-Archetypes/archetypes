package com.softwarearchetypes.party;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.common.Version;
import com.softwarearchetypes.events.PartyRelatedEvent;
import com.softwarearchetypes.events.RegisteredIdentityAdditionFailed;
import com.softwarearchetypes.events.RegisteredIdentityRemovalFailed;
import com.softwarearchetypes.events.RoleAdditionFailed;
import com.softwarearchetypes.events.RoleRemovalFailed;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

public sealed abstract class Party permits Organization, Person {

    private final PartyId partyId;
    private final Addresses addresses;
    private final Roles roles;
    private final RegisteredIdentifiers registeredIdentifiers;
    private final List<PartyRelatedEvent> events = new LinkedList<>();
    private final Version version;

    Party(PartyId partyId, Addresses addresses, Roles roles, RegisteredIdentifiers registeredIdentifiers, Version version) {
        checkArgument(partyId != null, "Party Id cannot be null");
        checkArgument(addresses != null, "Addresses cannot be null");
        checkArgument(roles != null, "Roles cannot be null");
        checkArgument(registeredIdentifiers != null, "Registered identifiers cannot be null");
        checkArgument(version != null, "Version cannot be null");
        this.partyId = partyId;
        this.addresses = addresses;
        this.roles = roles;
        this.registeredIdentifiers = registeredIdentifiers;
        this.version = version;
    }

    public Result<RoleAdditionFailed, Party> add(Role role) {
        return roles.add(role).map(event -> {
            events.add(event);
            return this;
        });
    }

    public Result<RoleRemovalFailed, Party> remove(Role role) {
        return roles.remove(role).map(event -> {
            events.add(event);
            return this;
        });
    }

    public Result<RegisteredIdentityAdditionFailed, Party> add(RegisteredIdentifier identifier) {
        return registeredIdentifiers.add(identifier).map(event -> {
            events.add(event);
            return this;
        });
    }

    public Result<RegisteredIdentityRemovalFailed, Party> remove(RegisteredIdentifier identifier) {
        return registeredIdentifiers.remove(identifier).map(event -> {
            events.add(event);
            return this;
        });
    }

    public final PartyId id() {
        return partyId;
    }

    public final Addresses addresses() {
        return addresses;
    }

    public final Roles roles() {
        return roles;
    }

    public final RegisteredIdentifiers registeredIdentifiers() {
        return registeredIdentifiers;
    }

    public List<PartyRelatedEvent> events() {
        return List.copyOf(events);
    }

    final void register(PartyRelatedEvent event) {
        events.add(event);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Party party)) {
            return false;
        }
        return Objects.equals(partyId, party.partyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partyId);
    }
}
