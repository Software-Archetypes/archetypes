package softwarearchetypes.carconfig;

import softwarearchetypes.sat.Clause;
import softwarearchetypes.sat.DPLLSolver;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CarConfigurationFacade {

    private final OptionsRepository optionsRepository;
    private final DPLLSolver dpllSolver;

    public CarConfigurationFacade(OptionsRepository optionsRepository, DPLLSolver dpllSolver) {
        this.optionsRepository = optionsRepository;
        this.dpllSolver = dpllSolver;
    }

    public boolean isProper(CarConfigId carConfigId, PickedOption... pickedOptions) {
        List<Rule> rules = optionsRepository.loadRules(carConfigId);
        List<Clause> adminConfig = rules.stream().map(Rule::toClause).flatMap(Collection::stream).toList();
        adminConfig.forEach(System.out::println);
        return dpllSolver.isClauseSatisfied(adminConfig,
                Arrays.asList(pickedOptions).stream().map(pickedOption -> pickedOption.option().id()).collect(Collectors.toSet()));
    }
}


record PickedOption(Option option) {

}
