package com.softwarearchetypes.pricing.formula;

import com.softwarearchetypes.pricing.AbstractIntegrationTest;
import com.softwarearchetypes.pricing.common.BigDecimalAssert;
import com.softwarearchetypes.pricing.common.ResultAssert;
import com.softwarearchetypes.pricing.formula.command.CreateFormulaCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

class FormulaFacadeIT extends AbstractIntegrationTest {

    @Autowired
    private FormulaFacade formulaFacade;

    @Autowired
    private FormulaQueryRepository queryRepository;

    @Test
    @DisplayName("Proper formula should be stored and retrieved from DB")
    void properFormulaShouldBeStoredAndRetrievedFromDB() {
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
        var formulaCreationResult = formulaFacade.createFormula(createFormulaCommand);

        //then: formula was stored successfully
        new ResultAssert(formulaCreationResult).isSuccess();

        //when: formula is queried
        var formulaPricing = queryRepository.getFormulaById(formulaCreationResult.getSuccess());

        //then: formula is executed properly
        var formulaTestData = new TestFormulaData(2, 3);
        var formulaResult = new BigDecimalAssert(formulaPricing.calculatePrice(formulaTestData));

        //and: result is 6
        formulaResult.hasValue(BigDecimal.valueOf(6));
    }


}