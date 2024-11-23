package com.softwarearchetypes.common;

public record Version(long value) {

    private static final long INITIAL_VALUE = 0L;

    public static Version initial() {
        return new Version(INITIAL_VALUE);
    }

    public static Version of(long value) {
        return new Version(value);
    }
}
