package com.softwarearchetypes.common;

import java.util.HashSet;
import java.util.Set;

public class CollectionFixture {

    public static <T> Set<T> copyAndAdd(Set<T> collection, T element) {
        Set<T> copy = new HashSet<>(collection);
        copy.add(element);
        return copy;
    }
}
