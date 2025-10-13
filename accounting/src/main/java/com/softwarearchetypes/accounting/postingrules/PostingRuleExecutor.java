package com.softwarearchetypes.accounting.postingrules;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import com.softwarearchetypes.accounting.Transaction;
import com.softwarearchetypes.accounting.TransactionId;
import com.softwarearchetypes.common.Result;

class PostingRuleExecutor {

    private final PostingRuleRepository postingRuleRepository;

    public PostingRuleExecutor(PostingRuleRepository postingRuleRepository) {
        this.postingRuleRepository = postingRuleRepository;
    }

    public Result<String, Set<TransactionId>> executeEligibleRules(PostingContext context, Function<Transaction, Result<String, TransactionId>> transactionExecutor) {
        List<PostingRule> rules = postingRuleRepository.findEligibleRules(context).stream()
                                                       .sorted(Comparator.comparing(PostingRule::priority))
                                                       .toList();
        return execute(rules, context, transactionExecutor);
    }

    @NotNull
    private static Result<String, Set<TransactionId>> execute(List<PostingRule> rules,
            PostingContext context,
            Function<Transaction, Result<String, TransactionId>> transactionExecutor) {
        Result.CompositeSetResult<String, TransactionId> compositeResult = Result.compositeSet();
        for (PostingRule rule : rules) {
            if (rule.isEligible(context)) {
                for (Transaction tx : rule.execute(context)) {
                    compositeResult.accumulate(transactionExecutor.apply(tx));
                    if (compositeResult.failure()) {
                        return compositeResult.toResult();
                    }
                }
            }
        }
        return compositeResult.toResult();
    }

    public List<Transaction> executeRules(List<PostingRule> rules, PostingContext context) {
        return rules.stream()
                    .filter(rule -> isEligible(rule, context))
                    .sorted(Comparator.comparing(PostingRule::priority))
                    .flatMap(rule -> rule.execute(context).stream())
                    .toList();
    }

    public List<Transaction> executeRule(PostingRule rule, PostingContext context) {
        if (isEligible(rule, context)) {
            return rule.execute(context);
        }
        return List.of();
    }

    private boolean isEligible(PostingRule rule, PostingContext context) {
        return rule.isEligible(context);
    }
}