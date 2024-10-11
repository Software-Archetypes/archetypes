package softwarearchetypes.carconfig;

import softwarearchetypes.sat.BooleanLogic;
import softwarearchetypes.sat.Clause;
import softwarearchetypes.sat.DPLLSolver;

import java.util.*;
import java.util.stream.Collectors;

public class CarConfigurationFacade {

    private final OptionsRepository optionsRepository;
    private final BooleanLogic booleanLogic;

    public CarConfigurationFacade(OptionsRepository optionsRepository, BooleanLogic booleanLogic) {
        this.optionsRepository = optionsRepository;
        this.booleanLogic = booleanLogic;
    }

    public boolean isProper(CarConfigId carConfigId, PickedOption ... pickedOptions) {
        List<Rule> rules = optionsRepository.loadRules(carConfigId);
        List<Clause> adminConfig = rules.stream().map(Rule::toClause).flatMap(Collection::stream).toList();
        return booleanLogic.isFormulaSatisfied(adminConfig,
                Arrays.asList(pickedOptions).stream().map(pickedOption -> pickedOption.option().id()).collect(Collectors.toSet()));
    }
}


record PickedOption(Option option) {

}
