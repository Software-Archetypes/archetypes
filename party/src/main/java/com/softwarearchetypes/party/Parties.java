package com.softwarearchetypes.party;

class Parties {

    private final PartyRepository partyRepository;

    Parties(PartyRepository partyRepository) {
        this.partyRepository = partyRepository;
    }

    //register
    //-> napisać że można to rozdzielić na Persons + Organizations, jeśli będzie dużo różnic
}
