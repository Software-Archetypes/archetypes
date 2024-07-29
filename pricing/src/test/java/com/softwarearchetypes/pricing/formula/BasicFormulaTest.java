package com.softwarearchetypes.pricing.formula;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class BasicFormulaTest {

    @Test
    @DisplayName("Should execute simple formula")
    void shouldExecuteSimpleFormula() {
        //given: simple record to represent input data
        record FormulaData(int a, int b) {
        }

        //and: simple adding formula with two variables
        var basicFormula = new BasicFormula(
                "{\"+\": [{\"var\": \"a\"}, {\"var\": \"b\"}]}",
                FormulaData.class.getName()
        );

        //and: formula data: (2,2)
        var formulaData = new FormulaData(2, 2);

        //when: formula is executed
        var result = basicFormula.calculatePrice(formulaData);

        //then: the result is 4
        Assertions.assertEquals(0, BigDecimal.valueOf(4).compareTo(result));
    }

    @Test
    @DisplayName("Should throw exception when input data type do not match expected type")
    void shouldThrowExceptionWhenInputDataTypeDoNotMatchExpectedType() {
        //given: simple record to represent input data
        record ValidFormulaData(int a, int b) {
        }

        //and: simple adding formula with two variables
        var basicFormula = new BasicFormula(
                "{\"+\": [2,2]}",
                ValidFormulaData.class.getName()
        );

        //when: an invalid input data type is used
        record InvalidFormulaData() {
        }

        //then: exception is thrown
        Assertions.assertThrows(IllegalArgumentException.class, () -> basicFormula.calculatePrice(new InvalidFormulaData()));

    }

}