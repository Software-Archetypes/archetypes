package softwarearchetypes.carconfig;

import org.junit.jupiter.api.Test;
import softwarearchetypes.sat.DPLLSolver;

import static org.junit.jupiter.api.Assertions.*;

public class NotSatisfiableConfigurationScenarios {
    static final CarConfigId DACIA = CarConfigId.random();

    static final Option SUNROOF = new Option(1);
    static final Option LEATHER_SEATS = new Option(2);

    OptionsRepository optionsRepository = new OptionsRepository();

    CarConfigurationDefinitionFacade carConfigDefinitionFacade = new CarConfigurationDefinitionFacade(optionsRepository, new DPLLSolver());

    @Test
    void mustBeTakenTogetherExcludesCantBeTakenTogether() {
        //given
        carConfigDefinitionFacade.mustBeTaken(DACIA, SUNROOF);
        carConfigDefinitionFacade.mustBeTakenTogether(DACIA, SUNROOF, LEATHER_SEATS);

        //then
        assertThrows(IllegalArgumentException.class,
                () -> carConfigDefinitionFacade.cantBeTakenTogether(DACIA, SUNROOF, LEATHER_SEATS),
                "Configuration is not satisfiable");

    }

    @Test
    void cantBeTakenTogetherExcludesMustBeTakenTogether() {
        //given
        carConfigDefinitionFacade.mustBeTaken(DACIA, SUNROOF);
        carConfigDefinitionFacade.cantBeTakenTogether(DACIA, SUNROOF, LEATHER_SEATS);

        //then
        assertThrows(IllegalArgumentException.class,
                () -> carConfigDefinitionFacade.mustBeTakenTogether(DACIA, SUNROOF, LEATHER_SEATS),
                "Configuration is not satisfiable");
    }

    @Test
    void cantBeTakenTogetherExcludesMustBeTaken() {
        //given
        carConfigDefinitionFacade.mustBeTaken(DACIA, SUNROOF);
        carConfigDefinitionFacade.cantBeTakenTogether(DACIA, SUNROOF, LEATHER_SEATS);

        //then
        assertThrows(IllegalArgumentException.class,
                () -> carConfigDefinitionFacade.mustBeTaken(DACIA, LEATHER_SEATS),
                "Configuration is not satisfiable");
    }
}
