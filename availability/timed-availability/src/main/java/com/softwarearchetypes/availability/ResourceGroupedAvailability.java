package com.softwarearchetypes.availability;

import com.softwarearchetypes.availability.segment.Segments;
import com.softwarearchetypes.availability.segment.SegmentInMinutes;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

class ResourceGroupedAvailability {

    private final List<ResourceAvailability> resourceAvailabilities;

    ResourceGroupedAvailability(List<ResourceAvailability> resourceAvailabilities) {
        this.resourceAvailabilities = resourceAvailabilities;
    }

    static ResourceGroupedAvailability of(ResourceId resourceId, TimeSlot timeslot) {
        List<ResourceAvailability> resourceAvailabilities = Segments
                .split(timeslot, SegmentInMinutes.defaultSegment())
                .stream()
                .map(segment -> new ResourceAvailability(ResourceAvailabilityId.newOne(), resourceId, segment))
                .toList();
        return new ResourceGroupedAvailability(resourceAvailabilities);
    }

    public static ResourceGroupedAvailability of(ResourceId resourceId, TimeSlot timeslot, ResourceId parentId) {
        List<ResourceAvailability> resourceAvailabilities = Segments
                .split(timeslot, SegmentInMinutes.defaultSegment())
                .stream()
                .map(segment -> new ResourceAvailability(ResourceAvailabilityId.newOne(), resourceId, parentId, segment))
                .toList();
        return new ResourceGroupedAvailability(resourceAvailabilities);
    }

    boolean block(Owner requester) {
        for (ResourceAvailability resourceAvailability : resourceAvailabilities) {
            if (!resourceAvailability.block(requester)) {
                return false;
            }
        }
        return true;
    }

    boolean disable(Owner requester) {
        for (ResourceAvailability resourceAvailability : resourceAvailabilities) {
            if (!resourceAvailability.disable(requester)) {
                return false;
            }
        }
        return true;
    }

    boolean release(Owner requester) {
        for (ResourceAvailability resourceAvailability : resourceAvailabilities) {
            if (!resourceAvailability.release(requester)) {
                return false;
            }
        }
        return true;
    }

    List<ResourceAvailability> availabilities() {
        return resourceAvailabilities;
    }

    Optional<ResourceId> resourceId() {
        //resourceId are the same;
        return resourceAvailabilities
                .stream()
                .map(ResourceAvailability::resourceId)
                .findFirst();
    }

    int size() {
        return resourceAvailabilities.size();
    }

    boolean blockedEntirelyBy(Owner owner) {
        return resourceAvailabilities
                .stream()
                .allMatch(ra -> ra.blockedBy().equals(owner));
    }

    boolean isDisabledEntirelyBy(Owner owner) {
        return resourceAvailabilities
                .stream()
                .allMatch(ra -> ra.isDisabledBy(owner));
    }

    boolean isEntirelyWithParentId(ResourceId parentId) {
        return resourceAvailabilities
                .stream()
                .allMatch(ra -> ra.resourceParentId().equals(parentId));
    }

    List<ResourceAvailability> findBlockedBy(Owner owner) {
        return resourceAvailabilities
                .stream()
                .filter(ra -> ra.blockedBy().equals(owner))
                .collect(toList());
    }

    boolean isEntirelyAvailable() {
        return resourceAvailabilities
                .stream()
                .allMatch(ra -> ra.blockedBy().byNone());
    }

    boolean hasNoSlots() {
        return resourceAvailabilities.isEmpty();
    }

    Set<Owner> owners() {
        return resourceAvailabilities
                .stream()
                .map(ResourceAvailability::blockedBy)
                .collect(Collectors.toSet());
    }
}
