package com.softwarearchetypes.pricing.common;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public sealed interface Result<F, S> permits Result.Success, Result.Failure {

    static <F, S> Result<F, S> success(S value) {
        return new Success<>(value);
    }

    static <F, S> Result<F, S> failure(F value) {
        return new Failure<>(value);
    }

    default <L, R> Result<L, R> biMap(Function<? super S, ? extends R> successMapper, Function<? super F, ? extends L> failureMapper) {
        if (success()) {
            return new Success<>(successMapper.apply(getSuccess()));
        } else {
            return new Failure<>(failureMapper.apply(getFailure()));
        }
    }

    default <R> Result<F, R> map(Function<? super S, ? extends R> mapper) {
        if (success()) {
            return new Success<>(mapper.apply(getSuccess()));
        } else {
            return new Failure<>(getFailure());
        }
    }

    default <L> Result<L, S> mapFailure(Function<? super F, ? extends L> mapper) {
        if (success()) {
            return new Success<>(getSuccess());
        } else {
            return new Failure<>(mapper.apply(getFailure()));
        }
    }

    default Result<F, S> peek(Consumer<? super S> successConsumer, Consumer<? super F> failureConsumer) {
        if (success()) {
            successConsumer.accept(getSuccess());
        } else {
            failureConsumer.accept(getFailure());
        }
        return this;
    }

    default Result<F, S> peekSuccess(Consumer<? super S> successConsumer) {
        return peek(successConsumer, it -> {
        });
    }

    default Result<F, S> peekFailure(Consumer<? super F> failureConsumer) {
        return peek(it -> {
        }, failureConsumer);
    }

    default <R> R ifSuccessOrElse(Function<S, R> successMapping, Function<F, R> failureMapping) {
        if (success()) {
            return successMapping.apply(getSuccess());
        } else {
            return failureMapping.apply(getFailure());
        }
    }

    default <R> Result<F, R> flatMap(Function<S, Result<F, R>> mapping) {
        if (success()) {
            return mapping.apply(getSuccess());
        } else {
            return (Result<F, R>) this;
        }
    }

    default <U> U fold(Function<? super F, ? extends U> leftMapper, Function<? super S, ? extends U> rightMapper) {
        Objects.requireNonNull(leftMapper, "leftMapper is null");
        Objects.requireNonNull(rightMapper, "rightMapper is null");
        if (success()) {
            return rightMapper.apply(getSuccess());
        } else {
            return leftMapper.apply(getFailure());
        }
    }

    default boolean success() {
        throw new IllegalStateException();
    }

    default boolean failure() {
        throw new IllegalStateException();
    }

    default S getSuccess() {
        throw new IllegalStateException();
    }

    default F getFailure() {
        throw new IllegalStateException();
    }

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

}
