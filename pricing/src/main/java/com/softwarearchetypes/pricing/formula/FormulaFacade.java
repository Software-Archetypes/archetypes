package com.softwarearchetypes.pricing.formula;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

            String inputDataJson = ClassToJsonMapper.mapClassToJson(command.inputDataType());
            var formulaTestResult = testFormula(command, inputDataJson);
            if (formulaTestResult.failure()) {
                return Result.failure(InvalidFormulaCreationCommand.dueToInvalidFormulaOrInputDataType(formulaTestResult.getFailure().getMessage()));
            }

            var formulaPricingEntity = new FormulaPricingEntity(
                    command.name(),
                    command.formula(),
                    command.inputDataType(),
                    inputDataJson,
                    clock
            );

            formulaPricingEntity = repository.save(formulaPricingEntity);

            return Result.success(formulaPricingEntity.getId());
        } catch (JsonProcessingException exception) {
            return Result.failure(InvalidFormulaCreationCommand.dueToErrorDuringParsingInputDataType());
        }
    }

    private Result<PriceCalculationException, Boolean> testFormula(CreateFormulaCommand command, String inputDataJson) throws JsonProcessingException {
        var testInputData = new ObjectMapper().readValue(inputDataJson, command.inputDataType());
        var basicFormula = new BasicFormula(command.formula(), command.inputDataType());

        try {
            basicFormula.calculatePrice(testInputData);
            return Result.success(true);
        } catch (PriceCalculationException e) {
            return Result.failure(e);
        }

    }


}
