package softwarearchetypes.multidimensionalknapsack;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class KnapsackTest {

    MultidimensionalKnapsack knapsack = new MultidimensionalKnapsack();

    @Test
    void nothingIsChosenWhenNoCapacities() {
        //given
        List<Item> items = List.of(
                new Item("Item1", 100, TotalWeight.of(new DemandedCapability("COMMON SENSE"))),
                new Item("Item2", 100, TotalWeight.of(new DemandedCapability("THINKING"))));

        //when
        Result result = knapsack.calculate(items, TotalCapacity.zero());

        //then
        assertEquals(0, result.profit(), 0.0d);
        assertEquals(0, result.chosenItems().size());
    }

    @Test
    void everythingIsChosenWhenAllWeightsAreZero() {
        //given
        List<Item> items = List.of(
                new Item("Item1", 200, TotalWeight.zero()),
                new Item("Item2", 100, TotalWeight.zero()));

        //when
        Result result = knapsack.calculate(items, TotalCapacity.zero());

        //then
        assertEquals(300, result.profit(), 0.0d);
        assertEquals(2, result.chosenItems().size());
    }

    @Test
    void ifEnoughCapacityAllItemsAreChosen() {
        //given
        List<Item> items = List.of(
                new Item("Item1", 100, TotalWeight.of(new DemandedCapability("WEB DEVELOPMENT"))),
                new Item("Item2", 300, TotalWeight.of(new DemandedCapability("WEB DEVELOPMENT"))));
        CapacityDimension c1 = new TimedCapability("anna", "WEB DEVELOPMENT");
        CapacityDimension c2 = new TimedCapability("zbyniu", "WEB DEVELOPMENT");

        //when
        Result result = knapsack.calculate(items, TotalCapacity.of(c1, c2));

        //then
        assertEquals(400, result.profit(), 0.0d);
        assertEquals(2, result.chosenItems().size());
    }

    @Test
    void mostValuableItemsAreChosen() {
        //given
        Item item1 = new Item("Item1", 100, TotalWeight.of(new DemandedCapability("JAVA")));
        Item item2 = new Item("Item2", 500, TotalWeight.of(new DemandedCapability("JAVA")));
        Item item3 = new Item("Item3", 300, TotalWeight.of(new DemandedCapability("JAVA")));
        CapacityDimension c1 = new TimedCapability("anna", "JAVA");
        CapacityDimension c2 = new TimedCapability("zbyniu", "JAVA");

        //when
        Result result = knapsack.calculate(List.of(item1, item2, item3), TotalCapacity.of(c1, c2));

        //then
        assertEquals(800, result.profit(), 0.0d);
        assertEquals(2, result.chosenItems().size());
        assertThat(result.itemToCapacities().get(item3)).hasSize(1);
        assertThat(result.itemToCapacities().get(item3)).containsAnyElementsOf(List.of(c1, c2));
        assertThat(result.itemToCapacities().get(item2)).hasSize(1);
        assertThat(result.itemToCapacities().get(item2)).containsAnyElementsOf(List.of(c1, c2));
        assertThat(result.itemToCapacities().get(item1)).isNull();
    }

}