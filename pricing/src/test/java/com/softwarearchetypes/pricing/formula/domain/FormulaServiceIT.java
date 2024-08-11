package com.softwarearchetypes.pricing.formula.domain;

import com.softwarearchetypes.pricing.common.ResultAssert;
import com.softwarearchetypes.pricing.formula.command.CreateFormulaCommand;
import com.softwarearchetypes.pricing.shared.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

class FormulaServiceIT extends AbstractIntegrationTest {

    @Autowired
    private FormulaService formulaService;

    @Test
    @DisplayName("Proper formula should be stored and retrieved from DB")
    void properFormulaShouldBeStoredAndRetrievedFromDB() throws PriceCalculationException {
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
        var formulaCreationResult = formulaService.createFormula(createFormulaCommand);

        //then: formula was stored successfully
        new ResultAssert(formulaCreationResult).isSuccess();

        //when: formula is queried and executed for parameters: 2, 3
        var formulaPricing = formulaService.findById(formulaCreationResult.getSuccess())
                .map(FormulaPricingAssert::new)
                .orElse(new FormulaPricingAssert(null));

        var formulaTestData = new TestFormulaData(2, 3);

        //then: price is 6
        formulaPricing.formulaExists()
                .formulaResult(formulaTestData)
                .hasValue(BigDecimal.valueOf(6));
    }


}