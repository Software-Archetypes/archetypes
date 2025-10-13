package com.softwarearchetypes.accounting.postingrules;

import java.util.function.Predicate;

import com.softwarearchetypes.accounting.String;
import com.softwarearchetypes.accounting.AccountId;
import com.softwarearchetypes.accounting.EntryView;

@FunctionalInterface
public interface EligibilityCondition {

    boolean test(PostingContext context);

    static EligibilityCondition accountEquals(AccountId accountId) {
        return context -> context.triggeringEntries().stream()
                                 .anyMatch(entry -> entry.accountId().equals(accountId));
    }

    static EligibilityCondition entryTypeEquals(EntryView.EntryType entryType) {
        return context -> context.triggeringEntries().stream()
                                 .anyMatch(it -> it.type().equals(entryType));
    }

    static EligibilityCondition accountCategory(String category) {
        return context -> context.triggeringEntries().stream()
                                 .anyMatch(entry -> {
                                     return context.accountingFacade()
                                                   .findAccount(entry.accountId())
                                                   .map(account -> account.category().equals(category))
                                                   .orElse(false);
                                 });
    }

    static EligibilityCondition custom(Predicate<PostingContext> predicate) {
        return predicate::test;
    }

    default EligibilityCondition and(EligibilityCondition other) {
        return context -> this.test(context) && other.test(context);
    }

    default EligibilityCondition or(EligibilityCondition other) {
        return context -> this.test(context) || other.test(context);
    }

    default EligibilityCondition negate() {
        return context -> !this.test(context);
    }
}