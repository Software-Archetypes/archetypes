package softwarearchetypes.carconfig;

import org.junit.jupiter.api.Test;
import softwarearchetypes.sat.BooleanLogic;
import softwarearchetypes.sat.DPLLSolver;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ClientConfigurationProcessScenarios {
    static final CarConfigId DACIA = CarConfigId.random();

    static final Option SUNROOF = new Option(1);
    static final Option LEATHER_SEATS = new Option(2);
    static final Option NAVIGATION_SYSTEM = new Option(3);
    static final Option BLUETOOTH = new Option(4);
    static final Option PARKING_SENSORS = new Option(5);

    OptionsRepository optionsRepository = new OptionsRepository();
    CarConfigurationProcessRepository carConfigurationProcessRepository = new CarConfigurationProcessRepository();

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
    void userPicksOptionA_optionAExcludesOptionB_BIsBlocked() {
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

//    @Test
//    void userPicksOptionA_optionAExcludesOptionB_BIsBlocked() {
//        //given
//        CarConfigProcessId carConfigProcessId = CarConfigProcessId.random();
//        carConfigDefinitionFacade.mustBeTakenTogether(DACIA, PARKING_SENSORS, LEATHER_SEATS);
//        carConfigDefinitionFacade.cantBeTakenTogether(DACIA, SUNROOF, BLUETOOTH);
//        carConfigFacade.start(carConfigProcessId);
//
//        //when
//        carConfigFacade.pickOption(carConfigProcessId, new PickedOption(SUNROOF));
//
//        //expect
//        List<Option> availablePicks = carConfigFacade.availableOptions(carConfigProcessId);
//        assertThat(availablePicks).containsExactlyInAnyOrderElementsOf(List.of(PARKING_SENSORS, LEATHER_SEATS));
//        assertThrows(IllegalArgumentException.class,
//                () -> carConfigFacade.pickOption(carConfigProcessId, new PickedOption(BLUETOOTH)),
//                "Cannot pick blocked option");
//    }
}
