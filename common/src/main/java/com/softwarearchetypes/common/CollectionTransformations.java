package com.softwarearchetypes.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class CollectionTransformations {

    private CollectionTransformations() {
    }

    public static HashMap<String, String> keyValueMapFrom(String[] parameters) {
        if (parameters == null) {
            return new HashMap<>();
        }
        if (parameters.length % 2 != 0) {
            throw new IllegalArgumentException("The number of arguments must be even (key, productName, ...)");
        }
        return IntStream.range(0, parameters.length / 2)
                        .map(i -> i * 2)
                        .peek(i -> {
                            String key = parameters[i];
                            if (key == null || key.isBlank()) {
                                throw new IllegalArgumentException(String.format("Key (idx=%d) cannot be empty or null", i));
                            }
                        })
                        .boxed()
                        .collect(Collectors.toMap(
                                i -> parameters[i],
                                i -> parameters[i + 1],
                                (a, b) -> b,
                                HashMap::new
                        ));
    }

    public static <T> Set<T> subtract(Set<T> minuend, Set<T> subtrahend) {
        Set<T> result = new HashSet<>(minuend);
        result.removeAll(subtrahend);
        return result;
    }
}
