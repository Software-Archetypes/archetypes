package com.softwarearchetypes.pricing.formula;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.softwarearchetypes.pricing.common.Result;
import com.softwarearchetypes.pricing.formula.command.CreateFormulaCommand;
import com.softwarearchetypes.pricing.formula.error.InvalidFormulaCreationCommand;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.UUID;

@Component
public class FormulaFacade {

    private final FormulaJpaRepository repository;
    private final Clock clock;

    FormulaFacade(FormulaJpaRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public Result<InvalidFormulaCreationCommand, UUID> createFormula(CreateFormulaCommand command) {

        try {
            var formulaPricingEntity = new FormulaPricingEntity(
                    command.name(),
                    command.formula(),
                    command.inputDataType(),
                    ClassToJsonMapper.mapClassToJson(command.inputDataType()),
                    clock
            );

            formulaPricingEntity = repository.save(formulaPricingEntity);

            return Result.success(formulaPricingEntity.getId());
        } catch (JsonProcessingException exception) {
            return Result.failure(InvalidFormulaCreationCommand.dueToErrorDuringParsingInputDataType());
        }
    }


}
