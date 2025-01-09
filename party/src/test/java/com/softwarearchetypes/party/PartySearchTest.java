package com.softwarearchetypes.party;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.softwarearchetypes.party.events.InMemoryEventsPublisher;

import static com.softwarearchetypes.party.GeoAddressFixture.someGeoAddressFor;
import static com.softwarearchetypes.party.PartyFixture.someCompany;
import static com.softwarearchetypes.party.PartyFixture.somePerson;
import static com.softwarearchetypes.party.PersonalDataFixture.nameOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PartySearchTest {

    private final InMemoryEventsPublisher eventPublisher = new InMemoryEventsPublisher();

    //party
    private final InMemoryPartyRepository repository = new InMemoryPartyRepository();
    private final PartyTestSupport testSupport = new PartyTestSupport(repository);
    private final PartiesQueries partiesQueries = new PartiesQueries(repository);

    //rel
    private final PartyRoleFactory partyRoleFactory = new PartyRoleFactory();
    private final PartyRelationshipFactory partyRelationshipFactory = new PartyRelationshipFactory(PartyRelationshipId::random);
    private final InMemoryPartyRelationshipRepository partyRelationshipRepository = new InMemoryPartyRelationshipRepository();
    private final PartyRelationshipsFacade facade = new PartyRelationshipsFacade(partyRoleFactory, partyRelationshipFactory, partyRelationshipRepository, partiesQueries, eventPublisher);
    private final PartyRelationshipsQueries partyRelationshipsQueries = new PartyRelationshipsQueries(partyRelationshipRepository);
    private final PartyRelationshipTestSupport partyRelationshipTestSupport = new PartyRelationshipTestSupport(facade);

    //address
    private final InMemoryAddressesRepository addressesRepository = new InMemoryAddressesRepository();
    private final AddressesFacade addressesFacade = new AddressesFacade(addressesRepository, eventPublisher);
    private final AddressesTestSupport addressesTestSupport = new AddressesTestSupport(addressesFacade);
    private final AddressesQueries addressesQueries = new AddressesQueries(addressesRepository);

    //object under test
    private final PartySearch partySearch = new PartySearch(partiesQueries, partyRelationshipsQueries, addressesQueries);

    private static final Role PROVIDER = Role.of("PROVIDER");
    private static final Role PARTNER = Role.of("PARTNER");
    private static final Role EMPLOYER = Role.of("EMPLOYER");
    private static final Role EMPLOYEE = Role.of("EMPLOYEE");
    private static final Role ORDERER = Role.of("ORDERER");
    private static final Role CONTRACTOR = Role.of("CONTRACTOR");
    private static final RelationshipName EMPLOYMENT = RelationshipName.of("EMPLOYMENT");
    private static final RelationshipName CONTRACT = RelationshipName.of("CONTRACT");

    @Test
    void shouldFindAnEmployeeOfAProviderThatWorksInWarsaw() {
        //given - first provider graph
        Party firstProvider = testSupport.thereIs(someCompany().with(OrganizationName.of("FIRST Inc.")).withRandomPartyId().with(PROVIDER).build());

        Party romanTheContractorFromWarsaw = testSupport.thereIs(somePerson().with(nameOf("Roman")).withRandomPartyId().build());
        addressesTestSupport.thereIsAddressOf(someGeoAddressFor(romanTheContractorFromWarsaw.id(), "WARSAW"));
        partyRelationshipTestSupport.thereIsARelationBetween(firstProvider, ORDERER, romanTheContractorFromWarsaw, CONTRACTOR, CONTRACT);

        Party peterTheEmployeeFromKrakow = testSupport.thereIs(somePerson().with(nameOf("Peter")).withRandomPartyId().build());
        addressesTestSupport.thereIsAddressOf(someGeoAddressFor(peterTheEmployeeFromKrakow.id(), "KRAKOW"));
        partyRelationshipTestSupport.thereIsARelationBetween(firstProvider, EMPLOYER, peterTheEmployeeFromKrakow, EMPLOYEE, EMPLOYMENT);

        //and - second provider graph
        Party secondProvider = testSupport.thereIs(someCompany().with(OrganizationName.of("SECOND Inc.")).withRandomPartyId().with(PROVIDER).build());

        Party adamTheEmployeeFromWarsaw = testSupport.thereIs(somePerson().with(nameOf("Adam")).withRandomPartyId().build());
        addressesTestSupport.thereIsAddressOf(someGeoAddressFor(adamTheEmployeeFromWarsaw.id(), "WARSAW"));
        partyRelationshipTestSupport.thereIsARelationBetween(secondProvider, EMPLOYER, adamTheEmployeeFromWarsaw, EMPLOYEE, EMPLOYMENT);

        Party chrisTheEmployeeFromLublin = testSupport.thereIs(somePerson().with(nameOf("Chris")).withRandomPartyId().build());
        addressesTestSupport.thereIsAddressOf(someGeoAddressFor(chrisTheEmployeeFromLublin.id(), "LUBLIN"));
        partyRelationshipTestSupport.thereIsARelationBetween(secondProvider, EMPLOYER, chrisTheEmployeeFromLublin, EMPLOYEE, EMPLOYMENT);

        Party mikeTheEmployeeBdg = testSupport.thereIs(somePerson().with(nameOf("Mike")).withRandomPartyId().build());
        addressesTestSupport.thereIsAddressOf(someGeoAddressFor(mikeTheEmployeeBdg.id(), "BDG"));
        partyRelationshipTestSupport.thereIsARelationBetween(secondProvider, EMPLOYER, mikeTheEmployeeBdg, EMPLOYEE, EMPLOYMENT);

        //and - partner graph
        Party partner = testSupport.thereIs(someCompany().with(OrganizationName.of("PARTNER Inc.")).withRandomPartyId().with(PARTNER).build());

        Party johnTheEmployeeFromWarsaw = testSupport.thereIs(somePerson().with(nameOf("John")).withRandomPartyId().build());
        addressesTestSupport.thereIsAddressOf(someGeoAddressFor(johnTheEmployeeFromWarsaw.id(), "WARSAW"));
        partyRelationshipTestSupport.thereIsARelationBetween(partner, EMPLOYER, johnTheEmployeeFromWarsaw, EMPLOYEE, EMPLOYMENT);

        //when searching for an employee of our provider who lives in Warsaw
        PartySearch.Search search = new PartySearch.Search(
                party -> party.roles().contains(PROVIDER),
                rel -> rel.name().equals(EMPLOYMENT),
                address -> address instanceof GeoAddress && ((GeoAddress.GeoAddressDetails) address.addressDetails()).city().equals("WARSAW")
        );
        List<Party> result = partySearch.findMatching(search);

        //then
        assertEquals(1, result.size());
        assertEquals(adamTheEmployeeFromWarsaw, result.getFirst());
    }

}