package com.softwarearchetypes.accounting.postingrules;

import java.util.List;

import com.softwarearchetypes.accounting.AccountId;
import com.softwarearchetypes.accounting.EntryId;
import com.softwarearchetypes.accounting.EntryView;
import com.softwarearchetypes.accounting.TransactionId;
import com.softwarearchetypes.accounting.events.AccountingEvent;
import com.softwarearchetypes.accounting.events.CreditEntryRegistered;
import com.softwarearchetypes.accounting.events.DebitEntryRegistered;
import com.softwarearchetypes.common.events.EventHandler;
import com.softwarearchetypes.common.events.PublishedEvent;

class PostingRulesEventHandler implements EventHandler {

    private final PostingRulesFacade postingRulesFacade;

    public PostingRulesEventHandler(PostingRulesFacade postingRulesFacade) {
        this.postingRulesFacade = postingRulesFacade;
    }

    @Override
    public boolean supports(PublishedEvent event) {
        return event instanceof AccountingEvent;
    }

    @Override
    public void handle(PublishedEvent event) {
        if (event instanceof AccountingEvent accountingEvent) {
            handleAccountingEvent(accountingEvent);
        }
    }

    private void handleAccountingEvent(AccountingEvent event) {
        switch (event) {
            case CreditEntryRegistered creditEvent -> handleEntryEvent(creditEvent);
            case DebitEntryRegistered debitEvent -> handleEntryEvent(debitEvent);
        }
    }

    private void handleEntryEvent(CreditEntryRegistered event) {
        EntryView entryView = new EntryView(
                new EntryId(event.entryId()),
                EntryView.EntryType.CREDIT,
                event.amount(),
                TransactionId.of(event.transactionId()),
                AccountId.of(event.accountId()),
                event.occurredAt(),
                event.appliesAt()
        );

        postingRulesFacade.executeRulesFor(List.of(entryView));
    }

    private void handleEntryEvent(DebitEntryRegistered event) {
        EntryView entryView = new EntryView(
                new EntryId(event.entryId()),
                EntryView.EntryType.DEBIT,
                event.amount(),
                TransactionId.of(event.transactionId()),
                AccountId.of(event.accountId()),
                event.occurredAt(),
                event.appliesAt()
        );

        postingRulesFacade.executeRulesFor(List.of(entryView));
    }
}