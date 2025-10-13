package com.softwarearchetypes.accounting.postingrules;

import java.util.List;

import com.softwarearchetypes.accounting.Transaction;

public interface PostingRule {

    PostingRuleId id();

    String name();

    boolean isEligible(PostingContext context);

    List<Transaction> execute(PostingContext context);

    default int priority() {
        return 100;
    }
}