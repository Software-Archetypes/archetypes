package com.softwarearchetypes.events;

public sealed interface RoleRemovalSucceeded extends PartyRelatedEvent permits RoleRemovalSkipped, RoleRemoved {

}
