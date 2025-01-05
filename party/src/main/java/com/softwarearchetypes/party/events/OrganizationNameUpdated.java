package com.softwarearchetypes.party.events;

public record OrganizationNameUpdated(String partyId, String value) implements OrganizationNameUpdateSucceeded, PublishedEvent {

}
