package com.softwarearchetypes.party;

import java.util.List;
import java.util.Set;

/**
 * Policy that determines whether a given RegisteredIdentifier can be assigned to a given Party.
 * Allows enforcing business rules like "PESEL only for persons" or "REGON only for organizations".
 */
interface RegisteredIdentifierDefiningPolicy {

    /**
     * Checks if the given identifier can be assigned to the given party.
     *
     * @param party      the party to check
     * @param identifier the identifier to be assigned
     * @return true if assignment is allowed, false otherwise
     */
    boolean canRegister(Party party, RegisteredIdentifier identifier);

    /**
     * Default policy that allows all identifiers for all parties.
     * Useful for testing or when no restrictions are needed.
     */
    static RegisteredIdentifierDefiningPolicy allowAll() {
        return new AllowAllIdentifiersPolicy();
    }

    /**
     * Policy for personal identifiers (PESEL, Passport) - only assignable to Person.
     */
    static RegisteredIdentifierDefiningPolicy personalIdentifiersOnlyForPersons() {
        return new PersonalIdentifiersOnlyForPersonsPolicy();
    }

    /**
     * Policy for organizational identifiers (REGON, KRS) - only assignable to Organization.
     */
    static RegisteredIdentifierDefiningPolicy organizationalIdentifiersOnlyForOrganizations() {
        return new OrganizationalIdentifiersOnlyForOrganizationsPolicy();
    }

    /**
     * Combines multiple policies with AND logic.
     * All policies must return true for the identifier to be allowed.
     */
    static RegisteredIdentifierDefiningPolicy composite(RegisteredIdentifierDefiningPolicy... policies) {
        return new CompositeIdentifierPolicy(policies);
    }

    /**
     * Combines all standard policies:
     * - Personal identifiers (PESEL, Passport) only for Person
     * - Organizational identifiers (REGON, KRS) only for Organization
     * - Tax numbers (NIP) for both Person and Organization
     */
    static RegisteredIdentifierDefiningPolicy all() {
        return composite(
                personalIdentifiersOnlyForPersons(),
                organizationalIdentifiersOnlyForOrganizations()
        );
    }

    /**
     * Default policy with standard business rules.
     */
    RegisteredIdentifierDefiningPolicy DEFAULT = all();
}

/**
 * Policy that allows all identifiers for all parties.
 */
class AllowAllIdentifiersPolicy implements RegisteredIdentifierDefiningPolicy {

    @Override
    public boolean canRegister(Party party, RegisteredIdentifier identifier) {
        return true;
    }
}

/**
 * Policy that restricts personal identifiers to Person only.
 * Personal identifiers: PERSONAL_IDENTIFICATION_NUMBER (PESEL), PASSPORT
 */
class PersonalIdentifiersOnlyForPersonsPolicy implements RegisteredIdentifierDefiningPolicy {

    private static final Set<String> PERSONAL_IDENTIFIER_TYPES = Set.of(
            "PERSONAL_IDENTIFICATION_NUMBER",
            "PASSPORT"
    );

    @Override
    public boolean canRegister(Party party, RegisteredIdentifier identifier) {
        if (PERSONAL_IDENTIFIER_TYPES.contains(identifier.type())) {
            return party instanceof Person;
        }
        return true; // Not a personal identifier, allow it
    }
}

/**
 * Policy that restricts organizational identifiers to Organization only.
 * Organizational identifiers: REGON, KRS (Polish company registers)
 */
class OrganizationalIdentifiersOnlyForOrganizationsPolicy implements RegisteredIdentifierDefiningPolicy {

    private static final Set<String> ORGANIZATIONAL_IDENTIFIER_TYPES = Set.of(
            "REGON",
            "KRS"
    );

    @Override
    public boolean canRegister(Party party, RegisteredIdentifier identifier) {
        if (ORGANIZATIONAL_IDENTIFIER_TYPES.contains(identifier.type())) {
            return party instanceof Organization;
        }
        return true; // Not an organizational identifier, allow it
    }
}

/**
 * Composite policy that combines multiple policies with AND logic.
 * All policies must return true for the identifier to be allowed.
 */
class CompositeIdentifierPolicy implements RegisteredIdentifierDefiningPolicy {

    private final List<RegisteredIdentifierDefiningPolicy> policies;

    CompositeIdentifierPolicy(RegisteredIdentifierDefiningPolicy... policies) {
        this.policies = List.of(policies);
    }

    @Override
    public boolean canRegister(Party party, RegisteredIdentifier identifier) {
        for (RegisteredIdentifierDefiningPolicy policy : policies) {
            if (!policy.canRegister(party, identifier)) {
                return false; // Fail fast
            }
        }
        return true;
    }
}
