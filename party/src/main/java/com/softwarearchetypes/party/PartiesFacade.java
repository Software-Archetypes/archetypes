package com.softwarearchetypes.party;

import java.util.Set;
import java.util.function.Supplier;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.common.Version;
import com.softwarearchetypes.party.events.EventPublisher;
import com.softwarearchetypes.party.events.IncorrectPartyTypeIdentified;
import com.softwarearchetypes.party.events.PartyNotFound;
import com.softwarearchetypes.party.events.PartyRegistrationFailed;
import com.softwarearchetypes.party.events.PartyRelatedFailureEvent;
import com.softwarearchetypes.party.events.RegisteredIdentifierAdditionFailed;
import com.softwarearchetypes.party.events.RegisteredIdentifierRemovalFailed;

/**
 * The class serves as a facade for the Party Management module, providing a unified interface
 * for managing parties (e.g., individuals, organizations) within the system. It simplifies interaction
 * with the underlying logic by offering methods for registering, updating, and managing parties, roles,
 * and identifiers. This class is designed to support diverse use cases and enforce business policies.
 *
 * ### Features:
 * - Registration of new parties with varying levels of complexity.
 * - Updating data for existing parties, including attributes, roles, and identifiers.
 * - Managing unique identifiers and enforcing policy compliance.
 *
 * ### Methods Overview:
 *
 * #### Registration Methods:
 * These methods handle the registration process for parties:
 *
 * 1. **Complex Registration**:
 *    - May require verification steps, such as validating a phone number or email address.
 *    - Could involve sending notifications, like an activation code, to the user for confirmation.
 *
 * 2. **Simple Registration**:
 *    - For specific cases, such as when a company is imported from a CRM system, verification steps
 *      might not be necessary. Minimal validation is performed.
 *
 * 3. **Unique Identifier Verification**:
 *    - Based on organizational policies, these methods can enforce the uniqueness of a "registered identifier"
 *      (e.g., email or username) to ensure it is not already in use.
 *
 * 4. **Mandatory Address Requirement**:
 *    - Enforces requirements for providing specific types of contact information during registration.
 *    - For instance, a confirmed phone number might be mandatory for a party, but not necessarily for an organization.
 *    - The system can support the creation of multiple aggregates to accommodate diverse data models.
 *
 * #### Update Methods:
 * These methods allow for modifying existing parties' data, roles, and identifiers:
 *
 * 1. **Updating Party Data**:
 *    - Enables modification of basic attributes, such as name, or metadata.
 *    - May trigger validations or workflows, depending on the type of data being updated.
 *
 * 2. **Role Management**:
 *    - Supports adding, removing, or updating roles assigned to a party.
 *    - Roles can define the behavior or permissions of the party within the system.
 *
 * 3. **Identifier Management**:
 *    - Manages unique identifiers associated with a party, such as emails, phone numbers, or external system IDs.
 *    - Allows for adding new identifiers, modifying existing ones, or marking identifiers as deprecated or invalid.
 *    - Ensures compliance with policies requiring identifier uniqueness or specific verification processes.
 **
 */
//tx required
class PartiesFacade {

    private final PartyRepository partyRepository;
    private final EventPublisher eventPublisher;

    private final Supplier<PartyId> newPartyIdSupplier;

    PartiesFacade(PartyRepository partyRepository, EventPublisher eventPublisher, Supplier<PartyId> newPartyIdSupplier) {
        this.partyRepository = partyRepository;
        this.eventPublisher = eventPublisher;
        this.newPartyIdSupplier = newPartyIdSupplier != null ? newPartyIdSupplier : PartyId::random;
    }

    Result<PartyRelatedFailureEvent, Person> registerPersonFor(PersonalData personalData, Set<Role> roles, Set<RegisteredIdentifier> registeredIdentifiers) {
        return registerPartyAccordingTo(() -> new Person(newPartyIdSupplier.get(), personalData, roles, registeredIdentifiers, Version.initial())).map(Person.class::cast);
    }

    Result<PartyRelatedFailureEvent, Company> registerCompanyFor(OrganizationName organizationName, Set<Role> roles, Set<RegisteredIdentifier> registeredIdentifiers) {
        return registerPartyAccordingTo(() -> new Company(newPartyIdSupplier.get(), organizationName, roles, registeredIdentifiers, Version.initial())).map(Company.class::cast);
    }

