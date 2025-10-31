package com.softwarearchetypes.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PreconditionsTest {

    @Test
    void shouldNotThrowExceptionWhenCheckArgumentWithTrueExpression() {
        //given
        boolean expression = true;
        String errorMessage = "This should not be thrown";

        //when & then
        assertDoesNotThrow(() -> Preconditions.checkArgument(expression, errorMessage));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenCheckArgumentWithFalseExpression() {
        //given
        boolean expression = false;
        String errorMessage = "Expression must be true";

        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Preconditions.checkArgument(expression, errorMessage)
        );

        //then
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void shouldNotThrowExceptionWhenCheckNotNullWithNonNullValue() {
        //given
        Object value = "non-null value";
        String errorMessage = "This should not be thrown";

        //when & then
        assertDoesNotThrow(() -> Preconditions.checkNotNull(value, errorMessage));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenCheckNotNullWithNullValue() {
        //given
        Object value = null;
        String errorMessage = "Value cannot be null";

        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Preconditions.checkNotNull(value, errorMessage)
        );

        //then
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForComplexCondition() {
        //given
        int age = 15;
        String errorMessage = "Age must be at least 18";

        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Preconditions.checkArgument(age >= 18, errorMessage)
        );

        //then
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void shouldNotThrowExceptionForComplexCondition() {
        //given
        int age = 25;
        String errorMessage = "Age must be at least 18";

        //when & then
        assertDoesNotThrow(() -> Preconditions.checkArgument(age >= 18, errorMessage));
    }
}
