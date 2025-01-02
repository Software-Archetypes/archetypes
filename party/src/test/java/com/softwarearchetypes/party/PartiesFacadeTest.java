package com.softwarearchetypes.party;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.party.PartyFixture.FixablePartyIdSupplier;
import com.softwarearchetypes.party.events.CompanyRegistered;
import com.softwarearchetypes.party.events.InMemoryEventsPublisher;
import com.softwarearchetypes.party.events.IncorrectPartyTypeIdentified;
import com.softwarearchetypes.party.events.OrganizationUnitRegistered;
import com.softwarearchetypes.party.events.PartyRelatedFailureEvent;
import com.softwarearchetypes.party.events.PersonRegistered;
import com.softwarearchetypes.party.events.PersonalDataUpdated;
import com.softwarearchetypes.party.events.PublishedEvent;
import com.softwarearchetypes.party.events.RegisteredIdentifierAdded;
import com.softwarearchetypes.party.events.RegisteredIdentifierRemoved;
import com.softwarearchetypes.party.events.RoleAdded;
import com.softwarearchetypes.party.events.RoleRemoved;

import static com.softwarearchetypes.common.CollectionFixture.copyAndAdd;
import static com.softwarearchetypes.party.OrganizationNameFixture.someOrganizationName;
import static com.softwarearchetypes.party.PartyFixture.somePerson;
import static com.softwarearchetypes.party.PersonalDataFixture.somePersonalData;
import static com.softwarearchetypes.party.RegisteredIdentifierFixture.someIdentifierSetOfSize;
import static com.softwarearchetypes.party.RegisteredIdentifierFixture.someRegisteredIdentifier;
import static com.softwarearchetypes.party.RoleFixture.someRole;
import static com.softwarearchetypes.party.RoleFixture.someRoleSetOfSize;
import static com.softwarearchetypes.party.RoleFixture.stringSetFrom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PartiesFacadeTest {

    private final InMemoryEventsPublisher eventPublisher = new InMemoryEventsPublisher();
    private final InMemoryPartyRepository repository = new InMemoryPartyRepository();
    private final FixablePartyIdSupplier partyIdSupplier = new FixablePartyIdSupplier();
    private final PartiesFacade partiesFacade = new PartiesFacade(repository, eventPublisher, partyIdSupplier);
    private final PartiesQueries partiesQueries = new PartiesQueries(repository);

    private final PartiesTestEventListener testEventListener = new PartiesTestEventListener(eventPublisher);

    @Test
    void canRegisterPerson() {
        //given
        PersonalData personalData = somePersonalData();
        Set<Role> roles = someRoleSetOfSize(5);
        RegisteredIdentifier identifier = someRegisteredIdentifier();

        //when
        partiesFacade.registerPersonFor(personalData, roles, Set.of(identifier));

        //then
        Optional<Party> party = partiesQueries.findOneBy(identifier);

        assertTrue(party.isPresent());
        assertInstanceOf(Person.class, party.get());
        assertEquals(personalData, ((Person) party.get()).personalData());
        assertEquals(Set.of(identifier), party.get().registeredIdentifiers());
        assertEquals(roles, party.get().roles());
    }

    @Test
    void personRegisteredEventIsEmittedWhenOperationSucceeds() {
        //given
        PersonalData personalData = somePersonalData();
        Set<Role> roles = someRoleSetOfSize(5);
        RegisteredIdentifier identifier = someRegisteredIdentifier();

        //and
        PartyId partyId = PartyId.random();
        partyIdSupplier.fixPartyIdTo(partyId);

        //and
        PersonRegistered expectedEvent = new PersonRegistered(partyId.asString(), personalData.firstName(), personalData.lastName(), Set.of(identifier.asString()), stringSetFrom(roles));

        //when
        partiesFacade.registerPersonFor(personalData, roles, Set.of(identifier));

        //then
        assertTrue(testEventListener.thereIsAnEventEqualTo(expectedEvent));

        //cleanup
        partyIdSupplier.clear();
    }

    @Test
    void canRegisterCompany() {
        //given
        OrganizationName organizationName = someOrganizationName();
        Set<Role> roles = someRoleSetOfSize(5);
        RegisteredIdentifier identifier = someRegisteredIdentifier();

        //when
        partiesFacade.registerCompanyFor(organizationName, roles, Set.of(identifier));

        //then
        Optional<Party> party = partiesQueries.findOneBy(identifier);

        assertTrue(party.isPresent());
        assertInstanceOf(Company.class, party.get());
        assertEquals(organizationName, ((Company) party.get()).organizationName());
        assertEquals(Set.of(identifier), party.get().registeredIdentifiers());
        assertEquals(roles, party.get().roles());
    }

    @Test
    void companyRegisteredEventIsEmittedWhenOperationSucceeds() {
        //given
        OrganizationName organizationName = someOrganizationName();
        Set<Role> roles = someRoleSetOfSize(5);
        RegisteredIdentifier identifier = someRegisteredIdentifier();

        //and
        PartyId partyId = PartyId.random();
        partyIdSupplier.fixPartyIdTo(partyId);

        //and
        CompanyRegistered expectedEvent = new CompanyRegistered(partyId.asString(), organizationName.asString(), Set.of(identifier.asString()), stringSetFrom(roles));

        //when
        partiesFacade.registerCompanyFor(organizationName, roles, Set.of(identifier));

        //then
        assertTrue(testEventListener.thereIsAnEventEqualTo(expectedEvent));

        //cleanup
        partyIdSupplier.clear();
    }

    @Test
    void canRegisterOrganizationUnit() {
        //given
        OrganizationName organizationName = someOrganizationName();
        Set<Role> roles = someRoleSetOfSize(5);
        RegisteredIdentifier identifier = someRegisteredIdentifier();

        //when
        partiesFacade.registerOrganizationUnitFor(organizationName, roles, Set.of(identifier));

        //then
        Optional<Party> party = partiesQueries.findOneBy(identifier);

        assertTrue(party.isPresent());
        assertInstanceOf(OrganizationUnit.class, party.get());
        assertEquals(organizationName, ((OrganizationUnit) party.get()).organizationName());
        assertEquals(Set.of(identifier), party.get().registeredIdentifiers());
        assertEquals(roles, party.get().roles());
    }

    @Test
    void organizationUnitRegisteredEventIsEmittedWhenOperationSucceeds() {
        //given
        OrganizationName organizationName = someOrganizationName();
        Set<Role> roles = someRoleSetOfSize(5);
        RegisteredIdentifier identifier = someRegisteredIdentifier();

        //and
        PartyId partyId = PartyId.random();
        partyIdSupplier.fixPartyIdTo(partyId);

        //and
        OrganizationUnitRegistered expectedEvent = new OrganizationUnitRegistered(partyId.asString(), organizationName.asString(), Set.of(identifier.asString()), stringSetFrom(roles));

        //when
        partiesFacade.registerOrganizationUnitFor(organizationName, roles, Set.of(identifier));

        //then
        assertTrue(testEventListener.thereIsAnEventEqualTo(expectedEvent));

        //cleanup
        partyIdSupplier.clear();
    }

    @Test
    void canAddRoleToParty() {
        //given
        Party party = thereIsSomeParty();
        Role newRole = someRole();

        //when
        partiesFacade.add(party.id(), newRole);

        //then
        Optional<Party> updatedParty = partiesQueries.findBy(party.id());

        assertEquals(copyAndAdd(party.roles(), newRole), updatedParty.get().roles());
    }

    @Test
    void roleAddedEventIsEmittedWhenOperationSucceeds() {
        //given
        Party party = thereIsSomeParty();
        Role newRole = someRole();

        //and
        RoleAdded expectedEvent = new RoleAdded(party.id().asString(), newRole.asString());

        //when
        partiesFacade.add(party.id(), newRole);

        //then
        assertTrue(testEventListener.thereIsAnEventEqualTo(expectedEvent));
    }

    @Test
    void canRemoveRoleFromParty() {
        //given
        Role roleToBeRemoved = someRole();
        Party party = thereIs(somePerson().withRandomPartyId().with(roleToBeRemoved).build());

        //when
        partiesFacade.remove(party.id(), roleToBeRemoved);

        //then
        Optional<Party> updatedParty = partiesQueries.findBy(party.id());

        assertTrue(updatedParty.get().roles().isEmpty());
    }

    @Test
    void roleRemovedEventIsEmittedWhenOperationSucceeds() {
        //given
        Role roleToBeRemoved = someRole();
        Party party = thereIs(somePerson().withRandomPartyId().with(roleToBeRemoved).build());

        //and
        RoleRemoved expectedEvent = new RoleRemoved(party.id().asString(), roleToBeRemoved.asString());

        //when
        partiesFacade.remove(party.id(), roleToBeRemoved);

        //then
        assertTrue(testEventListener.thereIsAnEventEqualTo(expectedEvent));
    }

    @Test
    void canAddRegisteredIdentifierToParty() {
        //given
        Party party = thereIsSomeParty();
        RegisteredIdentifier newRegisteredIdentifier = someRegisteredIdentifier();

        //when
        partiesFacade.add(party.id(), newRegisteredIdentifier);

        //then
        Optional<Party> updatedParty = partiesQueries.findBy(party.id());

        assertEquals(copyAndAdd(party.registeredIdentifiers(), newRegisteredIdentifier), updatedParty.get().registeredIdentifiers());
    }

    @Test
    void registeredIdentifierAddedEventIsEmittedWhenOperationSucceeds() {
        //given
        Party party = thereIsSomeParty();
        RegisteredIdentifier newRegisteredIdentifier = someRegisteredIdentifier();

        //and
        RegisteredIdentifierAdded expectedEvent = new RegisteredIdentifierAdded(party.id().asString(),
                newRegisteredIdentifier.type(), newRegisteredIdentifier.asString());

        //when
        partiesFacade.add(party.id(), newRegisteredIdentifier);

        //then
        assertTrue(testEventListener.thereIsAnEventEqualTo(expectedEvent));
    }

    @Test
    void canRemoveRegisteredIdentifierFromParty() {
        //given
        RegisteredIdentifier idToBeRemoved = someRegisteredIdentifier();
        Party party = thereIs(somePerson().withRandomPartyId().with(idToBeRemoved).build());

        //when
        partiesFacade.remove(party.id(), idToBeRemoved);

        //then
        Optional<Party> updatedParty = partiesQueries.findBy(party.id());

        assertTrue(updatedParty.get().registeredIdentifiers().isEmpty());
    }

    @Test
    void registeredIdentifierRemovedEventIsEmittedWhenRegisteredIdentifierIsRemovedFromParty() {
        //given
        RegisteredIdentifier idToBeRemoved = someRegisteredIdentifier();
        Party party = thereIs(somePerson().withRandomPartyId().with(idToBeRemoved).build());

        //and
        RegisteredIdentifierRemoved expectedEvent = new RegisteredIdentifierRemoved(party.id().asString(), idToBeRemoved.type(), idToBeRemoved.asString());

        //when
        partiesFacade.remove(party.id(), idToBeRemoved);

        //then
        assertTrue(testEventListener.thereIsAnEventEqualTo(expectedEvent));
    }

    @Test
    void canUpdatePersonalDataOfExistingPerson() {
        //given
        Person party = thereIsSomePerson();
        PersonalData newPersonalData = somePersonalData();

        //when
        partiesFacade.update(party.id(), newPersonalData);

        //then
        Optional<Party> updatedParty = partiesQueries.findBy(party.id());

        assertEquals(newPersonalData, updatedParty.map(Person.class::cast).get().personalData());
    }

    @Test
    void cannotUpdateOrganizationNameOfExistingPerson() {
        //given
        Person party = thereIsSomePerson();
        OrganizationName newOrganizationName = someOrganizationName();

        //and
        IncorrectPartyTypeIdentified expectedResult = new IncorrectPartyTypeIdentified(party.id().asString(), "Organization");

        //when
        Result<PartyRelatedFailureEvent, Organization> result = partiesFacade.update(party.id(), newOrganizationName);

        //then
        assertEquals(expectedResult, result.getFailure());
    }

    @Test
    void cannotUpdatePersonalDataOfOrganization() {
        //given
        Party party = thereIsSomeOrganization();
        PersonalData newPersonalData = somePersonalData();

        //and
        IncorrectPartyTypeIdentified expectedResult = new IncorrectPartyTypeIdentified(party.id().asString(), "Person");

        //when
        Result<PartyRelatedFailureEvent, Person> result = partiesFacade.update(party.id(), newPersonalData);

        //then
        assertEquals(expectedResult, result.getFailure());
    }

    @Test
    void personalDataUpdatedEventIsEmittedWhenOperationSucceeds() {
        //given
        Person party = thereIsSomePerson();
        PersonalData newPersonalData = somePersonalData();

        //and
        PersonalDataUpdated expectedEvent = new PersonalDataUpdated(party.id().asString(), newPersonalData.firstName(), newPersonalData.lastName());

        //when
        partiesFacade.update(party.id(), newPersonalData);

        //then
        assertTrue(testEventListener.thereIsAnEventEqualTo(expectedEvent));
    }

    private Party thereIsSomeParty() {
        return thereIsSomePerson();
    }

    private Person thereIsSomePerson() {
        return partiesFacade.registerPersonFor(somePersonalData(), someRoleSetOfSize(5), someIdentifierSetOfSize(5)).getSuccess();
    }

    private Organization thereIsSomeOrganization() {
        return partiesFacade.registerCompanyFor(someOrganizationName(), someRoleSetOfSize(5), someIdentifierSetOfSize(5)).getSuccess();
    }

    private Party thereIs(Party party) {
        repository.save(party);
        return party;
    }

    static class PartiesTestEventListener implements InMemoryEventsPublisher.InMemoryEventObserver {

        private BlockingQueue<PublishedEvent> events = new LinkedBlockingQueue<>();

        PartiesTestEventListener(InMemoryEventsPublisher eventsPublisher) {
            eventsPublisher.register(this);
        }

        @Override
        public void handle(PublishedEvent event) {
            events.add(event);
        }

        Optional<PublishedEvent> findMatching(Predicate<PublishedEvent> predicate) {
            return events.stream().filter(predicate).findFirst();
        }

        boolean thereIsAnEventEqualTo(PublishedEvent expectedEvent) {
            return findMatching(it -> it.equals(expectedEvent)).isPresent();
        }
    }
}