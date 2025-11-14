package com.softwarearchetypes.party;

import java.util.function.BiFunction;

import org.springframework.data.util.Pair;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.party.commands.AssignPartyRelationshipCommand;
import com.softwarearchetypes.party.commands.RemovePartyRelationshipCommand;
import com.softwarearchetypes.party.events.EventPublisher;
import com.softwarearchetypes.party.events.PartyRelationshipDefinitionFailed;
import com.softwarearchetypes.party.events.PartyRelationshipRemoved;

//tx required
public class PartyRelationshipsFacade {

    private static final BiFunction<PartyRelationshipDefinitionFailed, PartyRelationshipDefinitionFailed, PartyRelationshipDefinitionFailed> ANY_FAILURE = (fromFailure, toFailure) -> fromFailure != null ? fromFailure : toFailure;
    private final PartyRoleFactory partyRoleFactory;
    private final PartyRelationshipFactory partyRelationshipFactory;
    private final PartyRelationshipRepository repository;
    private final PartyRepository partyRepository;
    private final EventPublisher eventPublisher;

    PartyRelationshipsFacade(PartyRoleFactory partyRoleFactory, PartyRelationshipFactory partyRelationshipFactory, PartyRelationshipRepository repository, PartyRepository partyRepository, EventPublisher eventPublisher) {
        this.partyRoleFactory = partyRoleFactory;
        this.partyRelationshipFactory = partyRelationshipFactory;
        this.repository = repository;
        this.partyRepository = partyRepository;
        this.eventPublisher = eventPublisher;
    }

    public Result<PartyRelationshipDefinitionFailed, PartyRelationshipView> handle(AssignPartyRelationshipCommand command) {
        Role fromRole = Role.of(command.fromRole());
        Role toRole = Role.of(command.toRole());
        RelationshipName relationshipName = RelationshipName.of(command.relationshipName());

        //queries might come from external module, or from the same one. They can contain all info (including all relations),
        // but if graph is huge is better to search it online
        Result<PartyRelationshipDefinitionFailed, PartyRole> fromParty = definePartyRoleFor(command.fromPartyId(), fromRole);
        Result<PartyRelationshipDefinitionFailed, PartyRole> toParty = definePartyRoleFor(command.toPartyId(), toRole);

        return fromParty.combine(toParty, ANY_FAILURE, Pair::of)
                        .flatMap(rolesPair -> partyRelationshipFactory.defineFor(rolesPair.getFirst(), rolesPair.getSecond(), relationshipName))
                        //we should handle unique key (from, fromRole, to, toRole, relName) constraint violations here
                        .peekSuccess(repository::save)
                        .peekSuccess(relation -> eventPublisher.publish(relation.toPartyRelationshipAddedEvent()))
                        .map(PartyRelationshipViewMapper::toView);
    }

    public Result<PartyRelationshipDefinitionFailed, PartyRelationshipId> handle(RemovePartyRelationshipCommand command) {
        repository.delete(command.partyRelationshipId())
                  .ifPresent(id -> eventPublisher.publish(new PartyRelationshipRemoved(id.asString())));
        return Result.success(command.partyRelationshipId());
    }

    private Result<PartyRelationshipDefinitionFailed, PartyRole> definePartyRoleFor(PartyId toId, Role toRole) {
        return partyRepository.findBy(toId)
                             .map(party -> partyRoleFactory.defineFor(party, toRole))
                             .map(party -> party.mapFailure(failure -> PartyRelationshipDefinitionFailed.dueTo(failure.reason())))
                             .orElse(Result.failure(new PartyRelationshipDefinitionFailed("PARTY_NOT_FOUND")));
    }

}
