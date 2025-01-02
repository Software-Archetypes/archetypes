package com.softwarearchetypes.party.events;

public sealed interface PartyRegistered extends PartyRelatedEvent, PublishedEvent permits CompanyRegistered, OrganizationUnitRegistered, PersonRegistered {

}
