package com.softwarearchetypes.party;

import java.util.function.Supplier;

public class PartyRelationshipBuilderFactory {

    private final PartyRepository partyRepository;
    private final Supplier<PartyRelationshipId> partyRelationshipIdSupplier;
    private final PartyRoleDefiningPolicy partyRoleDefiningPolicy;
    private final PartyRelationshipDefiningPolicy partyRelationshipDefiningPolicy;

    PartyRelationshipBuilderFactory(PartyRepository partyRepository) {
        this(partyRepository, PartyRelationshipId::random, PartyRoleDefiningPolicy.alwaysAllow(), PartyRelationshipDefiningPolicy.alwaysAllow());
    }

    PartyRelationshipBuilderFactory(PartyRepository partyRepository, Supplier<PartyRelationshipId> partyRelationshipIdSupplier) {
        this(partyRepository, partyRelationshipIdSupplier, PartyRoleDefiningPolicy.alwaysAllow(), PartyRelationshipDefiningPolicy.alwaysAllow());
    }

    PartyRelationshipBuilderFactory(PartyRepository partyRepository, Supplier<PartyRelationshipId> partyRelationshipIdSupplier,
                                    PartyRoleDefiningPolicy partyRoleDefiningPolicy, PartyRelationshipDefiningPolicy partyRelationshipDefiningPolicy) {
        this.partyRepository = partyRepository;
        this.partyRelationshipIdSupplier = partyRelationshipIdSupplier;
        this.partyRoleDefiningPolicy = partyRoleDefiningPolicy;
        this.partyRelationshipDefiningPolicy = partyRelationshipDefiningPolicy;
    }

    public PartyRelationshipBuilder newRelationship() {
        return new PartyRelationshipBuilder(partyRepository, partyRelationshipIdSupplier, partyRoleDefiningPolicy, partyRelationshipDefiningPolicy);
    }
}
