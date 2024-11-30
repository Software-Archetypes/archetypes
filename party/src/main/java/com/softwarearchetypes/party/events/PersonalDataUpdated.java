package com.softwarearchetypes.party.events;

public record PersonalDataUpdated(String partyId, String firstName, String lastName) implements PersonalDataUpdateSucceeded, PublishedEvent {

}
