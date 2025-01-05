package com.softwarearchetypes.party;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.party.events.PartyRoleDefinitionFailed;

import static com.softwarearchetypes.party.PartyFixture.somePerson;
import static com.softwarearchetypes.party.RoleFixture.someRole;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PartyRoleFactoryTest {

    @Test
    void shouldCreatePartyRoleWhenAcceptAllPolicyIsApplied() {
        //given
        PartyRoleFactory factory = new PartyRoleFactory();

        //and
        Party party = somePerson().withRandomPartyId().build();
        Role role = someRole();

        //and
        PartyRole expectedPartyRole = PartyRole.of(party.id(), role);

        //when
        Result<PartyRoleDefinitionFailed, PartyRole> result = factory.defineFor(party, role);

        //then
        assertEquals(expectedPartyRole, result.getSuccess());
    }
    
    @Test
    void shouldFailToCreatePartyRoleWhenPolicyAcceptingOnlyCompaniesIsApplied() {
        //given
        PartyRoleDefiningPolicy acceptOnlyCompaniesPolicy = (party, role) -> Optional.ofNullable(party).filter(Company.class::isInstance).isPresent();
        PartyRoleFactory factory = new PartyRoleFactory(acceptOnlyCompaniesPolicy);

        //and
        Party party = somePerson().withRandomPartyId().build();
        Role role = someRole();

        //when
        Result<PartyRoleDefinitionFailed, PartyRole> result = factory.defineFor(party, role);

        //then
        assertEquals(PartyRoleDefinitionFailed.dueToPoliciesNotMet(), result.getFailure());
    }
}