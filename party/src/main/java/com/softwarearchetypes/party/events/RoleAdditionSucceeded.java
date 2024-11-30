package com.softwarearchetypes.party.events;

public sealed interface RoleAdditionSucceeded extends PartyRelatedEvent permits RoleAdded, RoleAdditionSkipped {
}
