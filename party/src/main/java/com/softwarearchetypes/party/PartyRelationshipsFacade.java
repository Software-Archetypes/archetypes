package com.softwarearchetypes.party;

import java.util.function.BiFunction;

import org.springframework.data.util.Pair;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.party.events.EventPublisher;
import com.softwarearchetypes.party.events.PartyRelationshipDefinitionFailed;
import com.softwarearchetypes.party.events.PartyRelationshipRemoved;

//tx required
//TODO: docsy
//TODO: rozkmina jakie zapytania można budować + zależności pomiedzy klasami/modułami jak dojdzie uwierzytelnianie
public class PartyRelationshipsFacade {

    private static final BiFunction<PartyRelationshipDefinitionFailed, PartyRelationshipDefinitionFailed, PartyRelationshipDefinitionFailed> ANY_FAILURE = (fromFailure, toFailure) -> fromFailure != null ? fromFailure : toFailure;
    private final PartyRoleFactory partyRoleFactory;
    private final PartyRelationshipFactory partyRelationshipFactory;
    private final PartyRelationshipRepository repository;
    private final PartiesQueries partiesQueries;
    private final EventPublisher eventPublisher;

    PartyRelationshipsFacade(PartyRoleFactory partyRoleFactory, PartyRelationshipFactory partyRelationshipFactory, PartyRelationshipRepository repository, PartiesQueries partiesQueries, EventPublisher eventPublisher) {
        this.partyRoleFactory = partyRoleFactory;
        this.partyRelationshipFactory = partyRelationshipFactory;
        this.repository = repository;
        this.partiesQueries = partiesQueries;
        this.eventPublisher = eventPublisher;
    }

    //TODO: party could be just a ReadModel
    public Result<PartyRelationshipDefinitionFailed, PartyRelationship> assign(PartyId fromId, Role fromRole, PartyId toId, Role toRole, RelationshipName name) {

        //queries might come from external module, or from the same one. They can contain all info (including all relations),
        // but if graph is huge is better to search it online
        Result<PartyRelationshipDefinitionFailed, PartyRole> fromParty = definePartyRoleFor(fromId, fromRole);
        Result<PartyRelationshipDefinitionFailed, PartyRole> toParty = definePartyRoleFor(toId, toRole);

        return fromParty.combine(toParty, ANY_FAILURE, Pair::of)
                        .flatMap(rolesPair -> partyRelationshipFactory.defineFor(rolesPair.getFirst(), rolesPair.getSecond(), name))
                        //we should handle unique key (from, fromRole, to, toRole, relName) constraint violations here
                        .peekSuccess(repository::save)
                        .peekSuccess(relation -> eventPublisher.publish(relation.toPartyRelationshipAddedEvent()));
    }

    public Result<PartyRelationshipDefinitionFailed, PartyRelationshipId> remove(PartyRelationshipId partyRelationshipId) {
        repository.delete(partyRelationshipId)
                  .ifPresent(id -> eventPublisher.publish(new PartyRelationshipRemoved(id.asString())));
        return Result.success(partyRelationshipId);
    }

    private Result<PartyRelationshipDefinitionFailed, PartyRole> definePartyRoleFor(PartyId toId, Role toRole) {
        return partiesQueries.findBy(toId)
                             .map(party -> partyRoleFactory.defineFor(party, toRole))
                             .map(party -> party.mapFailure(failure -> PartyRelationshipDefinitionFailed.dueTo(failure.reason())))
                             .orElse(Result.failure(new PartyRelationshipDefinitionFailed("PARTY_NOT_FOUND")));
    }

}
