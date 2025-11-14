package com.softwarearchetypes.party;

import com.softwarearchetypes.common.Version;

import java.util.HashSet;
import java.util.Set;

/**
 * Fluent builder for creating Party instances.
 * Allows setting common party attributes before specializing to specific party type.
 *
 * Usage:
 * <pre>
 * Person person = PartyBuilder.newParty()
 *     .withPartyId(partyId)
 *     .with(role)
 *     .with(identifier)
 *     .asPerson()
 *     .with(personalData)
 *     .build();
 * </pre>
 */
public class PartyBuilder {

    private PartyId partyId;
    private final Set<Role> roles = new HashSet<>();
    private final Set<RegisteredIdentifier> registeredIdentifiers = new HashSet<>();
    private Version version = Version.initial();

    private PartyBuilder() {
    }

    public static PartyBuilder newParty() {
        return new PartyBuilder();
    }

    public PartyBuilder withPartyId(PartyId partyId) {
        this.partyId = partyId;
        return this;
    }

    public PartyBuilder withRandomPartyId() {
        this.partyId = PartyId.random();
        return this;
    }

    public PartyBuilder with(Role role) {
        this.roles.add(role);
        return this;
    }

    public PartyBuilder withRoles(Set<Role> roles) {
        this.roles.addAll(roles);
        return this;
    }

    public PartyBuilder with(RegisteredIdentifier identifier) {
        this.registeredIdentifiers.add(identifier);
        return this;
    }

    public PartyBuilder withRegisteredIdentifiers(Set<RegisteredIdentifier> identifiers) {
        this.registeredIdentifiers.addAll(identifiers);
        return this;
    }

    public PartyBuilder withVersion(Version version) {
        this.version = version;
        return this;
    }

    public PersonBuilder asPerson() {
        return new PersonBuilder(partyId, roles, registeredIdentifiers, version);
    }

    public CompanyBuilder asCompany() {
        return new CompanyBuilder(partyId, roles, registeredIdentifiers, version);
    }

    public OrganizationUnitBuilder asOrganizationUnit() {
        return new OrganizationUnitBuilder(partyId, roles, registeredIdentifiers, version);
    }

    /**
     * Builder for Person instances.
     */
    public static class PersonBuilder {
        private final PartyId partyId;
        private final Set<Role> roles;
        private final Set<RegisteredIdentifier> registeredIdentifiers;
        private final Version version;
        private PersonalData personalData = PersonalData.empty();

        private PersonBuilder(PartyId partyId, Set<Role> roles, Set<RegisteredIdentifier> registeredIdentifiers, Version version) {
            this.partyId = partyId;
            this.roles = roles;
            this.registeredIdentifiers = registeredIdentifiers;
            this.version = version;
        }

        public PersonBuilder with(PersonalData personalData) {
            this.personalData = personalData;
            return this;
        }

        public Person build() {
            return new Person(partyId, personalData, roles, registeredIdentifiers, version);
        }
    }

    /**
     * Builder for Company instances.
     */
    public static class CompanyBuilder {
        private final PartyId partyId;
        private final Set<Role> roles;
        private final Set<RegisteredIdentifier> registeredIdentifiers;
        private final Version version;
        private OrganizationName organizationName;

        private CompanyBuilder(PartyId partyId, Set<Role> roles, Set<RegisteredIdentifier> registeredIdentifiers, Version version) {
            this.partyId = partyId;
            this.roles = roles;
            this.registeredIdentifiers = registeredIdentifiers;
            this.version = version;
        }

        public CompanyBuilder with(OrganizationName organizationName) {
            this.organizationName = organizationName;
            return this;
        }

        public Company build() {
            return new Company(partyId, organizationName, roles, registeredIdentifiers, version);
        }
    }

    /**
     * Builder for OrganizationUnit instances.
     */
    public static class OrganizationUnitBuilder {
        private final PartyId partyId;
        private final Set<Role> roles;
        private final Set<RegisteredIdentifier> registeredIdentifiers;
        private final Version version;
        private OrganizationName organizationName;

        private OrganizationUnitBuilder(PartyId partyId, Set<Role> roles, Set<RegisteredIdentifier> registeredIdentifiers, Version version) {
            this.partyId = partyId;
            this.roles = roles;
            this.registeredIdentifiers = registeredIdentifiers;
            this.version = version;
        }

        public OrganizationUnitBuilder with(OrganizationName organizationName) {
            this.organizationName = organizationName;
            return this;
        }

        public OrganizationUnit build() {
            return new OrganizationUnit(partyId, organizationName, roles, registeredIdentifiers, version);
        }
    }
}
