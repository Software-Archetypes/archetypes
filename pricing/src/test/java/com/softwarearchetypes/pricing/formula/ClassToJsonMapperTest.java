package com.softwarearchetypes.pricing.formula;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassToJsonMapperTest {

    @Test
    @DisplayName("Should properly map test class to json structure")
    void shouldProperlyMapTestClass() throws JsonProcessingException {
        //given: test record
        record TestInputData(String a, int b, BigDecimal c) {
        }

        //and: expected JSON structure
        var expectedResult = """
                {
                  "a" : "",
                  "b" : 1,
                  "c" : 1
                }
                """;

        //when: test record is mapped to JSON
        var json = ClassToJsonMapper.mapClassToJson(TestInputData.class);

        //then: json have expected strucutre
        assertEquals(unifyString(expectedResult), unifyString(json));

    }

    private String unifyString(String value) {
        String systemLineSeparator = System.lineSeparator();
        return value.trim().replaceAll("\r\n|\r|\n", systemLineSeparator);
    }

}