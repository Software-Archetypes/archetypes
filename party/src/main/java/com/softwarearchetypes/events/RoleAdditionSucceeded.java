package com.softwarearchetypes.events;

public sealed interface RoleAdditionSucceeded extends PartyRelatedEvent permits RoleAdded, RoleAdditionSkipped {
}
