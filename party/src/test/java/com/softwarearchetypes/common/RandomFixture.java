package com.softwarearchetypes.common;

import java.util.Collection;

import static org.apache.commons.lang3.RandomUtils.nextInt;

public class RandomFixture {

    public static <T> T randomElementOf(Collection<T> collection) {
        return collection.stream().toList().get(nextInt(0, collection.size()));
    }
}
