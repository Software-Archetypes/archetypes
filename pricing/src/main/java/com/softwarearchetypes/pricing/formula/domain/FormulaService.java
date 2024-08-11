package com.softwarearchetypes.pricing.formula.domain;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwarearchetypes.pricing.common.Result;
import com.softwarearchetypes.pricing.formula.command.CreateFormulaCommand;
import com.softwarearchetypes.pricing.formula.error.InvalidFormulaCreationCommand;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
public class FormulaService {

    private final FormulaRepository repository;
    private final Clock clock;

    FormulaService(FormulaRepository repository, Clock clock) {
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
                    OffsetDateTime.now(clock)
            );

            var formulaPricingId = repository.save(formulaPricingEntity);

            return Result.success(formulaPricingId);
        } catch (JsonProcessingException exception) {
            return Result.failure(InvalidFormulaCreationCommand.dueToErrorDuringParsingInputDataType());
        }
    }

    @Transactional(readOnly = true)
    public Optional<FormulaPricing> findById(UUID formulaPricingId) {
        return repository.findById(formulaPricingId)
                .map(formulaPricing -> new BasicFormula(
                        formulaPricing.formula(),
                        formulaPricing.inputDataClass()
                ));
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
