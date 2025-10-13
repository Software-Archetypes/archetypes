package com.softwarearchetypes.accounting.postingrules;

import java.util.List;
import java.util.Optional;

interface PostingRuleRepository {

    List<PostingRule> findAll();

    Optional<PostingRule> find(PostingRuleId id);

    void save(PostingRule rule);

    void delete(PostingRuleId id);

    List<PostingRule> findEligibleRules(PostingContext context);
}