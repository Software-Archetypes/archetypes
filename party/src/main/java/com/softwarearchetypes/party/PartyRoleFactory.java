package com.softwarearchetypes.party;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.party.events.PartyRoleDefinitionFailed;

import static com.softwarearchetypes.party.events.PartyRoleDefinitionFailed.dueToPoliciesNotMet;

class PartyRoleFactory {

    private static final PartyRoleDefiningPolicy DEFAULT_PARTY_ROLE_DEFINING_POLICY = new AlwaysAllowPartyRoleDefiningPolicy();

    private final PartyRoleDefiningPolicy policy;

    PartyRoleFactory(PartyRoleDefiningPolicy policy) {
        this.policy = policy != null ? policy : DEFAULT_PARTY_ROLE_DEFINING_POLICY;
    }

    PartyRoleFactory() {
        this(null);
    }

    Result<PartyRoleDefinitionFailed, PartyRole> defineFor(Party party, Role role) {
        if (policy.canDefineFor(party, role)) {
            return Result.success(PartyRole.of(party.id(), role));
        } else {
            return Result.failure(dueToPoliciesNotMet());
        }
    }
}
