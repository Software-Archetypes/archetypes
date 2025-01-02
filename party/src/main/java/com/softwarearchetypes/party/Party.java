package com.softwarearchetypes.party;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.common.Version;
import com.softwarearchetypes.party.events.PartyRegistered;
import com.softwarearchetypes.party.events.PartyRelatedEvent;
import com.softwarearchetypes.party.events.PublishedEvent;
import com.softwarearchetypes.party.events.RegisteredIdentifierAdded;
import com.softwarearchetypes.party.events.RegisteredIdentifierAdditionSkipped;
import com.softwarearchetypes.party.events.RegisteredIdentifierRemovalSkipped;
import com.softwarearchetypes.party.events.RegisteredIdentifierRemoved;
import com.softwarearchetypes.party.events.RegisteredIdentifierAdditionFailed;
import com.softwarearchetypes.party.events.RegisteredIdentifierRemovalFailed;
import com.softwarearchetypes.party.events.RoleAdded;
import com.softwarearchetypes.party.events.RoleAdditionFailed;
import com.softwarearchetypes.party.events.RoleAdditionSkipped;
import com.softwarearchetypes.party.events.RoleRemovalFailed;
import com.softwarearchetypes.party.events.RoleRemovalSkipped;
import com.softwarearchetypes.party.events.RoleRemoved;

import static com.softwarearchetypes.common.Preconditions.checkArgument;
import static com.softwarearchetypes.common.Preconditions.checkNotNull;

public sealed abstract class Party permits Organization, Person {

    private final PartyId partyId;
    private final Set<Role> roles;
    private final Set<RegisteredIdentifier> registeredIdentifiers;
    private final List<PartyRelatedEvent> events = new LinkedList<>();
    private final Version version;

    Party(PartyId partyId, Set<Role> roles, Set<RegisteredIdentifier> registeredIdentifiers, Version version) {
        checkArgument(partyId != null, "Party Id cannot be null");
        checkArgument(roles != null, "Roles cannot be null");
        checkArgument(registeredIdentifiers != null, "Registered identifiers cannot be null");
        checkArgument(version != null, "Version cannot be null");
        this.partyId = partyId;
        this.roles = new HashSet<>(roles);
        this.registeredIdentifiers = new HashSet<>(registeredIdentifiers);
        this.version = version;
    }

    public Result<RoleAdditionFailed, Party> add(Role role) {
        checkNotNull(role, "Role cannot be null");
        if (!roles.contains(role)) {
            roles.add(role);
            events.add(new RoleAdded(partyId.asString(), role.asString()));
        } else {
            //for idempotency
            events.add(RoleAdditionSkipped.dueToDuplicationFor(partyId.asString(), role.asString()));
        }
        return Result.success(this);
    }

    Result<RoleRemovalFailed, Party> remove(Role role) {
        checkNotNull(role, "Role cannot be null");
        if (roles.contains(role)) {
            roles.remove(role);
            events.add(new RoleRemoved(partyId.asString(), role.asString()));
        } else {
            //for idempotency
            events.add(RoleRemovalSkipped.dueToMissingRoleFor(partyId.asString(), role.asString()));
        }
        return Result.success(this);
    }

    public Result<RegisteredIdentifierAdditionFailed, Party> add(RegisteredIdentifier identifier) {
        checkNotNull(identifier, "Registered identifier cannot be null");
        if (!registeredIdentifiers.contains(identifier)) {
            registeredIdentifiers.add(identifier);
            events.add(new RegisteredIdentifierAdded(partyId.asString(), identifier.type(), identifier.asString()));
        } else {
            //for idempotency
            events.add(RegisteredIdentifierAdditionSkipped.dueToDataDuplicationFor(partyId.asString(), identifier.type(), identifier.asString()));
        }
        return Result.success(this);
    }

    public Result<RegisteredIdentifierRemovalFailed, Party> remove(RegisteredIdentifier identifier) {
        checkNotNull(identifier, "Registered identifier cannot be null");
        if (registeredIdentifiers.contains(identifier)) {
            registeredIdentifiers.remove(identifier);
            events.add(new RegisteredIdentifierRemoved(partyId.asString(), identifier.type(), identifier.asString()));
        } else {
            //for idempotency
            events.add(RegisteredIdentifierRemovalSkipped.dueToMissingIdentifierFor(partyId.asString(), identifier.type(), identifier.asString()));
        }
        return Result.success(this);
    }

    public final PartyId id() {
        return partyId;
    }

    public final Set<Role> roles() {
        return Set.copyOf(roles);
    }

    public final Set<RegisteredIdentifier> registeredIdentifiers() {
        return Set.copyOf(registeredIdentifiers);
    }

    public List<PartyRelatedEvent> events() {
        return List.copyOf(events);
    }

    public List<PublishedEvent> publishedEvents() {
        return events.stream().filter(PublishedEvent.class::isInstance).map(PublishedEvent.class::cast).collect(Collectors.toList());
    }

    abstract PartyRegistered toPartyRegisteredEvent();

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
