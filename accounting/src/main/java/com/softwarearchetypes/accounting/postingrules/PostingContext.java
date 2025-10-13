package com.softwarearchetypes.accounting.postingrules;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import com.softwarearchetypes.accounting.AccountingFacade;
import com.softwarearchetypes.accounting.EntryView;

public class PostingContext {

    private final List<EntryView> triggeringEntries;
    private final AccountingFacade accountingFacade;
    private final Instant executionTime;
    private final BusinessContext businessContext;

    public PostingContext(List<EntryView> triggeringEntries, AccountingFacade accountingFacade, Clock clock) {
        this(triggeringEntries, accountingFacade, clock.instant(), BusinessContext.empty());
    }

    public PostingContext(List<EntryView> triggeringEntries, AccountingFacade accountingFacade, Instant executionTime, BusinessContext businessContext) {
        this.triggeringEntries = List.copyOf(triggeringEntries);
        this.accountingFacade = accountingFacade;
        this.executionTime = executionTime;
        this.businessContext = businessContext;
    }

    public List<EntryView> triggeringEntries() {
        return triggeringEntries;
    }

    public AccountingFacade accountingFacade() {
        return accountingFacade;
    }

    public Instant executionTime() {
        return executionTime;
    }

    public BusinessContext businessContext() {
        return businessContext;
    }
}