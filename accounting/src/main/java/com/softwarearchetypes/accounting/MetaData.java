package com.softwarearchetypes.accounting;

import java.util.HashMap;
import java.util.Map;

public record MetaData(Map<String, String> metadata) {

    static MetaData empty() {
        return new MetaData(new HashMap<>());
    }

    static MetaData of(String... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("MetaData must have even number of elements (key-value pairs).");
        }
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }
        return new MetaData(map);
    }
}
