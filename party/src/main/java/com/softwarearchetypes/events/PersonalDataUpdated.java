package com.softwarearchetypes.events;

public record PersonalDataUpdated(String firstName, String lastName) implements PersonalDataUpdateSucceeded {
}
