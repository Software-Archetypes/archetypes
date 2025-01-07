package softwarearchetypes.carconfig;

import softwarearchetypes.sat.Clause;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CarConfigurationProcess {
    private Set<Option> blockedOptions;
    private Set<PickedOption> pickedOptions;
    private Set<Option> possibleOptions;
    private Set<Rule> rulesInDuty;

    private CarConfigurationProcess(Set<Option> blockedOptions,
                                    Set<PickedOption> pickedOptions,
                                    Set<Option> possibleOptions,
                                    Set<Rule> rulesInDuty) {
        this.blockedOptions = blockedOptions;
        this.pickedOptions = pickedOptions;
        this.possibleOptions = possibleOptions;
        this.rulesInDuty = rulesInDuty;
    }

    public static CarConfigurationProcess start(List<Rule> rulesForCar, List<Option> options) {
        return new CarConfigurationProcess(new HashSet<>(), new HashSet<>(), new HashSet<>(options), new HashSet<>(rulesForCar));
    }

    public void pick(PickedOption pickedOption) {
        if (blockedOptions.contains(pickedOption.option())) {
            throw new IllegalArgumentException("Cannot pick blocked option");
        }
        pickedOptions.add(pickedOption);
    }

    public Set<Option> getNonPickedOptions() {
        return possibleOptions.stream()
                .filter(option -> pickedOptions.stream()
                        .noneMatch(picked -> picked.option().equals(option)))
                .collect(Collectors.toSet());
    }

    public List<Clause> pickedOptionsClauses() {
        List<Clause> picked = pickedOptions.stream().map(option -> new Clause(option.option().id())).collect(Collectors.toList());
        return picked;
    }
    
    public List<Rule> rules() {
        return this.rulesInDuty.stream().toList();
    }
}
