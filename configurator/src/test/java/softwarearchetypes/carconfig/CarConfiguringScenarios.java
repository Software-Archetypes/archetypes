package softwarearchetypes.carconfig;

import org.junit.jupiter.api.Test;
import softwarearchetypes.sat.BooleanLogic;
import softwarearchetypes.sat.DPLLSolver;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CarConfiguringScenarios {
    static final CarConfigId DACIA = CarConfigId.random();

    static final Option SUNROOF = new Option(1);
    static final Option LEATHER_SEATS = new Option(2);
    static final Option BLUETOOTH = new Option(3);
    static final Option PARKING_SENSORS = new Option(4);

    OptionsRepository optionsRepository = new OptionsRepository();

    CarConfigurationDefinitionFacade carConfigDefinitionFacade = new CarConfigurationDefinitionFacade(optionsRepository, new DPLLSolver());
    CarConfigurationFacade carConfigFacade = new CarConfigurationFacade(optionsRepository, new CarConfigurationProcessRepository(), new BooleanLogic(), new DPLLSolver());

    @Test
    void userCannotPick_IfProcessIsNotStarted() {
        //given
        carConfigDefinitionFacade.cantBeTakenTogether(DACIA, SUNROOF, BLUETOOTH);
        CarConfigProcessId carConfigProcessId = CarConfigProcessId.random();

        //then
        assertThrows(IllegalArgumentException.class, () -> carConfigFacade.pickOption(carConfigProcessId, new PickedOption(SUNROOF)));
    }

    @Test
    void userPicksOptionA_optionAExcludesOptionB_BCannotBePicked() {
        //given
        CarConfigProcessId carConfigProcessId = CarConfigProcessId.random();
        carConfigDefinitionFacade.excludedConditionally(DACIA, PARKING_SENSORS, List.of(LEATHER_SEATS));
        carConfigFacade.start(carConfigProcessId, DACIA);

        //when
        carConfigFacade.pickOption(carConfigProcessId, new PickedOption(PARKING_SENSORS));

        //expect
        assertThrows(IllegalArgumentException.class,
                () -> carConfigFacade.pickOption(carConfigProcessId, new PickedOption(LEATHER_SEATS)),
                "Configuration is not satisfiable after pick!");
    }

    @Test
    void userPicksOptionA_optionACantBeTakenWithOptionB_BCannotBePicked() {
        //given
        CarConfigProcessId carConfigProcessId = CarConfigProcessId.random();
        carConfigDefinitionFacade.cantBeTakenTogether(DACIA, PARKING_SENSORS, LEATHER_SEATS);
        carConfigFacade.start(carConfigProcessId, DACIA);

        //when
        carConfigFacade.pickOption(carConfigProcessId, new PickedOption(PARKING_SENSORS));

        //expect
        assertThrows(IllegalArgumentException.class,
                () -> carConfigFacade.pickOption(carConfigProcessId, new PickedOption(LEATHER_SEATS)),
                "Configuration is not satisfiable after pick!");
    }
}
