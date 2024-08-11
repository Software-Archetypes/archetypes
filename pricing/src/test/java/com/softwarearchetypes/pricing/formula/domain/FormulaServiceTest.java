package com.softwarearchetypes.pricing.formula.domain;

import com.softwarearchetypes.pricing.common.ResultAssert;
import com.softwarearchetypes.pricing.formula.command.CreateFormulaCommand;
import com.softwarearchetypes.pricing.shared.AbstractUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FormulaServiceTest extends AbstractUnitTest {

    private FormulaService facade;

    @BeforeEach
    void setUp() {

        var repository = new FormulaJpaInMemoryRepository();
        facade = new FormulaService(
                repository,
                clock
        );
    }

    @Test
    @DisplayName("when creating valid formula with matching input data type then result should be success")
    void createValidFormula() {
        //given: simple record that will contain formula input data
        record TestFormulaData(int x, int y) {
        }

        //and: formula that multiplies x * y
        var formula = "{\"*\":[{ \"var\" : [\"x\"] }, { \"var\" : [\"y\"] }]}";

        //and: create formula command
        var createFormulaCommand = new CreateFormulaCommand(
                "Test formula",
                formula,
                TestFormulaData.class
        );

        //when: formula is created
        var formulaCreationResult = facade.createFormula(createFormulaCommand);

        //then: formula was stored successfully
        new ResultAssert(formulaCreationResult).isSuccess();
    }

    @Test
    @DisplayName("when creating valid formula but input data type is missing fields required by formula then result should be failure")
    void createValidFormulaWithInvalidDataInputType() {
        //given: simple record that will contain formula input data but with different fields names (a,b instead of x,y)
        record TestFormulaData(int a, int b) {
        }

        //and: formula that multiplies x * y
        var formula = "{\"*\":[{ \"var\" : [\"x\"] }, { \"var\" : [\"y\"] }]}";

        //and: create formula command
        var createFormulaCommand = new CreateFormulaCommand(
                "Test formula",
                formula,
                TestFormulaData.class
        );

        //when: formula is created
        var formulaCreationResult = facade.createFormula(createFormulaCommand);

        //then: result is failure
        new ResultAssert(formulaCreationResult).isFailure();
    }

    @Test
    @DisplayName("when creating invalid formula then result should be failure")
    void createInvalidFormula() {
        //given: simple record that will contain formula input data
        record TestFormulaData(int x, int y) {
        }

        //and: formula that multiplies x * y but with syntax error (typo in 'var')
        var formula = "{\"*\":[{ \"var\" : [\"x\"] }, { \"va\" : [\"y\"] }]}";

        //and: create formula command
        var createFormulaCommand = new CreateFormulaCommand(
                "Test formula",
                formula,
                TestFormulaData.class
        );

        //when: formula is created
        var formulaCreationResult = facade.createFormula(createFormulaCommand);

        //then: result is failure
        new ResultAssert(formulaCreationResult).isFailure();
    }

}