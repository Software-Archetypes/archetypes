package com.softwarearchetypes.accounting.postingrules;

import java.util.List;

import com.softwarearchetypes.accounting.Transaction;

import static io.pillopl.common.Preconditions.checkArgument;

class ConfigurablePostingRule implements PostingRule {

    private final PostingRuleId id;
    private final String name;
    private final EligibilityCondition eligibilityCondition;
    private final AccountFinder accountFinder;
    private final PostingCalculator postingCalculator;
    private final int priority;

    public ConfigurablePostingRule(PostingRuleId id, String name, EligibilityCondition eligibilityCondition,
                                   AccountFinder accountFinder, PostingCalculator postingCalculator, int priority) {
        checkArgument(id != null, "PostingRule ID must be defined");
        checkArgument(name != null && !name.isBlank(), "PostingRule name must be defined");
        checkArgument(eligibilityCondition != null, "EligibilityCondition must be defined");
        checkArgument(accountFinder != null, "AccountFinder must be defined");
        checkArgument(postingCalculator != null, "PostingCalculator must be defined");

        this.id = id;
        this.name = name;
        this.eligibilityCondition = eligibilityCondition;
        this.accountFinder = accountFinder;
        this.postingCalculator = postingCalculator;
        this.priority = priority;
    }

    public ConfigurablePostingRule(PostingRuleId id, String name, EligibilityCondition eligibilityCondition,
                                   AccountFinder accountFinder, PostingCalculator postingCalculator) {
        this(id, name, eligibilityCondition, accountFinder, postingCalculator, 100);
    }

    @Override
    public PostingRuleId id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean isEligible(PostingContext context) {
        return eligibilityCondition.test(context);
    }

    @Override
    public List<Transaction> execute(PostingContext context) {
        if (isEligible(context)) {
            TargetAccounts accounts = accountFinder.findAccounts(context);
            return postingCalculator.calculate(accounts, context);
        } else {
            return List.of();
        }
    }

    @Override
    public int priority() {
        return priority;
    }
}