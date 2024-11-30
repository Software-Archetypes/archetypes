package com.softwarearchetypes.party.events;

public sealed interface RoleRemovalSucceeded extends PartyRelatedEvent permits RoleRemovalSkipped, RoleRemoved {

}
