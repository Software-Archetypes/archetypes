package com.softwarearchetypes.party;

import static com.softwarearchetypes.common.Preconditions.checkArgument;
import static com.softwarearchetypes.common.StringUtils.isNotBlank;

//TODO: combine with Party?
public record PartyRole(PartyId partyId, Role role) {

    public PartyRole {
        checkArgument(partyId != null, "PartyId cannot be null");
        checkArgument(role != null, "Role cannot be null");
    }

    static PartyRole of(PartyId partyId, String value) {
        return of(partyId, Role.of(value));
    }

    static PartyRole of(PartyId partyId, Role role) {
        return new PartyRole(partyId, role);
    }

}
