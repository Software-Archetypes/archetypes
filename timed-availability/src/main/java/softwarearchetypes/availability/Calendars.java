package softwarearchetypes.availability;

import java.util.*;
import java.util.stream.Collectors;

public record Calendars(Map<ResourceId, softwarearchetypes.availability.Calendar> calendars) {

    public static Calendars of(softwarearchetypes.availability.Calendar... calendars) {
        Map<ResourceId, softwarearchetypes.availability.Calendar> collect =
                Arrays.stream(calendars)
                        .collect(Collectors.toMap(softwarearchetypes.availability.Calendar::resourceId, calendar -> calendar));
        return new Calendars(collect);
    }

    public softwarearchetypes.availability.Calendar get(ResourceId resourceId) {
        return calendars.getOrDefault(resourceId, Calendar.empty(resourceId));
    }
}


