package softwarearchetypes.multidimensionalknapsack;

public record Item(String name, double value, TotalWeight totalWeight) {

    boolean isWeightZero() {
        return totalWeight().components().isEmpty();
    }
}

