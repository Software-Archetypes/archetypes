package com.softwarearchetypes.common;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import com.softwarearchetypes.common.Result.CompositeResult;
import com.softwarearchetypes.common.Result.CompositeSetResult;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.RandomStringUtils.insecure;
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
        assertTrue(Result.success(insecure().nextAlphabetic(10)).success());
    }

    @Test
    void shouldNotBeMarkedAsFailureForSuccessResult() {
        assertFalse(Result.success(insecure().nextAlphabetic(10)).failure());
    }

    @Test
    void shouldBeMarkedAsFailureForFailureResult() {
        assertTrue(Result.failure(insecure().nextAlphabetic(10)).failure());
    }

    @Test
    void shouldNotBeMarkedAsSuccessForFailureResult() {
        assertFalse(Result.failure(insecure().nextAlphabetic(10)).success());
    }

    @Test
    void shouldFailToGetSuccessOnFailureResult() {
        assertThrows(IllegalStateException.class, () -> Result.failure(insecure().nextAlphabetic(10)).getSuccess());
    }

    @Test
    void shouldFailToGetFailureOnSuccessResult() {
        assertThrows(IllegalStateException.class, () -> Result.success(insecure().nextAlphabetic(10)).getFailure());
    }

    @Test
    void shouldChooseAndProperlyApplySuccessMappingFunction() {
        //given
        String value = insecure().nextAlphabetic(10);

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
        String value = insecure().nextAlphabetic(10);

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

    @Test
    void shouldMapSuccessValueUsingMapFunction() {
        //given
        int value = 10;

        //and
        Function<Integer, String> mapper = val -> "Value: " + (val * 2);

        //when
        Result<String, String> result = Result.<String, Integer>success(value).map(mapper);

        //then
        assertTrue(result.success());
        assertEquals("Value: 20", result.getSuccess());
    }

    @Test
    void shouldNotMapFailureValueUsingMapFunction() {
        //given
        String errorMessage = "Error occurred";

        //and
        Function<Integer, String> mapper = val -> "Value: " + (val * 2);

        //when
        Result<String, String> result = Result.<String, Integer>failure(errorMessage).map(mapper);

        //then
        assertTrue(result.failure());
        assertEquals(errorMessage, result.getFailure());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenMapWithNullMapper() {
        //given
        Result<String, Integer> result = Result.success(10);

        //when & then
        assertThrows(IllegalArgumentException.class, () -> result.map(null));
    }

    @Test
    void shouldMapFailureValueUsingMapFailureFunction() {
        //given
        int errorCode = 404;

        //and
        Function<Integer, String> mapper = code -> "Error " + code + ": Not Found";

        //when
        Result<String, Integer> result = Result.<Integer, Integer>failure(errorCode).mapFailure(mapper);

        //then
        assertTrue(result.failure());
        assertEquals("Error 404: Not Found", result.getFailure());
    }

    @Test
    void shouldNotMapSuccessValueUsingMapFailureFunction() {
        //given
        int value = 42;

        //and
        Function<Integer, String> mapper = code -> "Error " + code;

        //when
        Result<String, Integer> result = Result.<Integer, Integer>success(value).mapFailure(mapper);

        //then
        assertTrue(result.success());
        assertEquals(value, result.getSuccess());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenMapFailureWithNullMapper() {
        //given
        Result<String, Integer> result = Result.failure("error");

        //when & then
        assertThrows(IllegalArgumentException.class, () -> result.mapFailure(null));
    }

    @Test
    void shouldFlatMapSuccessResultWithAnotherSuccessResult() {
        //given
        int value = 5;

        //and
        Function<Integer, Result<String, Integer>> mapper = val -> Result.success(val * 2);

        //when
        Result<String, Integer> result = Result.<String, Integer>success(value).flatMap(mapper);

        //then
        assertTrue(result.success());
        assertEquals(10, result.getSuccess());
    }

    @Test
    void shouldFlatMapSuccessResultWithFailureResult() {
        //given
        int value = 5;
        String errorMessage = "Validation failed";

        //and
        Function<Integer, Result<String, Integer>> mapper = val -> Result.failure(errorMessage);

        //when
        Result<String, Integer> result = Result.<String, Integer>success(value).flatMap(mapper);

        //then
        assertTrue(result.failure());
        assertEquals(errorMessage, result.getFailure());
    }

    @Test
    void shouldNotFlatMapFailureResult() {
        //given
        String errorMessage = "Initial error";

        //and
        Function<Integer, Result<String, Integer>> mapper = val -> Result.success(val * 2);

        //when
        Result<String, Integer> result = Result.<String, Integer>failure(errorMessage).flatMap(mapper);

        //then
        assertTrue(result.failure());
        assertEquals(errorMessage, result.getFailure());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenFlatMapWithNullMapper() {
        //given
        Result<String, Integer> result = Result.success(10);

        //when & then
        assertThrows(IllegalArgumentException.class, () -> result.flatMap(null));
    }

    @Test
    void shouldFoldSuccessResultUsingRightMapper() {
        //given
        int value = 10;

        //and
        Function<String, Integer> leftMapper = error -> -1;
        Function<Integer, Integer> rightMapper = val -> val * 3;

        //when
        Integer result = Result.<String, Integer>success(value).fold(leftMapper, rightMapper);

        //then
        assertEquals(30, result);
    }

    @Test
    void shouldFoldFailureResultUsingLeftMapper() {
        //given
        String errorMessage = "Error";

        //and
        Function<String, Integer> leftMapper = String::length;
        Function<Integer, Integer> rightMapper = val -> val * 3;

        //when
        Integer result = Result.<String, Integer>failure(errorMessage).fold(leftMapper, rightMapper);

        //then
        assertEquals(5, result);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenFoldingWithNullLeftMapper() {
        //given
        Result<String, Integer> result = Result.success(10);

        //when & then
        assertThrows(IllegalArgumentException.class, () -> result.fold(null, val -> val * 2));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenFoldingWithNullRightMapper() {
        //given
        Result<String, Integer> result = Result.success(10);

        //when & then
        assertThrows(IllegalArgumentException.class, () -> result.fold(error -> -1, null));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenBiMapWithNullSuccessMapper() {
        //given
        Result<String, Integer> result = Result.success(10);

        //when & then
        assertThrows(IllegalArgumentException.class, () -> result.biMap(null, error -> ""));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenBiMapWithNullFailureMapper() {
        //given
        Result<String, Integer> result = Result.success(10);

        //when & then
        assertThrows(IllegalArgumentException.class, () -> result.biMap(val -> "", null));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenIfSuccessOrElseWithNullSuccessMapping() {
        //given
        Result<String, Integer> result = Result.success(10);

        //when & then
        assertThrows(IllegalArgumentException.class, () -> result.ifSuccessOrElse(null, error -> ""));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenIfSuccessOrElseWithNullFailureMapping() {
        //given
        Result<String, Integer> result = Result.success(10);

        //when & then
        assertThrows(IllegalArgumentException.class, () -> result.ifSuccessOrElse(val -> "", null));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenPeekWithNullSuccessConsumer() {
        //given
        Result<String, Integer> result = Result.success(10);

        //when & then
        assertThrows(IllegalArgumentException.class, () -> result.peek(null, error -> {}));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenPeekWithNullFailureConsumer() {
        //given
        Result<String, Integer> result = Result.success(10);

        //when & then
        assertThrows(IllegalArgumentException.class, () -> result.peek(val -> {}, null));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenPeekSuccessWithNullConsumer() {
        //given
        Result<String, Integer> result = Result.success(10);

        //when & then
        assertThrows(IllegalArgumentException.class, () -> result.peekSuccess(null));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenPeekFailureWithNullConsumer() {
        //given
        Result<String, Integer> result = Result.failure("error");

        //when & then
        assertThrows(IllegalArgumentException.class, () -> result.peekFailure(null));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenCombineWithNullSecondResult() {
        //given
        Result<String, Integer> result = Result.success(10);

        //when & then
        assertThrows(IllegalArgumentException.class, () -> result.combine(null, (f1, f2) -> "", (s1, s2) -> 0));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenCombineWithNullFailureCombiner() {
        //given
        Result<String, Integer> firstResult = Result.success(10);
        Result<String, Integer> secondResult = Result.success(20);

        //when & then
        assertThrows(IllegalArgumentException.class, () -> firstResult.combine(secondResult, null, (s1, s2) -> s1 + s2));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenCombineWithNullSuccessCombiner() {
        //given
        Result<String, Integer> firstResult = Result.success(10);
        Result<String, Integer> secondResult = Result.success(20);

        //when & then
        assertThrows(IllegalArgumentException.class, () -> firstResult.combine(secondResult, (f1, f2) -> "", null));
    }

    @Test
    void shouldCreateEmptyCompositeResult() {
        //when
        CompositeResult<String, Integer> composite = Result.composite();
        Result<String, List<Integer>> result = composite.toResult();

        //then
        assertTrue(result.success());
        assertTrue(result.getSuccess().isEmpty());
    }

    @Test
    void shouldCreateEmptyCompositeSetResult() {
        //when
        CompositeSetResult<String, Integer> composite = Result.compositeSet();
        Result<String, Set<Integer>> result = composite.toResult();

        //then
        assertTrue(result.success());
        assertTrue(result.getSuccess().isEmpty());
    }

    @Test
    void shouldAccumulateSuccessResultsIntoList() {
        //given
        CompositeResult<String, Integer> composite = Result.composite();

        //when
        Result<String, List<Integer>> result = composite
                .accumulate(Result.success(1))
                .accumulate(Result.success(2))
                .accumulate(Result.success(3))
                .toResult();

        //then
        assertTrue(result.success());
        assertEquals(List.of(1, 2, 3), result.getSuccess());
    }

    @Test
    void shouldAccumulateSuccessResultsIntoSet() {
        //given
        CompositeSetResult<String, Integer> composite = Result.compositeSet();

        //when
        Result<String, Set<Integer>> result = composite
                .accumulate(Result.success(1))
                .accumulate(Result.success(2))
                .accumulate(Result.success(3))
                .toResult();

        //then
        assertTrue(result.success());
        assertEquals(Set.of(1, 2, 3), result.getSuccess());
    }

    @Test
    void shouldStopAccumulatingOnFirstFailure() {
        //given
        CompositeResult<String, Integer> composite = Result.composite();

        //when
        Result<String, List<Integer>> result = composite
                .accumulate(Result.success(1))
                .accumulate(Result.failure("Error occurred"))
                .accumulate(Result.success(3))
                .toResult();

        //then
        assertTrue(result.failure());
        assertEquals("Error occurred", result.getFailure());
    }

    @Test
    void shouldStopAccumulatingToSetOnFirstFailure() {
        //given
        CompositeSetResult<String, Integer> composite = Result.compositeSet();

        //when
        Result<String, Set<Integer>> result = composite
                .accumulate(Result.success(1))
                .accumulate(Result.failure("Error occurred"))
                .accumulate(Result.success(3))
                .toResult();

        //then
        assertTrue(result.failure());
        assertEquals("Error occurred", result.getFailure());
    }

    @Test
    void shouldRetainFailureWhenAccumulatingToFailedComposite() {
        //given
        CompositeResult<String, Integer> composite = Result.<String, Integer>composite()
                .accumulate(Result.failure("First error"));

        //when
        Result<String, List<Integer>> result = composite
                .accumulate(Result.success(1))
                .accumulate(Result.success(2))
                .toResult();

        //then
        assertTrue(result.failure());
        assertEquals("First error", result.getFailure());
    }

    @Test
    void shouldRetainFailureWhenAccumulatingToSetToFailedComposite() {
        //given
        CompositeSetResult<String, Integer> composite = Result.<String, Integer>compositeSet()
                .accumulate(Result.failure("First error"));

        //when
        Result<String, Set<Integer>> result = composite
                .accumulate(Result.success(1))
                .accumulate(Result.success(2))
                .toResult();

        //then
        assertTrue(result.failure());
        assertEquals("First error", result.getFailure());
    }

    @Test
    void shouldAccumulateToSetRemovingDuplicates() {
        //given
        CompositeSetResult<String, Integer> composite = Result.compositeSet();

        //when
        Result<String, Set<Integer>> result = composite
                .accumulate(Result.success(1))
                .accumulate(Result.success(2))
                .accumulate(Result.success(1))
                .accumulate(Result.success(3))
                .toResult();

        //then
        assertTrue(result.success());
        assertEquals(Set.of(1, 2, 3), result.getSuccess());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenAccumulateWithNull() {
        //given
        CompositeResult<String, Integer> composite = Result.composite();

        //when & then
        assertThrows(IllegalArgumentException.class, () -> composite.accumulate(null));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenAccumulateToSetWithNull() {
        //given
        CompositeSetResult<String, Integer> composite = Result.compositeSet();

        //when & then
        assertThrows(IllegalArgumentException.class, () -> composite.accumulate(null));
    }

    @Test
    void shouldReturnTrueForSuccessOnCompositeResult() {
        //given
        CompositeResult<String, Integer> composite = Result.<String, Integer>composite()
                .accumulate(Result.success(1))
                .accumulate(Result.success(2));

        //when & then
        assertTrue(composite.success());
        assertFalse(composite.failure());
    }

    @Test
    void shouldReturnTrueForFailureOnCompositeResult() {
        //given
        CompositeResult<String, Integer> composite = Result.<String, Integer>composite()
                .accumulate(Result.success(1))
                .accumulate(Result.failure("Error"));

        //when & then
        assertTrue(composite.failure());
        assertFalse(composite.success());
    }

    @Test
    void shouldReturnTrueForSuccessOnCompositeSetResult() {
        //given
        CompositeSetResult<String, Integer> composite = Result.<String, Integer>compositeSet()
                .accumulate(Result.success(1))
                .accumulate(Result.success(2));

        //when & then
        assertTrue(composite.success());
        assertFalse(composite.failure());
    }

    @Test
    void shouldReturnTrueForFailureOnCompositeSetResult() {
        //given
        CompositeSetResult<String, Integer> composite = Result.<String, Integer>compositeSet()
                .accumulate(Result.success(1))
                .accumulate(Result.failure("Error"));

        //when & then
        assertTrue(composite.failure());
        assertFalse(composite.success());
    }

    private static int randomNumber() {
        return RANDOM.nextInt();
    }

}
