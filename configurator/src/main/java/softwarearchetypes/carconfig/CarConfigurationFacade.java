package softwarearchetypes.carconfig;

import softwarearchetypes.sat.Clause;
import softwarearchetypes.sat.DPLLSolver;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CarConfigurationFacade {

    private final OptionsRepository optionsRepository;
    private final CarConfigurationProcessRepository configurationProcessRepository;
    private final DPLLSolver dpllSolver;

    public CarConfigurationFacade(OptionsRepository optionsRepository, CarConfigurationProcessRepository configurationProcessRepository, DPLLSolver dpllSolver) {
        this.optionsRepository = optionsRepository;
        this.configurationProcessRepository = configurationProcessRepository;
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

        //SAT is used to check if the defition of a confiuration is satisafable
        //here we check if certain choise is proper
        //which means SAT is NOT needed, we use it as an experiment, to maybe get an answer to a question: what should I remove/add to fulffil all the rules?

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

    public Set<Option> getMissingOptions(CarConfigProcessId carConfigProcessId) {
        CarConfigurationProcess carConfigurationProcess = configurationProcessRepository.load(carConfigProcessId);
        List<Clause> pickedOptionsWithRules = Stream.concat(carConfigurationProcess.rules().stream()
                        .map(rule -> rule.toClause()).flatMap(Collection::stream),
                carConfigurationProcess.pickedOptionsClauses().stream()).collect(Collectors.toList());

        Set<Option> missingOptions = new HashSet<>();
        for (Option nonPickedOption : carConfigurationProcess.getNonPickedOptions()) {
            Clause positiveClause = new Clause(nonPickedOption.id());
            Clause negativeClause = new Clause(-nonPickedOption.id());

            boolean canBeTrue = this.dpllSolver.solve(Stream.concat(
                    pickedOptionsWithRules.stream(),
                    Stream.of(positiveClause)
            ).collect(Collectors.toList()), new HashMap<>());

            boolean canBeFalse = this.dpllSolver.solve(Stream.concat(
                    pickedOptionsWithRules.stream(),
                    Stream.of(negativeClause)
            ).collect(Collectors.toList()), new HashMap<>());

            if (canBeTrue && !canBeFalse) missingOptions.add(nonPickedOption);
        }

        return missingOptions;
    }
}

record PickedOption(Option option) {

}
