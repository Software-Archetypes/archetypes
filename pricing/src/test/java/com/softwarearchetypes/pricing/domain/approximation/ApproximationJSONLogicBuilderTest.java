package com.softwarearchetypes.pricing.domain.approximation;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApproximationJSONLogicBuilderTest {

    @Test
    void shouldBuildJSONLogicIfMap() {
        //given: build map with two keys and json logic functions
        var builder = new ApproximationJSONLogicBuilder();

        String function1 = """
                {"+":[4,2]}""";
        builder.addIfCondition(BigDecimal.ONE, function1);

        String function2 = """
                {"-":[3,3]}""";
        builder.addIfCondition(BigDecimal.TEN, function2);

        //when
        var result = builder.build();

        //then
        var expectedResult = """
                {"if":[{"==":[{"var":"key"},1]},"{\\"+\\":[4,2]}",{"==":[{"var":"key"},10]},"{\\"-\\":[3,3]}"]}""";

        assertEquals(expectedResult, result);
    }

}