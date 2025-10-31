package com.softwarearchetypes.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.softwarearchetypes.common.Preconditions.checkNotNull;

public sealed interface Result<F, S> permits Result.Success, Result.Failure {

    final class Success<F, S> implements Result<F, S> {

        private final S success;

        Success(S success) {
            this.success = success;
        }

        @Override
        public boolean success() {
            return true;
        }

        @Override
        public boolean failure() {
            return false;
        }

        @Override
        public S getSuccess() {
            return success;
        }
    }

    final class Failure<F, S> implements Result<F, S> {

        private final F failure;

        Failure(F failure) {
            this.failure = failure;
        }

        @Override
        public F getFailure() {
            return failure;
        }

        @Override
        public boolean success() {
            return false;
        }

        @Override
        public boolean failure() {
            return true;
        }
    }

    default <L, R> Result<L, R> biMap(Function<? super S, ? extends R> successMapper, Function<? super F, ? extends L> failureMapper) {
        checkNotNull(successMapper, "successMapper cannot be null");
        checkNotNull(failureMapper, "failureMapper cannot be null");
        if (success()) {
            return new Success<>(successMapper.apply(getSuccess()));
        } else {
            return new Failure<>(failureMapper.apply(getFailure()));
        }
    }

    default <R> Result<F, R> map(Function<? super S, ? extends R> mapper) {
        checkNotNull(mapper, "mapper cannot be null");
        if (success()) {
            return new Success<>(mapper.apply(getSuccess()));
        } else {
            return new Failure<>(getFailure());
        }
    }

    default <L> Result<L, S> mapFailure(Function<? super F, ? extends L> mapper) {
        checkNotNull(mapper, "mapper cannot be null");
        if (success()) {
            return new Success<>(getSuccess());
        } else {
            return new Failure<>(mapper.apply(getFailure()));
        }
    }

    default Result<F, S> peek(Consumer<? super S> successConsumer, Consumer<? super F> failureConsumer) {
        checkNotNull(successConsumer, "successConsumer cannot be null");
        checkNotNull(failureConsumer, "failureConsumer cannot be null");
        if (success()) {
            successConsumer.accept(getSuccess());
        } else {
            failureConsumer.accept(getFailure());
        }
        return this;
    }

    default Result<F, S> peekSuccess(Consumer<? super S> successConsumer) {
        checkNotNull(successConsumer, "successConsumer cannot be null");
        return peek(successConsumer, it -> {
        });
    }

    default Result<F, S> peekFailure(Consumer<? super F> failureConsumer) {
        checkNotNull(failureConsumer, "failureConsumer cannot be null");
        return peek(it -> {
        }, failureConsumer);
    }

    default <R> R ifSuccessOrElse(Function<S, R> successMapping, Function<F, R> failureMapping) {
        checkNotNull(successMapping, "successMapping cannot be null");
        checkNotNull(failureMapping, "failureMapping cannot be null");
        if (success()) {
            return successMapping.apply(getSuccess());
        } else {
            return failureMapping.apply(getFailure());
        }
    }

    default <R> Result<F, R> flatMap(Function<S, Result<F, R>> mapping) {
        checkNotNull(mapping, "mapping cannot be null");
        if (success()) {
            return mapping.apply(getSuccess());
        } else {
            return (Result<F, R>) this;
        }
    }

    default <U> U fold(Function<? super F, ? extends U> leftMapper, Function<? super S, ? extends U> rightMapper) {
        checkNotNull(leftMapper, "leftMapper cannot be null");
        checkNotNull(rightMapper, "rightMapper cannot be null");
        if (success()) {
            return rightMapper.apply(getSuccess());
        } else {
            return leftMapper.apply(getFailure());
        }
    }

    default <FAILURE, SUCCESS> Result<FAILURE, SUCCESS> combine(Result<F, S> secondResult, BiFunction<F, F, FAILURE> failureCombiner, BiFunction<S, S, SUCCESS> successCombiner) {
        checkNotNull(secondResult, "secondResult cannot be null");
        checkNotNull(failureCombiner, "failureCombiner cannot be null");
        checkNotNull(successCombiner, "successCombiner cannot be null");
        if (success() && secondResult.success()) {
            return new Success<>(successCombiner.apply(getSuccess(), secondResult.getSuccess()));
        } else {
            return new Failure<>(failureCombiner.apply(failure() ? getFailure() : null, secondResult.failure() ? secondResult.getFailure() : null));
        }
    }

