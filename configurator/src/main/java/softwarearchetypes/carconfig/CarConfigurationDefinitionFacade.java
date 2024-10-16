package softwarearchetypes.carconfig;

import softwarearchetypes.sat.Clause;
import softwarearchetypes.sat.DPLLSolver;

import java.util.*;
import java.util.stream.Stream;

public class CarConfigurationDefinitionFacade {

    private final OptionsRepository optionsRepository;
    private final DPLLSolver dpllSolver;

    public CarConfigurationDefinitionFacade(OptionsRepository optionsRepository, DPLLSolver dpllSolver) {
        this.optionsRepository = optionsRepository;
        this.dpllSolver = dpllSolver;
    }

    public void addOption(CarConfigId carConfigId, Option option) {
        optionsRepository.addOption(carConfigId, option);
    }

    public void removeOption(CarConfigId carConfigId, Option option) {
        optionsRepository.deleteOption(carConfigId, option);
    }

    public void addOptions(CarConfigId carConfigId, List<Option> options) {
        options.forEach(option -> this.addOption(carConfigId, option));
    }

    public void cantBeTakenTogether(CarConfigId carConfigId, Option a, Option b) {
        cantBeTakenTogether(carConfigId, List.of(a, b));
    }

    public void cantBeTakenTogether(CarConfigId carConfigId, List<Option> cantBeTakenTogether) {
        List<Rule> excludes = new ArrayList<>();
        for (Option option : cantBeTakenTogether) {
            for (Option exclude : cantBeTakenTogether) {
                if (!exclude.equals(option)) {
                    excludes.add(new ExcludeRule(option, exclude));
                }
            }
        }

        checkSatisfiability(carConfigId, excludes);
        addOptions(carConfigId, cantBeTakenTogether);
        optionsRepository.addRules(carConfigId, excludes);
    }

    public void mustBeTaken(CarConfigId carConfigId, Option mustBePresent) {
        oneOfMustBeTaken(carConfigId, List.of(mustBePresent));
    }

    public void oneOfMustBeTaken(CarConfigId carConfigId, List<Option> oneOfMustBeTaken) {
        Rule oneOfPresence = new OneOfPresenceRule(oneOfMustBeTaken);

        checkSatisfiability(carConfigId, List.of(oneOfPresence));
        addOptions(carConfigId, oneOfMustBeTaken);
        optionsRepository.addRule(carConfigId, oneOfPresence);
    }

    public void mustBeTakenTogether(CarConfigId carConfigId, Option a, Option b) {
        mustBeTakenTogether(carConfigId, List.of(a, b));
    }

    public void includedConditionally(CarConfigId carConfigId, Option ifThisTaken, List<Option> oneOfMustBeTaken) {
        Rule include = new IncludeOneOfRule(ifThisTaken, oneOfMustBeTaken);

        checkSatisfiability(carConfigId, List.of(include));
        optionsRepository.addRule(carConfigId, include);
        addOptions(carConfigId, oneOfMustBeTaken);
        addOption(carConfigId, ifThisTaken);
    }

    public void excludedConditionally(CarConfigId carConfigId, Option ifThisTaken, List<Option> cantBeTaken) {
        List<Rule> excludes = new ArrayList<>();
        for (Option toExclude : cantBeTaken) {
            excludes.add(new ExcludeRule(ifThisTaken, toExclude));
        }

        checkSatisfiability(carConfigId, excludes);
        addOptions(carConfigId, cantBeTaken);
        addOption(carConfigId, ifThisTaken);
        optionsRepository.addRules(carConfigId, excludes);
    }

    public void mustBeTakenTogether(CarConfigId carConfigId, List<Option> mustBeTakenTogether) {
        List<Rule> includes = new ArrayList<>();
        for (Option option : mustBeTakenTogether) {
            for (Option pairWith : mustBeTakenTogether) {
                if (!pairWith.equals(option)) {
                    includes.add(new IncludeRule(option, pairWith));
                }
            }
        }

        checkSatisfiability(carConfigId, includes);
        addOptions(carConfigId, mustBeTakenTogether);
        optionsRepository.addRules(carConfigId, includes);
    }

    private void checkSatisfiability(CarConfigId carConfigId, List<Rule> newRules) {
        List<Rule> rules = optionsRepository.loadRules(carConfigId);
        List<Clause> existingClauses = rules.stream().map(Rule::toClause).flatMap(Collection::stream).toList();
        List<Clause> newClauses = newRules.stream().map(Rule::toClause).flatMap(Collection::stream).toList();
        List<Clause> all = Stream.concat(existingClauses.stream(), newClauses.stream()).toList();
        all.forEach(System.out::println);
        boolean isSatisfiable = dpllSolver.solve(all, new HashMap<>());
        if(!isSatisfiable) throw new IllegalArgumentException("Configuration is not satisfiable");
    }
}


record Option(Integer id) {

}

record IncludeOneOfRule(Option ifTaken, List<Option> oneHasToBeTaken) implements Rule {
    @Override
    public List<Clause> toClause() {
        return List.of(
                new Clause(
                        Stream.concat(
                                        Stream.of(-ifTaken.id()),
                                        oneHasToBeTaken.stream().map(Option::id))
                                .toList()));
    }
}

record IncludeRule(Option ifTaken, Option mustBeTakenToo) implements Rule {
    @Override
    public List<Clause> toClause() {
        return List.of(new Clause(-ifTaken.id(), mustBeTakenToo.id()));
    }
}

record ExcludeRule(Option ifTaken, Option cantBeTaken) implements Rule {

    @Override
    public List<Clause> toClause() {
        return List.of(new Clause(-ifTaken.id(), -cantBeTaken.id()));
    }
}

record OneOfPresenceRule(List<Option> options) implements Rule {
    @Override
    public List<Clause> toClause() {
        return List.of(
                new Clause(
                        options.stream()
                                .map(Option::id)
                                .toList())
        );
    }
}
