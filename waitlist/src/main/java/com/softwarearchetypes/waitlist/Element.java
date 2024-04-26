package com.softwarearchetypes.waitlist;

import java.util.UUID;

record Element(UUID value, int priority) implements Comparable<Element> {

    private static final int DEFAULT_PRIORITY = 1;

    static Element of(UUID value, int priority) {
        return new Element(value, priority);
    }

    static Element of(UUID value) {
        return new Element(value, DEFAULT_PRIORITY);
    }

    @Override
    public int compareTo(Element o) {
        return Integer.compare(this.priority, o.priority);
    }
}
