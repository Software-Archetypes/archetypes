package softwarearchetypes.carconfig;

import org.junit.jupiter.api.Test;
import softwarearchetypes.sat.DPLLSolver;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class MissingOptionsToSatisfyConfigurationScenarios {
    static final CarConfigId DACIA = CarConfigId.random();

    static final Option SUNROOF = new Option(1);
    static final Option LEATHER_SEATS = new Option(2);
    static final Option BLUETOOTH = new Option(3);
    static final Option PARKING_SENSORS = new Option(4);

    OptionsRepository optionsRepository = new OptionsRepository();

    CarConfigurationDefinitionFacade carConfigDefinitionFacade = new CarConfigurationDefinitionFacade(optionsRepository, new DPLLSolver());
    CarConfigurationFacade carConfigFacade = new CarConfigurationFacade(optionsRepository, new CarConfigurationProcessRepository(), new DPLLSolver());

    @Test
    void mustBeTakenOptionIsNotPicked() {
        //given
        carConfigDefinitionFacade.mustBeTaken(DACIA, SUNROOF);
        CarConfigProcessId carConfigProcessId = startedCarConfiguration(DACIA);

        //when
        Set<Option> missingOptions = carConfigFacade.getMissingOptions(carConfigProcessId);

        //then
        assertThat(missingOptions).containsExactly(SUNROOF);
    }

    @Test
    void severalMustBeTakenOptionsAreNotPicked() {
        //given
        carConfigDefinitionFacade.mustBeTaken(DACIA, SUNROOF);
        carConfigDefinitionFacade.mustBeTaken(DACIA, LEATHER_SEATS);
        CarConfigProcessId carConfigProcessId = startedCarConfiguration(DACIA);

        //when
        Set<Option> missingOptions = carConfigFacade.getMissingOptions(carConfigProcessId);

        //then
        assertThat(missingOptions).containsExactly(SUNROOF, LEATHER_SEATS);
    }

    @Test
    void anyOfMustBeTakenTogetherIsNotPicked_noneIsMissing() {
        //given
        carConfigDefinitionFacade.mustBeTakenTogether(DACIA, SUNROOF, LEATHER_SEATS);
        CarConfigProcessId carConfigProcessId = startedCarConfiguration(DACIA);

        //when
        Set<Option> missingOptions = carConfigFacade.getMissingOptions(carConfigProcessId);

        //then
        assertThat(missingOptions).isEmpty();
    }

    @Test
    void mustBeTakenTogether() {
        //given
        carConfigDefinitionFacade.mustBeTakenTogether(DACIA, SUNROOF, LEATHER_SEATS);
        CarConfigProcessId carConfigProcessId = startedCarConfiguration(DACIA);
        carConfigFacade.pickOption(carConfigProcessId, new PickedOption(SUNROOF));

        //when
        Set<Option> missingOptions = carConfigFacade.getMissingOptions(carConfigProcessId);

        //then
        assertThat(missingOptions).containsExactly(LEATHER_SEATS);
    }

    @Test
    void includedConditionally_ifThisTakenNotPicked_noneIsMissing() {
        //given
        carConfigDefinitionFacade.includedConditionally(DACIA, SUNROOF, List.of(LEATHER_SEATS));
        CarConfigProcessId carConfigProcessId = startedCarConfiguration(DACIA);

        //when
        Set<Option> missingOptions = carConfigFacade.getMissingOptions(carConfigProcessId);

        //then
        assertThat(missingOptions).isEmpty();
    }

    @Test
    void includedConditionally() {
        //given
        carConfigDefinitionFacade.includedConditionally(DACIA, SUNROOF, List.of(LEATHER_SEATS));
        CarConfigProcessId carConfigProcessId = startedCarConfiguration(DACIA);
        carConfigFacade.pickOption(carConfigProcessId, new PickedOption(SUNROOF));

        //when
        Set<Option> missingOptions = carConfigFacade.getMissingOptions(carConfigProcessId);

        //then
        assertThat(missingOptions).containsExactly(LEATHER_SEATS);
    }

    @Test
    void includedConditionally_intersectsWithExcludesConditionally() {
        //given
        carConfigDefinitionFacade.includedConditionally(DACIA, SUNROOF, List.of(LEATHER_SEATS, BLUETOOTH));
        carConfigDefinitionFacade.excludedConditionally(DACIA, PARKING_SENSORS, List.of(BLUETOOTH));
        CarConfigProcessId carConfigProcessId = startedCarConfiguration(DACIA);
        carConfigFacade.pickOption(carConfigProcessId, new PickedOption(SUNROOF));
        carConfigFacade.pickOption(carConfigProcessId, new PickedOption(PARKING_SENSORS));

        //when
        Set<Option> missingOptions = carConfigFacade.getMissingOptions(carConfigProcessId);

        //then
        assertThat(missingOptions).containsExactly(LEATHER_SEATS);
    }

    @Test
    void includedConditionally_intersectsWithCantBeTakenTogether() {
        //given
        carConfigDefinitionFacade.includedConditionally(DACIA, SUNROOF, List.of(LEATHER_SEATS, BLUETOOTH));
        carConfigDefinitionFacade.cantBeTakenTogether(DACIA, PARKING_SENSORS, BLUETOOTH);
        CarConfigProcessId carConfigProcessId = startedCarConfiguration(DACIA);
        carConfigFacade.pickOption(carConfigProcessId, new PickedOption(SUNROOF));
        carConfigFacade.pickOption(carConfigProcessId, new PickedOption(PARKING_SENSORS));

        //when
        Set<Option> missingOptions = carConfigFacade.getMissingOptions(carConfigProcessId);

        //then
        assertThat(missingOptions).containsExactly(LEATHER_SEATS);
    }

    @Test
    void oneOfMustBeTaken_intersectsWithCantBeTakenTogether() {
        //given
        carConfigDefinitionFacade.oneOfMustBeTaken(DACIA, List.of(LEATHER_SEATS, BLUETOOTH));
        carConfigDefinitionFacade.cantBeTakenTogether(DACIA, PARKING_SENSORS, BLUETOOTH);
        CarConfigProcessId carConfigProcessId = startedCarConfiguration(DACIA);
        carConfigFacade.pickOption(carConfigProcessId, new PickedOption(PARKING_SENSORS));

        //when
        Set<Option> missingOptions = carConfigFacade.getMissingOptions(carConfigProcessId);

        //then
        assertThat(missingOptions).containsExactly(LEATHER_SEATS);
    }

    @Test
    void oneOfMustBeTaken_intersectsWithExcludedConditionally() {
        //given
        carConfigDefinitionFacade.oneOfMustBeTaken(DACIA, List.of(LEATHER_SEATS, BLUETOOTH));
        carConfigDefinitionFacade.excludedConditionally(DACIA, PARKING_SENSORS, List.of(BLUETOOTH));
        CarConfigProcessId carConfigProcessId = startedCarConfiguration(DACIA);
        carConfigFacade.pickOption(carConfigProcessId, new PickedOption(PARKING_SENSORS));

        //when
        Set<Option> missingOptions = carConfigFacade.getMissingOptions(carConfigProcessId);

        //then
        assertThat(missingOptions).containsExactly(LEATHER_SEATS);
    }

    private CarConfigProcessId startedCarConfiguration(CarConfigId forCar) {
        CarConfigProcessId carConfigProcessId = CarConfigProcessId.random();
        carConfigFacade.start(carConfigProcessId, forCar);
        return carConfigProcessId;
    }
}
