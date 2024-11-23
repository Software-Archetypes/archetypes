package com.softwarearchetypes.common;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CollectionFixture {

    public static <T> Set<T> copyAndAdd(Set<T> collection, T element) {
        Set<T> copy = new HashSet<>(collection);
        copy.add(element);
        return copy;
    }

    public static Set<String> stringSetFrom(Set<? extends Enum<?>> set) {
        return set.stream().map(Enum::name).collect(Collectors.toSet());
    }
}
