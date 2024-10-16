package softwarearchetypes.carconfig;

import org.junit.jupiter.api.Test;
import softwarearchetypes.sat.DPLLSolver;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NotSatisfiableConfigurationScenarios {
    static final CarConfigId DACIA = CarConfigId.random();

    static final Option SUNROOF = new Option(1);
    static final Option LEATHER_SEATS = new Option(2);
    static final Option NAVIGATION_SYSTEM = new Option(3);
    static final Option BLUETOOTH = new Option(4);

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

    @Test
    void mustBeTakenExcludesCanBeTakenTogether() {
        //given
        carConfigDefinitionFacade.mustBeTaken(DACIA, SUNROOF);
        carConfigDefinitionFacade.mustBeTaken(DACIA, LEATHER_SEATS);

        //then
        assertThrows(IllegalArgumentException.class,
                () -> carConfigDefinitionFacade.cantBeTakenTogether(DACIA, SUNROOF, LEATHER_SEATS),
                "Configuration is not satisfiable");
    }

    @Test
    void excludedConditionallyExcludesMustBeTaken() {
        //given
        carConfigDefinitionFacade.excludedConditionally(DACIA, SUNROOF, List.of(LEATHER_SEATS));
        carConfigDefinitionFacade.mustBeTaken(DACIA, SUNROOF);

        //then
        assertThrows(IllegalArgumentException.class,
                () -> carConfigDefinitionFacade.mustBeTaken(DACIA, LEATHER_SEATS),
                "Configuration is not satisfiable");
    }

    @Test
    void mustBeTakenExcludesExcludedConditionally() {
        //given
        carConfigDefinitionFacade.mustBeTaken(DACIA, SUNROOF);
        carConfigDefinitionFacade.mustBeTaken(DACIA, LEATHER_SEATS);

        //then
        assertThrows(IllegalArgumentException.class,
                () -> carConfigDefinitionFacade.excludedConditionally(DACIA, SUNROOF, List.of(LEATHER_SEATS)),
                "Configuration is not satisfiable");
    }

    @Test
    void cantBeTakenTogetherExcludesIncludesConditionally() {
        //given
        carConfigDefinitionFacade.mustBeTaken(DACIA, SUNROOF);
        carConfigDefinitionFacade.cantBeTakenTogether(DACIA, SUNROOF, LEATHER_SEATS);

        //then
        assertThrows(IllegalArgumentException.class,
                () -> carConfigDefinitionFacade.includedConditionally(DACIA, SUNROOF, List.of(LEATHER_SEATS)),
                "Configuration is not satisfiable");
    }

    @Test
    void includesConditionallyExcludesCantBeTakenTogether() {
        //given
        carConfigDefinitionFacade.mustBeTaken(DACIA, SUNROOF);
        carConfigDefinitionFacade.includedConditionally(DACIA, SUNROOF, List.of(LEATHER_SEATS));

        //then
        assertThrows(IllegalArgumentException.class,
                () -> carConfigDefinitionFacade.cantBeTakenTogether(DACIA, SUNROOF, LEATHER_SEATS),
                "Configuration is not satisfiable");
    }

    @Test
    void chainOfIncludesConditionallyExcludesCantBeTakenTogether() {
        //given
        carConfigDefinitionFacade.mustBeTaken(DACIA, SUNROOF);
        carConfigDefinitionFacade.includedConditionally(DACIA, SUNROOF, List.of(LEATHER_SEATS));
        carConfigDefinitionFacade.includedConditionally(DACIA, LEATHER_SEATS, List.of(NAVIGATION_SYSTEM));
        carConfigDefinitionFacade.includedConditionally(DACIA, NAVIGATION_SYSTEM, List.of(BLUETOOTH));

        //then
        assertThrows(IllegalArgumentException.class,
                () -> carConfigDefinitionFacade.cantBeTakenTogether(DACIA, SUNROOF, BLUETOOTH),
                "Configuration is not satisfiable");
    }

    @Test
    void chainOfIncludedConditionallyWithEventualExcludedExcludesMustBeTakenTogether() {
        //given
        carConfigDefinitionFacade.mustBeTaken(DACIA, SUNROOF);
        carConfigDefinitionFacade.includedConditionally(DACIA, SUNROOF, List.of(LEATHER_SEATS));
        carConfigDefinitionFacade.includedConditionally(DACIA, LEATHER_SEATS, List.of(NAVIGATION_SYSTEM));
        carConfigDefinitionFacade.excludedConditionally(DACIA, NAVIGATION_SYSTEM, List.of(BLUETOOTH));

        //then
        assertThrows(IllegalArgumentException.class,
                () -> carConfigDefinitionFacade.mustBeTakenTogether(DACIA, SUNROOF, BLUETOOTH),
                "Configuration is not satisfiable");
    }
}
