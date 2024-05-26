package com.softwarearchetypes.availability;

import com.softwarearchetypes.MockedEventPublisherConfiguration;
import com.softwarearchetypes.TestDbConfiguration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import com.softwarearchetypes.availability.segment.Segments;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = { TestDbConfiguration.class, MockedEventPublisherConfiguration.class})
@Sql(scripts = "classpath:schema-availability.sql")
public class AvailabilityCalendarTest {

    @Autowired
    AvailabilityFacade availabilityFacade;

    @Test
    void loadsCalendarForEntireMonth() {
        //given
        ResourceId resourceId = ResourceId.newOne();
        Duration durationOfSevenSlots = Duration.ofMinutes(7 * Segments.DEFAULT_SEGMENT_DURATION_IN_MINUTES);
        TimeSlot sevenSlots = TimeSlot.createTimeSlotAtUTCOfDuration(2021, 1, 1, durationOfSevenSlots);
        TimeSlot minimumSlot = new TimeSlot(sevenSlots.from(), sevenSlots.from().plus(Segments.DEFAULT_SEGMENT_DURATION_IN_MINUTES, ChronoUnit.MINUTES));
        Owner owner = Owner.newOne();
        //and
        availabilityFacade.createResourceSlots(resourceId, sevenSlots);

        //when
        availabilityFacade.block(resourceId, minimumSlot, owner);

        //then
        Calendar calendar = availabilityFacade.loadCalendar(resourceId, sevenSlots);
        assertThat(calendar.takenBy(owner)).containsExactly(minimumSlot);
        assertThat(calendar.availableSlots()).containsExactlyInAnyOrderElementsOf(sevenSlots.leftoverAfterRemovingCommonWith(minimumSlot));
    }

    @Test
    void loadsCalendarForMultipleResources() {
        //given
        ResourceId resourceId = ResourceId.newOne();
        ResourceId resourceId2 = ResourceId.newOne();
        Duration durationOfSevenSlots = Duration.ofMinutes(7 * Segments.DEFAULT_SEGMENT_DURATION_IN_MINUTES);
        TimeSlot sevenSlots = TimeSlot.createTimeSlotAtUTCOfDuration(2021, 1, 1, durationOfSevenSlots);
        TimeSlot minimumSlot = new TimeSlot(sevenSlots.from(), sevenSlots.from().plus(Segments.DEFAULT_SEGMENT_DURATION_IN_MINUTES, ChronoUnit.MINUTES));

        Owner owner = Owner.newOne();
        availabilityFacade.createResourceSlots(resourceId, sevenSlots);
        availabilityFacade.createResourceSlots(resourceId2, sevenSlots);

        //when
        availabilityFacade.block(resourceId, minimumSlot, owner);
        availabilityFacade.block(resourceId2, minimumSlot, owner);

        //then
        Calendars calendars = availabilityFacade.loadCalendars(Set.of(resourceId, resourceId2), sevenSlots);
        Assertions.assertThat(calendars.get(resourceId).takenBy(owner)).containsExactly(minimumSlot);
        Assertions.assertThat(calendars.get(resourceId2).takenBy(owner)).containsExactly(minimumSlot);
        Assertions.assertThat(calendars.get(resourceId).availableSlots()).containsExactlyInAnyOrderElementsOf(sevenSlots.leftoverAfterRemovingCommonWith(minimumSlot));
        Assertions.assertThat(calendars.get(resourceId2).availableSlots()).containsExactlyInAnyOrderElementsOf(sevenSlots.leftoverAfterRemovingCommonWith(minimumSlot));
    }


}