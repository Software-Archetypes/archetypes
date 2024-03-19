package softwarearchetypes.multidimensionalknapsack;

public interface WeightDimension<T extends CapacityDimension> {
    boolean isSatisfiedBy(T capacityDimension);
}
