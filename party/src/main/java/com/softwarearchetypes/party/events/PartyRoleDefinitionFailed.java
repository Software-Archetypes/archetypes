package com.softwarearchetypes.party.events;

public record PartyRoleDefinitionFailed(String reason) implements PartyRelatedFailureEvent {

    private static final String POLICIES_NOT_MET_REASON = "Policies for assigning party role not met";

    public static PartyRoleDefinitionFailed dueToPoliciesNotMet() {
        return new PartyRoleDefinitionFailed(POLICIES_NOT_MET_REASON);
    }
}