    default boolean success() {
        throw new IllegalStateException();
    }

    static <F, S> Result<F, S> success(S value) {
        return new Success<>(value);
    }

    default boolean failure() {
        throw new IllegalStateException();
    }

    static <F, S> Result<F, S> failure(F value) {
        return new Failure<>(value);
    }

    /**
     * Creates an empty composite Result accumulator with an empty list.
     * Use with accumulate() to progressively build up a Result containing multiple values.
     *
     * @return CompositeResult with empty list
     */
    static <F, S> CompositeResult<F, S> composite() {
        return new CompositeResult<>(new ArrayList<>());
    }

    /**
     * Creates an empty composite Result accumulator with an empty set.
     * Use with accumulate() to progressively build up a Result containing multiple values.
     *
     * @return CompositeSetResult with empty set
     */
    static <F, S> CompositeSetResult<F, S> compositeSet() {
        return new CompositeSetResult<>(new HashSet<>());
    }

    /**
     * Helper class for accumulating multiple Results into a list.
     * Provides fail-fast semantics - stops on first failure.
     */
    final class CompositeResult<F, S> {
        private final Result<F, List<S>> result;

        private CompositeResult(List<S> initialList) {
            this.result = new Success<>(initialList);
        }

        private CompositeResult(F failure) {
            this.result = new Failure<>(failure);
        }

        /**
         * Accumulates a new Result into this composite.
         * If already failed, returns the existing failure.
         * If new Result fails, returns new failure.
         * If both succeed, adds the new value to the list.
         *
         * @param newResult the Result to accumulate
         * @return CompositeResult with accumulated values or failure
         */
        public CompositeResult<F, S> accumulate(Result<F, S> newResult) {
            checkNotNull(newResult, "newResult cannot be null");
            if (result.failure()) {
                return this;
            }
            if (newResult.failure()) {
                return new CompositeResult<>(newResult.getFailure());
            }
            List<S> accumulated = new ArrayList<>(result.getSuccess());
            accumulated.add(newResult.getSuccess());
            return new CompositeResult<>(accumulated);
        }

        /**
         * Checks if this composite is in success state.
         *
         * @return true if success, false if failure
         */
        public boolean success() {
            return result.success();
        }

        /**
         * Checks if this composite is in failure state.
         *
         * @return true if failure, false if success
         */
        public boolean failure() {
            return result.failure();
        }

        /**
         * Extracts the final Result.
         *
         * @return Result containing either a list of all successes or the first failure
         */
        public Result<F, List<S>> toResult() {
            return result;
        }
    }

    /**
     * Helper class for accumulating multiple Results into a set.
     * Provides fail-fast semantics - stops on first failure.
     */
    final class CompositeSetResult<F, S> {
        private final Result<F, Set<S>> result;

        private CompositeSetResult(Set<S> initialSet) {
            this.result = new Success<>(initialSet);
        }

        private CompositeSetResult(F failure) {
            this.result = new Failure<>(failure);
        }

        /**
         * Accumulates a new Result into this composite.
         * If already failed, returns the existing failure.
         * If new Result fails, returns new failure.
         * If both succeed, adds the new value to the set.
         *
         * @param newResult the Result to accumulate
         * @return CompositeSetResult with accumulated values or failure
         */
        public CompositeSetResult<F, S> accumulate(Result<F, S> newResult) {
            checkNotNull(newResult, "newResult cannot be null");
            if (result.failure()) {
                return this;
            }
            if (newResult.failure()) {
                return new CompositeSetResult<>(newResult.getFailure());
            }
            Set<S> accumulated = new HashSet<>(result.getSuccess());
            accumulated.add(newResult.getSuccess());
            return new CompositeSetResult<>(accumulated);
        }

        /**
         * Checks if this composite is in success state.
         *
         * @return true if success, false if failure
         */
        public boolean success() {
            return result.success();
        }

        /**
         * Checks if this composite is in failure state.
         *
         * @return true if failure, false if success
         */
        public boolean failure() {
            return result.failure();
        }

        /**
         * Extracts the final Result.
         *
         * @return Result containing either a set of all successes or the first failure
         */
        public Result<F, Set<S>> toResult() {
            return result;
        }
    }

    default S getSuccess() {
        throw new IllegalStateException();
    }

    default F getFailure() {
        throw new IllegalStateException();
    }

}
