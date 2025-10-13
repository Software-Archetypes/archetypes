package com.softwarearchetypes.accounting.postingrules;

import java.util.Map;
import java.util.Optional;

public interface BusinessContext {

    Map<String, Object> data();

    default <T> Optional<T> get(String key, Class<T> type) {
        Object value = data().get(key);
        if (type.isInstance(value)) {
            return Optional.of(type.cast(value));
        }
        return Optional.empty();
    }

    static BusinessContext empty() {
        return Map::of;
    }

    static BusinessContext of(Map<String, Object> data) {
        return () -> Map.copyOf(data);
    }
}