package com.softwarearchetypes.party.events;

public record PartyRelationshipDefinitionFailed(String reason) implements PartyRelatedFailureEvent {

    private static final String POLICIES_NOT_MET_REASON = "Policies for defining party relationship not met";

    public static PartyRelationshipDefinitionFailed dueToPoliciesNotMet() {
        return new PartyRelationshipDefinitionFailed(POLICIES_NOT_MET_REASON);
    }

    public static PartyRelationshipDefinitionFailed dueTo(String reason) {
        return new PartyRelationshipDefinitionFailed(reason);
    }
}
