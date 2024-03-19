package com.softwarearchetypes.availability;

import java.util.*;
import java.util.stream.Collectors;

public record Calendars(Map<ResourceId, com.softwarearchetypes.availability.Calendar> calendars) {

    public static Calendars of(com.softwarearchetypes.availability.Calendar... calendars) {
        Map<ResourceId, com.softwarearchetypes.availability.Calendar> collect =
                Arrays.stream(calendars)
                        .collect(Collectors.toMap(com.softwarearchetypes.availability.Calendar::resourceId, calendar -> calendar));
        return new Calendars(collect);
    }

    public com.softwarearchetypes.availability.Calendar get(ResourceId resourceId) {
        return calendars.getOrDefault(resourceId, Calendar.empty(resourceId));
    }
}


