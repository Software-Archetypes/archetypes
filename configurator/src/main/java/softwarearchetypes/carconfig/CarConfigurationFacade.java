package softwarearchetypes.carconfig;

import softwarearchetypes.sat.Clause;
import softwarearchetypes.sat.DPLLSolver;

import java.util.*;
import java.util.stream.Stream;

public class CarConfigurationFacade {

    private final OptionsRepository optionsRepository;
    private final DPLLSolver dpllSolver;

    public CarConfigurationFacade(OptionsRepository optionsRepository, DPLLSolver dpllSolver) {
        this.optionsRepository = optionsRepository;
        this.dpllSolver = dpllSolver;
    }

    public boolean isProper(CarConfigId carConfigId, PickedOption ... pickedOptions) {
        List<Rule> rules = optionsRepository.loadRules(carConfigId);
        List<Clause> adminConfig = rules.stream().map(Rule::toClause).flatMap(Collection::stream).toList();
        List<Clause> userChoice = Arrays.asList(pickedOptions).stream().map(pickedOption -> new Clause(pickedOption.option().id())).toList();
        List<Clause> all = Stream.concat(adminConfig.stream(), userChoice.stream()).toList();
        all.forEach(System.out::println);
        return dpllSolver.solve(all, new HashMap<>());
    }
}


record PickedOption(Option option) {

}
