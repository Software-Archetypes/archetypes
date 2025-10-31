package com.softwarearchetypes.pricing;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Parameters {
    private final Map<String, Object> values;

    public Parameters() {
        this.values = new HashMap<>();
    }

    public Parameters(Map<String, Object> values) {
        this.values = new HashMap<>(values);
    }


    public static Parameters empty() {
        return new Parameters();
    }

    public BigDecimal getBigDecimal(String key) {
        Object value = values.get(key);
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return new BigDecimal(value.toString());
        }
        if (value instanceof String) {
            return new BigDecimal((String) value);
        }
        throw new IllegalArgumentException("Cannot convert " + value + " to BigDecimal");
    }


    public boolean contains(String key) {
        return values.containsKey(key);
    }

    public boolean containsAll(Set<String> keys) {
        return values.keySet().containsAll(keys);
    }

    public Object get(String key) {
        return values.get(key);
    }

    @Override
    public String toString() {
        return "Parameters" + values;
    }

    public Set<String> keys() {
        return values.keySet();
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public void setValues(Map<String, Object> values) {
        this.values.clear();
        this.values.putAll(values);
    }
}