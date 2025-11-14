package com.softwarearchetypes.party;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.party.events.RegisteredIdentifierAdditionFailed;
import org.junit.jupiter.api.Test;

import static com.softwarearchetypes.party.PartyFixture.someCompany;
import static com.softwarearchetypes.party.PartyFixture.somePerson;
import static org.junit.jupiter.api.Assertions.*;

class RegisteredIdentifierDefiningPolicyTest {

    @Test
    void allowAllPolicyShouldAllowAnyIdentifierForAnyParty() {
        //given
        RegisteredIdentifierDefiningPolicy policy = RegisteredIdentifierDefiningPolicy.allowAll();
        Person person = somePerson().withRandomPartyId().build();
        Company company = someCompany().withRandomPartyId().build();

        PersonalIdentificationNumber pesel = new PersonalIdentificationNumber("44051401359");
        TaxNumber taxNumber = new TaxNumber("1234563218");
        Passport passport = new Passport("AB1234567", Validity.ALWAYS);

        //expect - all combinations allowed
        assertTrue(policy.canRegister(person, pesel));
        assertTrue(policy.canRegister(person, taxNumber));
        assertTrue(policy.canRegister(person, passport));
        assertTrue(policy.canRegister(company, pesel));
        assertTrue(policy.canRegister(company, taxNumber));
        assertTrue(policy.canRegister(company, passport));
    }

    @Test
    void personalIdentifiersOnlyForPersonsPolicyShouldRestrictPeselToPersons() {
        //given
        RegisteredIdentifierDefiningPolicy policy = RegisteredIdentifierDefiningPolicy.personalIdentifiersOnlyForPersons();
        Person person = somePerson().withRandomPartyId().build();
        Company company = someCompany().withRandomPartyId().build();
        PersonalIdentificationNumber pesel = new PersonalIdentificationNumber("44051401359");

        //expect
        assertTrue(policy.canRegister(person, pesel));
        assertFalse(policy.canRegister(company, pesel));
    }

    @Test
    void personalIdentifiersOnlyForPersonsPolicyShouldRestrictPassportToPersons() {
        //given
        RegisteredIdentifierDefiningPolicy policy = RegisteredIdentifierDefiningPolicy.personalIdentifiersOnlyForPersons();
        Person person = somePerson().withRandomPartyId().build();
        Company company = someCompany().withRandomPartyId().build();
        Passport passport = new Passport("AB1234567", Validity.ALWAYS);

        //expect
        assertTrue(policy.canRegister(person, passport));
        assertFalse(policy.canRegister(company, passport));
    }

    @Test
    void personalIdentifiersOnlyForPersonsPolicyShouldAllowTaxNumberForBoth() {
        //given
        RegisteredIdentifierDefiningPolicy policy = RegisteredIdentifierDefiningPolicy.personalIdentifiersOnlyForPersons();
        Person person = somePerson().withRandomPartyId().build();
        Company company = someCompany().withRandomPartyId().build();
        TaxNumber taxNumber = new TaxNumber("1234563218");

        //expect - tax number is not a personal identifier
        assertTrue(policy.canRegister(person, taxNumber));
        assertTrue(policy.canRegister(company, taxNumber));
    }

    @Test
    void allPolicyShouldCombineAllStandardRestrictions() {
        //given
        RegisteredIdentifierDefiningPolicy policy = RegisteredIdentifierDefiningPolicy.all();
        Person person = somePerson().withRandomPartyId().build();
        Company company = someCompany().withRandomPartyId().build();

        PersonalIdentificationNumber pesel = new PersonalIdentificationNumber("44051401359");
        TaxNumber taxNumber = new TaxNumber("1234563218");
        Passport passport = new Passport("AB1234567", Validity.ALWAYS);

        //expect - person can have personal identifiers and tax number
        assertTrue(policy.canRegister(person, pesel));
        assertTrue(policy.canRegister(person, passport));
        assertTrue(policy.canRegister(person, taxNumber));

        //expect - company can only have tax number, not personal identifiers
        assertFalse(policy.canRegister(company, pesel));
        assertFalse(policy.canRegister(company, passport));
        assertTrue(policy.canRegister(company, taxNumber));
    }

