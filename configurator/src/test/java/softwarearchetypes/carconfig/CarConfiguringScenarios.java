package softwarearchetypes.carconfig;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import softwarearchetypes.sat.DPLLSolver;

import java.util.List;


class CarConfiguringScenarios {

    static final CarConfigId DACIA = CarConfigId.random();

    static final Option SUNROOF = new Option(1);
    static final Option LEATHER_SEATS = new Option(2);
    static final Option NAVIGATION_SYSTEM = new Option(3);
    static final Option BLUETOOTH = new Option(4);
    static final Option PARKING_SENSORS = new Option(5);

    OptionsRepository optionsRepository = new OptionsRepository();

    CarConfigurationDefinitionFacade carConfigDefinitionFacade = new CarConfigurationDefinitionFacade(optionsRepository);
    CarConfigurationFacade carConfigFacade = new CarConfigurationFacade(optionsRepository, new DPLLSolver());

    @Test
    void cantBeTakenTogether() {
        //given
        carConfigDefinitionFacade.cantBeTakenTogether(DACIA, SUNROOF, LEATHER_SEATS);

        //expect
        assertTrue(carConfigFacade.isProper(DACIA, new PickedOption(SUNROOF)));
        assertTrue(carConfigFacade.isProper(DACIA, new PickedOption(LEATHER_SEATS)));
        assertFalse(carConfigFacade.isProper(DACIA, new PickedOption(SUNROOF), new PickedOption(LEATHER_SEATS)));
    }

    @Test
    void mustBeTakenTogether() {
        //given
        carConfigDefinitionFacade.mustBeTakenTogether(DACIA, SUNROOF, LEATHER_SEATS);

        //expect
        assertFalse(carConfigFacade.isProper(DACIA, new PickedOption(SUNROOF)));
        assertFalse(carConfigFacade.isProper(DACIA, new PickedOption(LEATHER_SEATS)));
        assertTrue(carConfigFacade.isProper(DACIA, new PickedOption(SUNROOF), new PickedOption(LEATHER_SEATS)));

    }

    @Test
    void includedConditionally() {
        //given
        carConfigDefinitionFacade.includedConditionally(DACIA, LEATHER_SEATS, List.of(BLUETOOTH, NAVIGATION_SYSTEM));

        //expect
        assertFalse(carConfigFacade.isProper(DACIA, new PickedOption(LEATHER_SEATS)));
        assertTrue(carConfigFacade.isProper(DACIA, new PickedOption(BLUETOOTH)));
        assertTrue(carConfigFacade.isProper(DACIA, new PickedOption(LEATHER_SEATS), new PickedOption(BLUETOOTH)));
        assertTrue(carConfigFacade.isProper(DACIA, new PickedOption(LEATHER_SEATS), new PickedOption(NAVIGATION_SYSTEM)));
        assertTrue(carConfigFacade.isProper(DACIA, new PickedOption(LEATHER_SEATS), new PickedOption(NAVIGATION_SYSTEM), new PickedOption(BLUETOOTH)));
    }

    @Test
    void excludedConditionally() {
        //given
        carConfigDefinitionFacade.excludedConditionally(DACIA, LEATHER_SEATS, List.of(BLUETOOTH, PARKING_SENSORS));

        //expect
        assertTrue(carConfigFacade.isProper(DACIA, new PickedOption(LEATHER_SEATS)));
        assertTrue(carConfigFacade.isProper(DACIA, new PickedOption(BLUETOOTH)));
        assertTrue(carConfigFacade.isProper(DACIA, new PickedOption(PARKING_SENSORS)));
        assertFalse(carConfigFacade.isProper(DACIA, new PickedOption(LEATHER_SEATS), new PickedOption(BLUETOOTH)));
        assertFalse(carConfigFacade.isProper(DACIA, new PickedOption(LEATHER_SEATS), new PickedOption(PARKING_SENSORS)));
        assertFalse(carConfigFacade.isProper(DACIA, new PickedOption(LEATHER_SEATS), new PickedOption(BLUETOOTH), new PickedOption(PARKING_SENSORS)));
    }
}
