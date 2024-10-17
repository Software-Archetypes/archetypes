package softwarearchetypes.carconfig;

import softwarearchetypes.sat.BooleanLogic;
import softwarearchetypes.sat.Clause;
import softwarearchetypes.sat.DPLLSolver;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CarConfigurationFacade {

    private final OptionsRepository optionsRepository;
    private final CarConfigurationProcessRepository configurationProcessRepository;
    private final BooleanLogic booleanLogic;
    private final DPLLSolver dpllSolver;

    public CarConfigurationFacade(OptionsRepository optionsRepository, CarConfigurationProcessRepository configurationProcessRepository, BooleanLogic booleanLogic, DPLLSolver dpllSolver) {
        this.optionsRepository = optionsRepository;
        this.configurationProcessRepository = configurationProcessRepository;
        this.booleanLogic = booleanLogic;
        this.dpllSolver = dpllSolver;
    }

    public void pickOption(CarConfigProcessId carConfigProcessId, PickedOption pickedOption) {
        CarConfigurationProcess carConfigurationProcess = configurationProcessRepository.load(carConfigProcessId);
        List<Clause> picked = carConfigurationProcess.pickedOptionsClauses();
        List<Clause> pickedWithNew = Stream.concat(
                picked.stream(),
                Stream.of(new Clause(pickedOption.option().id()))
        ).collect(Collectors.toList());
        List<Clause> all = Stream.concat(pickedWithNew.stream(),
                carConfigurationProcess.rules().stream().map(rule -> rule.toClause()).flatMap(Collection::stream).toList().stream()).toList();

        boolean satisfiableAfterPick = this.dpllSolver.solve(all, new HashMap<>());
        if (!satisfiableAfterPick) throw new IllegalArgumentException("Configuration is not satisfiable after pick!");
        carConfigurationProcess.pick(pickedOption);
    }

    public void start(CarConfigProcessId carConfigProcessId, CarConfigId carConfigId) {
        List<Rule> rules = optionsRepository.loadRules(carConfigId);
        List<Option> options = optionsRepository.load(carConfigId);
        CarConfigurationProcess newCarConfigurationProcess = CarConfigurationProcess.start(rules, options);
        configurationProcessRepository.addProcess(carConfigProcessId, newCarConfigurationProcess);
    }

    public List<Option> availableOptions(CarConfigProcessId carConfigProcessId) {
        CarConfigurationProcess carConfigurationProcess = configurationProcessRepository.load(carConfigProcessId);
        return carConfigurationProcess.availableOptions();
    }
}

record PickedOption(Option option) {

}
