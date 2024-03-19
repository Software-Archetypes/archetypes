package softwarearchetypes.multidimensionalknapsack;



import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

record TimedCapability(UUID uuid, String id, String capability) implements CapacityDimension {

    public TimedCapability(String id, String capacityName) {
        this(UUID.randomUUID(), id, capacityName);
    }
}

record DemandedCapability(String capability) implements WeightDimension<TimedCapability> {

    @Override
    public boolean isSatisfiedBy(TimedCapability capacityDimension) {
        return capacityDimension.capability().equals(capability);
    }
}

record AvailableTimedCapability(UUID uuid, String id, String capability, TimeSlot timeSlot) implements CapacityDimension {

    public AvailableTimedCapability(String id, String capability, TimeSlot timeSlot) {
        this(UUID.randomUUID(), id, capability, timeSlot);
    }
}

record DemandedTimedCapability(String capability, TimeSlot timeSlot) implements WeightDimension<AvailableTimedCapability> {

    @Override
    public boolean isSatisfiedBy(AvailableTimedCapability capacityTimedDimension) {
        return capacityTimedDimension.capability().equals(capability) &&
                this.timeSlot.within(capacityTimedDimension.timeSlot());
    }
}

record Project(String projectId, int value, List<DemandedTimedCapability> missingDemands) {

    public Item toItem() {
        List<WeightDimension> weights = new ArrayList<>(missingDemands);
        return new Item(projectId, value, new TotalWeight(weights));
    }
}


record TimeSlot(Instant from, Instant to) {

    public static TimeSlot createTimeSlotAtUTCOfDuration(int year, int month, int day, Duration duration) {
        LocalDate thisDay = LocalDate.of(year, month, day);
        Instant from = thisDay.atStartOfDay(ZoneId.of("UTC")).toInstant();
        return new TimeSlot(from, from.plus(duration));
    }

    public static TimeSlot createMonthlyTimeSlotAtUTC(int year, int month) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1);
        Instant from = startOfMonth.atStartOfDay(ZoneId.of("UTC")).toInstant();
        Instant to = endOfMonth.atTime(0, 0, 0).atZone(ZoneId.of("UTC")).toInstant();
        return new TimeSlot(from, to);
    }

    public boolean within(TimeSlot other) {
        return !this.from.isBefore(other.from) && !this.to.isAfter(other.to);
    }

}


