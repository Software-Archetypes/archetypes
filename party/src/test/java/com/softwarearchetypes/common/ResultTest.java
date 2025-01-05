package com.softwarearchetypes.common;

import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ResultTest {

    private static final Random RANDOM = new Random();

    @Test
    void shouldBeMarkedAsSuccessForSuccessResult() {
        assertTrue(Result.success(randomAlphabetic(10)).success());
    }

    @Test
    void shouldNotBeMarkedAsFailureForSuccessResult() {
        assertFalse(Result.success(randomAlphabetic(10)).failure());
    }

    @Test
    void shouldBeMarkedAsFailureForFailureResult() {
        assertTrue(Result.failure(randomAlphabetic(10)).failure());
    }

    @Test
    void shouldNotBeMarkedAsSuccessForFailureResult() {
        assertFalse(Result.failure(randomAlphabetic(10)).success());
    }

    @Test
    void shouldFailToGetSuccessOnFailureResult() {
        assertThrows(IllegalStateException.class, () -> Result.failure(randomAlphabetic(10)).getSuccess());
    }

    @Test
    void shouldFailToGetFailureOnSuccessResult() {
        assertThrows(IllegalStateException.class, () -> Result.success(randomAlphabetic(10)).getFailure());
    }

    @Test
    void shouldChooseAndProperlyApplySuccessMappingFunction() {
        //given
        String value = randomAlphabetic(10);

        //and
        Function<String, String> successMappingFunction = val -> "SUCCESS-" + val;
        Function<String, String> failureMappingFunction = val -> "FAILURE-" + val;

        //when
        Result<String, String> result = Result.success(value);

        //then
        assertEquals("SUCCESS-" + value, result.ifSuccessOrElse(successMappingFunction, failureMappingFunction));
    }

    @Test
    void shouldChooseAndProperlyApplyFailureMappingFunction() {
        //given
        String value = randomAlphabetic(10);

        //and
        Function<String, String> successMappingFunction = val -> "SUCCESS-" + val;
        Function<String, String> failureMappingFunction = val -> "FAILURE-" + val;

        //when
        Result<String, String> result = Result.failure(value);

        //then
        assertEquals("FAILURE-" + value, result.ifSuccessOrElse(successMappingFunction, failureMappingFunction));
    }

    @Test
    void shouldMapSuccessResultAccordingToMappingFunction() {
        //given
        int value = 1;

        //and
        Function<Integer, String> successMappingFunction = val -> String.valueOf(val * 2);
        Function<Integer, String> failureMappingFunction = val -> "";

        //when
        Result<Integer, Integer> result = Result.success(value);

        //then
        assertEquals("2", result.biMap(successMappingFunction, failureMappingFunction).getSuccess());
    }

    @Test
    void shouldMapFailureResultAccordingToMappingFunction() {
        //given
        int value = 1;

        //and
        Function<Integer, String> successMappingFunction = val -> String.valueOf(val * 2);
        Function<Integer, String> failureMappingFunction = val -> "";

        //when
        Result<Integer, Integer> result = Result.failure(value);

        //then
        assertEquals("", result.biMap(successMappingFunction, failureMappingFunction).getFailure());
    }

    @Test
    void shouldCallSuccessConsumerOnPeek() {
        //given
        int value = randomNumber();
        Consumer<Integer> successConsumer = mock(Consumer.class);
        Consumer<Integer> failureConsumer = mock(Consumer.class);

        //and
        Result<Integer, Integer> result = Result.success(value);

        //when
        Result<Integer, Integer> peekResult = result.peek(successConsumer, failureConsumer);

        //then
        assertEquals(result, peekResult);
        verify(successConsumer, times(1)).accept(value);
        verify(failureConsumer, times(0)).accept(value);
    }

    @Test
    void shouldCallSuccessConsumerOnPeekSuccess() {
        //given
        int value = randomNumber();
        Consumer<Integer> successConsumer = mock(Consumer.class);
        Consumer<Integer> failureConsumer = mock(Consumer.class);

        //and
        Result<Integer, Integer> result = Result.success(value);

        //when
        Result<Integer, Integer> peekResult = result.peekSuccess(successConsumer)
                                                    .peekFailure(failureConsumer);

        //then
        assertEquals(result, peekResult);
        verify(successConsumer, times(1)).accept(value);
        verify(failureConsumer, times(0)).accept(value);
    }

    @Test
    void shouldCallFailureConsumerOnPeek() {
        //given
        int value = randomNumber();
        Consumer<Integer> successConsumer = mock(Consumer.class);
        Consumer<Integer> failureConsumer = mock(Consumer.class);

        //and
        Result<Integer, Integer> result = Result.failure(value);

        //when
        Result<Integer, Integer> peekResult = result.peek(successConsumer, failureConsumer);

        //then
        assertEquals(result, peekResult);
        verify(successConsumer, times(0)).accept(value);
        verify(failureConsumer, times(1)).accept(value);
    }

    @Test
    void shouldCallFailureConsumerOnPeekFailure() {
        //given
        int value = randomNumber();
        Consumer<Integer> successConsumer = mock(Consumer.class);
        Consumer<Integer> failureConsumer = mock(Consumer.class);

        //and
        Result<Integer, Integer> result = Result.failure(value);

        //when
        Result<Integer, Integer> peekResult = result.peekSuccess(successConsumer)
                                                    .peekFailure(failureConsumer);

        //then
        assertEquals(result, peekResult);
        verify(successConsumer, times(0)).accept(value);
        verify(failureConsumer, times(1)).accept(value);
    }

    @Test
    void shouldCombineTwoSuccessResults() {
        //given
        int firstValue = randomNumber();
        int secondValue = randomNumber();

        //and
        Result<Integer, Integer> firstResult = Result.success(firstValue);
        Result<Integer, Integer> secondResult = Result.success(secondValue);

        //and
        BiFunction<Integer, Integer, Integer> successCombiner = Integer::sum;
        BiFunction<Integer, Integer, Integer> failureCombiner = (val1, val2) -> val1 - val2;

        //when
        Result<Integer, Integer> combinedResult = firstResult.combine(secondResult, failureCombiner, successCombiner);

        //then
        assertEquals(firstValue + secondValue, combinedResult.getSuccess());
    }

    @Test
    void shouldCombineTwoFailureResults() {
        //given
        int firstValue = randomNumber();
        int secondValue = randomNumber();

        //and
        Result<Integer, Integer> firstResult = Result.failure(firstValue);
        Result<Integer, Integer> secondResult = Result.failure(secondValue);

        //and
        BiFunction<Integer, Integer, Integer> successCombiner = Integer::sum;
        BiFunction<Integer, Integer, Integer> failureCombiner = (val1, val2) -> val1 - val2;

        //when
        Result<Integer, Integer> combinedResult = firstResult.combine(secondResult, failureCombiner, successCombiner);

        //then
        assertEquals(firstValue - secondValue, combinedResult.getFailure());
    }

    @Test
    void shouldProduceFailureResultWhenCombiningFailureAndSuccessResults() {
        //given
        int firstValue = randomNumber();
        int secondValue = randomNumber();

        //and
        Result<Integer, Integer> firstResult = Result.success(firstValue);
        Result<Integer, Integer> secondResult = Result.failure(secondValue);

        //and
        BiFunction<Integer, Integer, Integer> successCombiner = Integer::sum;
        BiFunction<Integer, Integer, Integer> failureCombiner = (val1, val2) -> ofNullable(val1).orElse(0) - ofNullable(val2).orElse(0);

        //when
        Result<Integer, Integer> successFailureCombinedResult = firstResult.combine(secondResult, failureCombiner, successCombiner);

        //then
        assertEquals(-secondValue, successFailureCombinedResult.getFailure());

        //when
        Result<Integer, Integer> failureSuccessCombinedResult = secondResult.combine(firstResult, failureCombiner, successCombiner);

        //then
        assertEquals(secondValue, failureSuccessCombinedResult.getFailure());
    }

    private static int randomNumber() {
        return RANDOM.nextInt();
    }

}