    Result<PartyRelatedFailureEvent, OrganizationUnit> registerOrganizationUnitFor(OrganizationName organizationName, Set<Role> roles, Set<RegisteredIdentifier> registeredIdentifiers) {
        return registerPartyAccordingTo(() -> new OrganizationUnit(newPartyIdSupplier.get(), organizationName, roles, registeredIdentifiers, Version.initial())).map(OrganizationUnit.class::cast);
    }

    Result<PartyRelatedFailureEvent, Party> add(PartyId partyId, Role role) {
        return partyRepository.findBy(partyId)
                              .map(party -> party.add(role))
                              .map(party -> party.mapFailure(PartyRelatedFailureEvent.class::cast))
                              .orElse(Result.failure(new PartyNotFound(partyId.asString())))
                              .peekSuccess(partyRepository::save)
                              .peekSuccess(party -> eventPublisher.publish(party.publishedEvents()));
    }

    Result<PartyRelatedFailureEvent, Party> remove(PartyId partyId, Role role) {
        return partyRepository.findBy(partyId)
                              .map(party -> party.remove(role))
                              .map(party -> party.mapFailure(PartyRelatedFailureEvent.class::cast))
                              .orElse(Result.failure(new PartyNotFound(partyId.asString())))
                              .peekSuccess(partyRepository::save)
                              .peekSuccess(party -> eventPublisher.publish(party.publishedEvents()));
    }

    Result<PartyRelatedFailureEvent, Party> add(PartyId partyId, RegisteredIdentifier identifier) {
        return partyRepository.findBy(partyId)
                              .map(party -> party.add(identifier))
                              .map(party -> party.mapFailure(PartyRelatedFailureEvent.class::cast))
                              .orElse(Result.failure(new RegisteredIdentifierAdditionFailed(partyId.asString(), identifier.asString(), "PARTY_NOT_FOUND")))
                              .peekSuccess(partyRepository::save)
                              .peekSuccess(party -> eventPublisher.publish(party.publishedEvents()));
    }

    Result<PartyRelatedFailureEvent, Party> remove(PartyId partyId, RegisteredIdentifier identifier) {
        return partyRepository.findBy(partyId)
                              .map(party -> party.remove(identifier))
                              .map(party -> party.mapFailure(PartyRelatedFailureEvent.class::cast))
                              .orElse(Result.failure(new RegisteredIdentifierRemovalFailed(partyId.asString(), identifier.asString(), "PARTY_NOT_FOUND")))
                              .peekSuccess(partyRepository::save)
                              .peekSuccess(party -> eventPublisher.publish(party.publishedEvents()));
    }

    Result<PartyRelatedFailureEvent, Person> update(PartyId partyId, PersonalData personalData) {
        return partyRepository.findBy(partyId, Person.class)
                              .map(Person.class::cast)
                              .map(party -> party.update(personalData))
                              .map(party -> party.mapFailure(PartyRelatedFailureEvent.class::cast))
                              .orElse(Result.failure(new IncorrectPartyTypeIdentified(partyId.asString(), "Person")))
                              .peekSuccess(partyRepository::save)
                              .peekSuccess(party -> eventPublisher.publish(party.publishedEvents()));
    }

    Result<PartyRelatedFailureEvent, Organization> update(PartyId partyId, OrganizationName organizationName) {
        return partyRepository.findBy(partyId, Organization.class)
                              .map(Organization.class::cast)
                              .map(party -> party.update(organizationName))
                              .map(party -> party.mapFailure(PartyRelatedFailureEvent.class::cast))
                              .orElse(Result.failure(new IncorrectPartyTypeIdentified(partyId.asString(), "Organization")))
                              .peekSuccess(partyRepository::save)
                              .peekSuccess(party -> eventPublisher.publish(party.publishedEvents()));
    }

    private Result<PartyRelatedFailureEvent, Party> registerPartyAccordingTo(Supplier<Party> partySupplier) {
        try {
            Party party = partySupplier.get();
            partyRepository.save(party);
            eventPublisher.publish(party.toPartyRegisteredEvent());
            return Result.success(party);
        } catch (Exception ex) {
            return Result.failure(PartyRegistrationFailed.dueTo(ex));
        }
    }

}
