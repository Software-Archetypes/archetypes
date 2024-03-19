package softwarearchetypes.multidimensionalknapsack;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OptimizationForTimedCapabilitiesTest {

    MultidimensionalKnapsack knapsack = new MultidimensionalKnapsack();

    @Test
    void nothingIsChosenWhenNoCapabilitiesInTimeSlot() {
        //given
        TimeSlot june = TimeSlot.createMonthlyTimeSlotAtUTC(2020, 6);
        TimeSlot october = TimeSlot.createMonthlyTimeSlotAtUTC(2020, 10);

        List<Project> projects = List.of(
                new Project("Project1", 100,
                        List.of(new DemandedTimedCapability("COMMON SENSE", june))),
                new Project("Project2", 100,
                        List.of(new DemandedTimedCapability("THINKING", june))));

        //when
        Result result = knapsack.calculate(projectsToItems(projects), TotalCapacity.of(
                new AvailableTimedCapability("anna", "COMMON SENSE", october)
        ));

        //then
        assertEquals(0, result.profit(), 0.0d);
        assertEquals(0, result.chosenItems().size());
    }


    @Test
    void mostProfitableProjectIsChosen() {
        //given
        TimeSlot june = TimeSlot.createMonthlyTimeSlotAtUTC(2020, 6);

        List<Project> projects = List.of(
                new Project("Project1", 200,
                        List.of(new DemandedTimedCapability("COMMON SENSE", june))),
                new Project("Project2", 100,
                        List.of(new DemandedTimedCapability("THINKING", june))));

        //when
        Result result = knapsack.calculate(projectsToItems(projects), TotalCapacity.of(
                new AvailableTimedCapability("anna", "COMMON SENSE", june)
        ));

        //then
        assertEquals(200, result.profit(), 0.0d);
        assertEquals(1, result.chosenItems().size());
    }

    List<Item> projectsToItems(List<Project> items) {
        return items
                        .stream()
                        .map(Project::toItem)
                        .toList();
    }

}