    @Test
    void constructorShouldRejectPeselForCompanyWithDefaultPolicy() {
        //given
        PersonalIdentificationNumber pesel = new PersonalIdentificationNumber("44051401359");

        //expect - default policy in Party uses all() which restricts PESEL to Person
        assertThrows(IllegalArgumentException.class, () ->
            someCompany().withRandomPartyId()
                .with(pesel)
                .build()
        );
    }

    @Test
    void constructorShouldAcceptPeselForPersonWithDefaultPolicy() {
        //given
        PersonalIdentificationNumber pesel = new PersonalIdentificationNumber("44051401359");

        //when
        Person person = somePerson().withRandomPartyId()
            .with(pesel)
            .build();

        //then
        assertTrue(person.registeredIdentifiers().contains(pesel));
    }

    @Test
    void constructorShouldAcceptTaxNumberForCompanyWithDefaultPolicy() {
        //given
        TaxNumber taxNumber = new TaxNumber("1234563218");

        //when
        Company company = someCompany().withRandomPartyId()
            .with(taxNumber)
            .build();

        //then
        assertTrue(company.registeredIdentifiers().contains(taxNumber));
    }

    @Test
    void addShouldReturnFailureWhenPersonalIdentifierNotAllowedForCompany() {
        //given
        Company company = someCompany().withRandomPartyId().build();
        PersonalIdentificationNumber pesel = new PersonalIdentificationNumber("44051401359");

        //when
        Result<RegisteredIdentifierAdditionFailed, Party> result = company.add(pesel);

        //then
        assertTrue(result.failure());
        assertEquals("IDENTIFIER_NOT_ALLOWED_FOR_PARTY_TYPE", result.getFailure().reason());
        assertFalse(company.registeredIdentifiers().contains(pesel));
    }

    @Test
    void addShouldSucceedWhenPersonalIdentifierAllowedForPerson() {
        //given
        Person person = somePerson().withRandomPartyId().build();
        PersonalIdentificationNumber pesel = new PersonalIdentificationNumber("44051401359");

        //when
        Result<RegisteredIdentifierAdditionFailed, Party> result = person.add(pesel);

        //then
        assertTrue(result.success());
        assertTrue(result.getSuccess().registeredIdentifiers().contains(pesel));
    }

    @Test
    void canRegisterShouldCheckPolicyCorrectly() {
        //given
        Person person = somePerson().withRandomPartyId().build();
        Company company = someCompany().withRandomPartyId().build();

        PersonalIdentificationNumber pesel = new PersonalIdentificationNumber("44051401359");
        TaxNumber taxNumber = new TaxNumber("1234563218");
        Passport passport = new Passport("AB1234567", Validity.ALWAYS);

        //expect - person can register all
        assertTrue(person.canRegister(pesel));
        assertTrue(person.canRegister(taxNumber));
        assertTrue(person.canRegister(passport));

        //expect - company can only register tax number
        assertFalse(company.canRegister(pesel));
        assertTrue(company.canRegister(taxNumber));
        assertFalse(company.canRegister(passport));
    }

    @Test
    void compositePolicyShouldCombineMultiplePoliciesWithAndLogic() {
        //given
        RegisteredIdentifierDefiningPolicy restrictivePolicy = RegisteredIdentifierDefiningPolicy.composite(
            RegisteredIdentifierDefiningPolicy.personalIdentifiersOnlyForPersons(),
            RegisteredIdentifierDefiningPolicy.organizationalIdentifiersOnlyForOrganizations()
        );

        Person person = somePerson().withRandomPartyId().build();
        Company company = someCompany().withRandomPartyId().build();
        PersonalIdentificationNumber pesel = new PersonalIdentificationNumber("44051401359");
        Passport passport = new Passport("AB1234567", Validity.ALWAYS);

        //expect - composite policy should restrict personal identifiers to persons only
        assertTrue(restrictivePolicy.canRegister(person, pesel));
        assertTrue(restrictivePolicy.canRegister(person, passport));
        assertFalse(restrictivePolicy.canRegister(company, pesel));
        assertFalse(restrictivePolicy.canRegister(company, passport));
    }
}
