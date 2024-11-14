package com.softwarearchetypes.party;

import java.util.Optional;

public interface PartyRepository {

    // Optional<? extends Party> findBy(PhoneNumber phoneNumber);
    //
    // Optional<? extends Party> findBy(EmailAddress emailAddress);
    //
    // Optional<? extends Party> findBy(PartyId partyId);
    //
    // Optional<? extends Party> findBy(Identity identity);

    void save(Party party);

    void delete(Party party);
}
