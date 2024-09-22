package softwarearchetypes.carconfig;

import softwarearchetypes.sat.Clause;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CarConfigurationDefinitionFacade {

    private final OptionsRepository optionsRepository;

    public CarConfigurationDefinitionFacade(OptionsRepository optionsRepository) {
        this.optionsRepository = optionsRepository;
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
        addOptions(carConfigId, cantBeTakenTogether);
        optionsRepository.addRules(carConfigId, excludes);
    }

    public void mustBeTakenTogether(CarConfigId carConfigId, Option a, Option b) {
        mustBeTakenTogether(carConfigId, List.of(a, b));
    }

    public void includedConditionally(CarConfigId carConfigId, Option ifThisTaken, List<Option> oneOfMustBeTaken) {
        optionsRepository.addRule(carConfigId, new IncludeOneOfRule(ifThisTaken, oneOfMustBeTaken));
        addOptions(carConfigId, oneOfMustBeTaken);
        addOption(carConfigId, ifThisTaken);
    }

    public void excludedConditionally(CarConfigId carConfigId, Option ifThisTaken, List<Option> cantBeTaken) {
        List<Rule> excludes = new ArrayList<>();
        for (Option toExclude : cantBeTaken) {
            excludes.add(new ExcludeRule(ifThisTaken, toExclude));
        }
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
        addOptions(carConfigId, mustBeTakenTogether);
        optionsRepository.addRules(carConfigId, includes);

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
