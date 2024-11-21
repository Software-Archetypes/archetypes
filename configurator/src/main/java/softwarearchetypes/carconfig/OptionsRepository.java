package softwarearchetypes.carconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class OptionsRepository {

    private final Map<CarConfigId, List<Option>> options = new HashMap<>();
    private final Map<CarConfigId, List<Rule>> rules = new HashMap<>();

    List<Option> load(CarConfigId id) {
        return options.get(id);
    }

    void addOption(CarConfigId id, Option option) {
        options.computeIfAbsent(id, k -> new ArrayList<>()).add(option);
    }

    void addOptions(CarConfigId id, List<Option> options) {
        this.options.computeIfAbsent(id, k -> new ArrayList<>()).addAll(options);
    }

    void deleteOption(CarConfigId carConfigId, Option option) {
        List<Option> config = options.get(carConfigId);
        config.remove(option);
    }

    List<Rule> loadRules(CarConfigId id) {
        return rules.getOrDefault(id, new ArrayList<>());
    }

    void addRule(CarConfigId id, Rule rule) {
        rules.computeIfAbsent(id, k -> new ArrayList<>()).add(rule);
    }

    void addRules(CarConfigId id, List<Rule> rules) {
        this.rules.computeIfAbsent(id, k -> new ArrayList<>()).addAll(rules);
    }

    void delete(CarConfigId carConfigId) {
        rules.remove(carConfigId);
    }
}
