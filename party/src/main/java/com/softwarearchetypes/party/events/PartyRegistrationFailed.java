package com.softwarearchetypes.party.events;

public record PartyRegistrationFailed(Throwable reason) implements PartyRelatedFailureEvent {

    public static PartyRegistrationFailed dueTo(Throwable reason) {
        return new PartyRegistrationFailed(reason);
    }
}
