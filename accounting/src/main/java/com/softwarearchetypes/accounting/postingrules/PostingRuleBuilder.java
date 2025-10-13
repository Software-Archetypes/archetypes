package com.softwarearchetypes.accounting.postingrules;

import java.util.Map;

import com.softwarearchetypes.accounting.AccountId;
import com.softwarearchetypes.accounting.EntryView;

import static io.pillopl.common.Preconditions.checkArgument;

public class PostingRuleBuilder {

    private final PostingRuleId id = PostingRuleId.generate();
    private final String name;
    private EligibilityCondition eligibilityCondition;
    private AccountFinder accountFinder;
    private PostingCalculator postingCalculator;
    private int priority = 100;

    private PostingRuleBuilder(String name) {
        this.name = name;
    }

    public static PostingRuleBuilder createRule(String name) {
        return new PostingRuleBuilder(name);
    }

    public PostingRuleBuilder whenTriggerAccountIs(AccountId accountId) {
        return when(EligibilityCondition.accountEquals(accountId));
    }

    public PostingRuleBuilder whenTriggerEntryTypeIs(EntryView.EntryType entryType) {
        return when(EligibilityCondition.entryTypeEquals(entryType));
    }

    public PostingRuleBuilder when(EligibilityCondition condition) {
        this.eligibilityCondition = condition;
        return this;
    }

    public PostingRuleBuilder transferTo(AccountFinder finder) {
        this.accountFinder = finder;
        return this;
    }

    public PostingRuleBuilder transferTo(Map<String, AccountId> accounts) {
        return transferTo(AccountFinder.fixed(accounts));
    }

    public PostingRuleBuilder transferTo(String tag, AccountId accountId) {
        return transferTo(AccountFinder.fixed(tag, accountId));
    }

    public PostingRuleBuilder calculateUsing(PostingCalculator calculator) {
        this.postingCalculator = calculator;
        return this;
    }

    public PostingRuleBuilder priority(int priority) {
        this.priority = priority;
        return this;
    }

    public ConfigurablePostingRule build() {
        checkArgument(name != null && !name.isBlank(), "PostingRule name must be defined");
        checkArgument(eligibilityCondition != null, "EligibilityCondition must be defined");
        checkArgument(accountFinder != null, "AccountFinder must be defined");
        checkArgument(postingCalculator != null, "PostingCalculator must be defined");

        return new ConfigurablePostingRule(id, name, eligibilityCondition, accountFinder, postingCalculator, priority);
    }
}