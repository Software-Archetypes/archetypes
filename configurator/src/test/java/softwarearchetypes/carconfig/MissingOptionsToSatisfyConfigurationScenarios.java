package softwarearchetypes.carconfig;

import org.junit.jupiter.api.Test;
import softwarearchetypes.sat.BooleanLogic;
import softwarearchetypes.sat.DPLLSolver;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class MissingOptionsToSatisfyConfigurationScenarios {
    static final CarConfigId DACIA = CarConfigId.random();

    static final Option SUNROOF = new Option(1);

    OptionsRepository optionsRepository = new OptionsRepository();

    CarConfigurationDefinitionFacade carConfigDefinitionFacade = new CarConfigurationDefinitionFacade(optionsRepository, new DPLLSolver());
    CarConfigurationFacade carConfigFacade = new CarConfigurationFacade(optionsRepository, new CarConfigurationProcessRepository(), new BooleanLogic(), new DPLLSolver());

    @Test
    void mustBeTakenOptionIsNotPicked() {
        //given
        carConfigDefinitionFacade.mustBeTaken(DACIA, SUNROOF);
        CarConfigProcessId carConfigProcessId = startedCarConfiguration(DACIA);

        //when
        Set<Option> missingOptions = carConfigFacade.getMissingOptions(carConfigProcessId);

        //expect
        assertThat(missingOptions).containsExactly(SUNROOF);
    }

    private CarConfigProcessId startedCarConfiguration(CarConfigId forCar) {
        CarConfigProcessId carConfigProcessId = CarConfigProcessId.random();
        carConfigFacade.start(carConfigProcessId, forCar);
        return carConfigProcessId;
    }
}
