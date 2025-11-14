package com.softwarearchetypes.party;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.springframework.data.util.Pair;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.party.events.PartyRelationshipDefinitionFailed;
import com.softwarearchetypes.party.events.PartyRoleDefinitionFailed;

import static com.softwarearchetypes.party.events.PartyRelationshipDefinitionFailed.dueToPoliciesNotMet;

class PartyRelationshipBuilder {

    private static final BiFunction<PartyRelationshipDefinitionFailed, PartyRelationshipDefinitionFailed, PartyRelationshipDefinitionFailed> ANY_FAILURE = (fromFailure, toFailure) -> fromFailure != null ? fromFailure : toFailure;
    private PartyRoleDefiningPolicy partyRoleDefiningPolicy = PartyRoleDefiningPolicy.alwaysAllow();
    private PartyRelationshipDefiningPolicy partyRelationshipDefiningPolicy = PartyRelationshipDefiningPolicy.alwaysAllow();
    private Supplier<PartyRelationshipId> partyRelationshipIdSupplier = PartyRelationshipId::random;
    //can be replaced with partyQueries when moving away from Party
    private PartyRepository partyRepository;

    private RelationshipName name;
    private PartyId fromPartyId;
    private Role fromRole;
    private PartyId toPartyId;
    private Role toRole;
    private Validity validity = Validity.ALWAYS;

    PartyRelationshipBuilder(PartyRepository partyRepository) {
        this.partyRepository = partyRepository;
    }

    PartyRelationshipBuilder(PartyRepository partyRepository, Supplier<PartyRelationshipId> partyRelationshipIdSupplier) {
        this.partyRepository = partyRepository;
        this.partyRelationshipIdSupplier = partyRelationshipIdSupplier;
    }

    PartyRelationshipBuilder(PartyRepository partyRepository, Supplier<PartyRelationshipId> partyRelationshipIdSupplier,
                             PartyRoleDefiningPolicy partyRoleDefiningPolicy, PartyRelationshipDefiningPolicy partyRelationshipDefiningPolicy) {
        this.partyRepository = partyRepository;
        this.partyRelationshipIdSupplier = partyRelationshipIdSupplier;
        this.partyRoleDefiningPolicy = partyRoleDefiningPolicy;
        this.partyRelationshipDefiningPolicy = partyRelationshipDefiningPolicy;
    }

    PartyRelationshipBuilder from(PartyId partyId, Role role) {
        this.fromPartyId = partyId;
        this.fromRole = role;
        return this;
    }

    PartyRelationshipBuilder from(PartyId partyId, String role) {
        return from(partyId, Role.of(role));
    }

    PartyRelationshipBuilder to(PartyId partyId, Role role) {
        this.toPartyId = partyId;
        this.toRole = role;
        return this;
    }

    PartyRelationshipBuilder to(PartyId partyId, String role) {
        return to(partyId, Role.of(role));
    }

    PartyRelationshipBuilder named(RelationshipName name) {
        this.name = name;
        return this;
    }

    PartyRelationshipBuilder named(String name) {
        return named(RelationshipName.of(name));
    }

    PartyRelationshipBuilder withValidity(Validity validity) {
        this.validity = validity;
        return this;
    }

    PartyRelationshipBuilder withRolePolicy(PartyRoleDefiningPolicy partyRoleDefiningPolicy) {
        this.partyRoleDefiningPolicy = partyRoleDefiningPolicy;
        return this;
    }

    PartyRelationshipBuilder withRelationshipPolicy(PartyRelationshipDefiningPolicy partyRelationshipDefiningPolicy) {
        this.partyRelationshipDefiningPolicy = partyRelationshipDefiningPolicy;
        return this;
    }

    PartyRelationshipBuilder withIdSupplier(Supplier<PartyRelationshipId> partyRelationshipIdSupplier) {
        this.partyRelationshipIdSupplier = partyRelationshipIdSupplier;
        return this;
    }

    Result<PartyRelationshipDefinitionFailed, PartyRelationship> build() {
        Result<PartyRelationshipDefinitionFailed, PartyRole> fromParty = definePartyRoleFor(fromPartyId, fromRole);
        Result<PartyRelationshipDefinitionFailed, PartyRole> toParty = definePartyRoleFor(toPartyId, toRole);
        return fromParty.combine(toParty, ANY_FAILURE, Pair::of)
                 .flatMap(rolesPair -> defineRelationFor(rolesPair.getFirst(), rolesPair.getSecond(), name, validity));
    }

    private Result<PartyRelationshipDefinitionFailed, PartyRole> definePartyRoleFor(PartyId toId, Role toRole) {
        return partyRepository.findBy(toId)
                              .map(party -> defineRoleFor(party, toRole))
                              .map(party -> party.mapFailure(failure -> PartyRelationshipDefinitionFailed.dueTo(failure.reason())))
                              .orElse(Result.failure(new PartyRelationshipDefinitionFailed("PARTY_NOT_FOUND")));
    }

    private  Result<PartyRoleDefinitionFailed, PartyRole> defineRoleFor(Party party, Role role) {
        if (partyRoleDefiningPolicy.canDefineFor(party, role)) {
            return Result.success(PartyRole.of(party.id(), role));
        } else {
            return Result.failure(PartyRoleDefinitionFailed.dueToPoliciesNotMet());
        }
    }

    Result<PartyRelationshipDefinitionFailed, PartyRelationship> defineRelationFor(PartyRole from, PartyRole to, RelationshipName name, Validity validity) {
        if (partyRelationshipDefiningPolicy.canDefineFor(from, to, name)) {
            return Result.success(PartyRelationship.from(partyRelationshipIdSupplier.get(), from, to, name, validity));
        } else {
            return Result.failure(dueToPoliciesNotMet());
        }
    }
}
