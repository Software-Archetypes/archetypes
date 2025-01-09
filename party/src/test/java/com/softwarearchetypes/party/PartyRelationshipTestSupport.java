package com.softwarearchetypes.party;

class PartyRelationshipTestSupport {

    private final PartyRelationshipsFacade facade;

    PartyRelationshipTestSupport(PartyRelationshipsFacade facade) {
        this.facade = facade;
    }

    void thereIsARelationBetween(Party from, Role fromRole, Party to, Role toRole, RelationshipName name) {
        facade.assign(from.id(), fromRole, to.id(), toRole, name);
    }
}
