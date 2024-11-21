package softwarearchetypes.carconfig;

import org.junit.jupiter.api.Test;
import softwarearchetypes.sat.BooleanLogic;
import softwarearchetypes.sat.DPLLSolver;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MissingRulesToSatisfyConfigurationScenarios {
    static final CarConfigId DACIA = CarConfigId.random();

    static final Option SUNROOF = new Option(1);
    static final Option LEATHER_SEATS = new Option(2);
    static final Option PARKING_SENSORS = new Option(5);

    OptionsRepository optionsRepository = new OptionsRepository();

    CarConfigurationDefinitionFacade carConfigDefinitionFacade = new CarConfigurationDefinitionFacade(optionsRepository, new DPLLSolver());
    CarConfigurationFacade carConfigFacade = new CarConfigurationFacade(optionsRepository, new CarConfigurationProcessRepository(), new BooleanLogic(), new DPLLSolver());

    @Test
    void mustBeTakenOptionIsNotPicked() {
        //given
        carConfigDefinitionFacade.mustBeTaken(DACIA, SUNROOF);
        CarConfigProcessId carConfigProcessId = startedCarConfiguration(DACIA);

        //when
        List<Rule> rules = carConfigFacade.getNotSatisfiedRules(carConfigProcessId);

        //expect
        assertThat(rules).containsExactly(new );
        assertThrows(IllegalArgumentException.class,
                () -> carConfigFacade.pickOption(carConfigProcessId, new PickedOption(LEATHER_SEATS)),
                "Configuration is not satisfiable after pick!");
    }

    private CarConfigProcessId startedCarConfiguration(CarConfigId forCar) {
        CarConfigProcessId carConfigProcessId = CarConfigProcessId.random();
        carConfigFacade.start(carConfigProcessId, DACIA);
        return carConfigProcessId;
    }
}
