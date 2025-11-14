package com.softwarearchetypes.party;

import com.softwarearchetypes.party.commands.AssignPartyRelationshipCommand;

class PartyRelationshipTestSupport {

    private final PartyRelationshipsFacade facade;

    PartyRelationshipTestSupport(PartyRelationshipsFacade facade) {
        this.facade = facade;
    }

    void thereIsARelationBetween(Party from, Role fromRole, Party to, Role toRole, RelationshipName name) {
        facade.handle(new AssignPartyRelationshipCommand(from.id(), fromRole.asString(), to.id(), toRole.asString(), name.asString()));
    }
}
