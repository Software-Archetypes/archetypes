package com.softwarearchetypes.party;

import org.junit.jupiter.api.Test;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.party.events.InMemoryEventsPublisher;
import com.softwarearchetypes.party.events.PartyRelationshipDefinitionFailed;

import static com.softwarearchetypes.party.RelationshipNameFixture.someRelationshipName;
import static com.softwarearchetypes.party.RoleFixture.someRole;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PartyRelationshipsFacadeTest {

    private final InMemoryPartyRepository partyRepository = new InMemoryPartyRepository();
    private final PartiesQueries partiesQueries = new PartiesQueries(partyRepository);
    private final InMemoryEventsPublisher eventsPublisher = new InMemoryEventsPublisher();
    private final PartyTestSupport partyTestSupport = new PartyTestSupport(partyRepository);

    private final PartyRoleFactory partyRoleFactory = new PartyRoleFactory();
    private final PartyRelationshipFactory partyRelationshipFactory = new PartyRelationshipFactory(PartyRelationshipId::random);
    private final InMemoryPartyRelationshipRepository partyRelationshipRepository = new InMemoryPartyRelationshipRepository();
    private final PartyRelationshipsFacade facade = new PartyRelationshipsFacade(partyRoleFactory, partyRelationshipFactory, partyRelationshipRepository, partiesQueries, eventsPublisher);
    private final PartyRelationshipsQueries partyRelationshipsQueries = new PartyRelationshipsQueries(partyRelationshipRepository);

    @Test
    void shouldFailToAddRelationshipWhenFromPartyDoesNotExist() {
        //given
        PartyId nonExistingFromPartyId = PartyId.random();
        Role nonExistingFromPartyRole = someRole();

        //and
        Party toParty = partyTestSupport.thereIsSomePerson();
        Role toPartyRole = someRole();

        //and
        RelationshipName relationshipName = someRelationshipName();

        //and
        PartyRelationshipDefinitionFailed expectedResult = PartyRelationshipDefinitionFailed.dueTo("PARTY_NOT_FOUND");

        //when
        Result<PartyRelationshipDefinitionFailed, PartyRelationship> result = facade.assign(nonExistingFromPartyId, nonExistingFromPartyRole, toParty.id(), toPartyRole, relationshipName);

        //then
        assertEquals(expectedResult, result.getFailure());
        assertTrue(partyRelationshipsQueries.findAllRelationsFrom(nonExistingFromPartyId).isEmpty());
    }

    @Test
    void shouldFailToAddRelationshipWhenToPartyDoesNotExist() {
        //given
        Party fromParty = partyTestSupport.thereIsSomePerson();
        Role fromPartyRole = someRole();

        //and
        PartyId nonExistingToPartyId = PartyId.random();
        Role nonExistingToPartyRole = someRole();

        //and
        RelationshipName relationshipName = someRelationshipName();

        //and
        PartyRelationshipDefinitionFailed expectedResult = PartyRelationshipDefinitionFailed.dueTo("PARTY_NOT_FOUND");

        //when
        Result<PartyRelationshipDefinitionFailed, PartyRelationship> result = facade.assign(fromParty.id(), fromPartyRole, nonExistingToPartyId, nonExistingToPartyRole, relationshipName);

        //then
        assertEquals(expectedResult, result.getFailure());
        assertTrue(partyRelationshipsQueries.findAllRelationsFrom(fromParty.id()).isEmpty());
    }

    @Test
    void shouldAddRelationshipBetweenParties() {
        //given
        Party fromParty = partyTestSupport.thereIsSomePerson();
        Role fromPartyRole = someRole();

        //and
        Party toParty = partyTestSupport.thereIsSomePerson();
        Role toPartyRole = someRole();

        //and
        RelationshipName relationshipName = someRelationshipName();

        //when
        Result<PartyRelationshipDefinitionFailed, PartyRelationship> result = facade.assign(fromParty.id(), fromPartyRole, toParty.id(), toPartyRole, relationshipName);

        //then
        assertEquals(result.getSuccess(), partyRelationshipsQueries.findBy(result.getSuccess().id()).get());
    }

    @Test
    void shouldRemoveRelationshipBetweenParties() {
        //given
        Party fromParty = partyTestSupport.thereIsSomePerson();
        Role fromPartyRole = someRole();

        //and
        Party toParty = partyTestSupport.thereIsSomePerson();
        Role toPartyRole = someRole();

        //and
        RelationshipName relationshipName = someRelationshipName();

        //and
        PartyRelationshipId existingRelId = facade.assign(fromParty.id(), fromPartyRole, toParty.id(), toPartyRole, relationshipName)
                                                  .map(PartyRelationship::id)
                                                  .getSuccess();

        //when
        facade.remove(existingRelId);

        //then
        assertTrue(partyRelationshipsQueries.findBy(existingRelId).isEmpty());
    }
}