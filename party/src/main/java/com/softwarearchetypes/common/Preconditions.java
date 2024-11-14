package com.softwarearchetypes.common;

public final class Preconditions {

    public static void checkArgument(boolean expression, String errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public static void checkNotNull(Object value, String errorMessage) {
        checkArgument(value != null, errorMessage);
    }
}
