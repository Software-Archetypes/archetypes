package com.softwarearchetypes.accounting.postingrules;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class InMemoryPostingRuleRepository implements PostingRuleRepository {

    private final Map<PostingRuleId, PostingRule> rules = new HashMap<>();

    @Override
    public List<PostingRule> findAll() {
        return List.copyOf(rules.values());
    }

    @Override
    public Optional<PostingRule> find(PostingRuleId id) {
        return Optional.ofNullable(rules.get(id));
    }

    @Override
    public void save(PostingRule rule) {
        rules.put(rule.id(), rule);
    }

    @Override
    public void delete(PostingRuleId id) {
        rules.remove(id);
    }

    @Override
    public List<PostingRule> findEligibleRules(PostingContext context) {
        return rules.values()
                .stream()
                .filter(rule -> rule.isEligible(context))
                .toList();
    }
}