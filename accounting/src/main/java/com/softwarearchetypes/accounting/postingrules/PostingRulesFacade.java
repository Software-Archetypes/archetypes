package com.softwarearchetypes.accounting.postingrules;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import com.softwarearchetypes.accounting.AccountingFacade;
import com.softwarearchetypes.accounting.EntryView;
import com.softwarearchetypes.common.Result;

/**
 * Facade for PostingRules module.
 *
 * Note: This facade intentionally exposes domain objects (PostingRule, PostingRuleBuilder)
 * and interfaces (EligibilityCondition, PostingCalculator) to demonstrate the rich domain model capabilities.
 * In a production solution, we recommend transitioning to command-based API (CreatePostingRule, UpdatePostingRule, etc.)
 * to better encapsulate the domain model and provide clearer boundaries.
 */
public class PostingRulesFacade {

    private final PostingRuleRepository postingRuleRepository;
    private final PostingRuleExecutor postingRuleExecutor;
    private final AccountingFacade accountingFacade;
    private final Clock clock;

    public PostingRulesFacade(PostingRuleRepository postingRuleRepository, PostingRuleExecutor postingRuleExecutor, AccountingFacade accountingFacade, Clock clock) {
        this.postingRuleRepository = postingRuleRepository;
        this.postingRuleExecutor = postingRuleExecutor;
        this.accountingFacade = accountingFacade;
        this.clock = clock;
    }

    public Result<String, PostingRuleId> saveRule(PostingRule rule) {
        try {
            postingRuleRepository.save(rule);
            return Result.success(rule.id());
        } catch (Exception ex) {
            return Result.failure(ex.getMessage());
        }
    }

    public Result<String, PostingRuleId> deleteRule(PostingRuleId id) {
        try {
            postingRuleRepository.delete(id);
            return Result.success(id);
        } catch (Exception ex) {
            return Result.failure(ex.getMessage());
        }
    }

    public Optional<PostingRule> findRule(PostingRuleId id) {
        return postingRuleRepository.find(id);
    }

    public List<PostingRule> findAllRules() {
        return postingRuleRepository.findAll();
    }

    public Result executeRulesFor(List<EntryView> triggeringEntries) {
        PostingContext context = new PostingContext(triggeringEntries, accountingFacade, clock);
        return postingRuleExecutor.executeEligibleRules(context, accountingFacade::execute);
    }
}