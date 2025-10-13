package com.softwarearchetypes.accounting.postingrules;

import java.time.Clock;

import com.softwarearchetypes.accounting.AccountingFacade;
import io.pillopl.common.EventPublisher;

public class PostingRulesConfiguration {

    private final PostingRuleRepository postingRuleRepository;
    private final PostingRulesFacade postingRulesFacade;
    private final PostingRulesEventHandler eventHandler;

    PostingRulesConfiguration(PostingRuleRepository postingRuleRepository,
            PostingRulesFacade postingRulesFacade, PostingRulesEventHandler eventHandler) {
        this.postingRuleRepository = postingRuleRepository;
        this.postingRulesFacade = postingRulesFacade;
        this.eventHandler = eventHandler;
    }

    public static PostingRulesConfiguration inMemory(AccountingFacade accountingFacade, EventPublisher eventPublisher, Clock clock) {
        PostingRuleRepository postingRuleRepository = new InMemoryPostingRuleRepository();
        PostingRuleExecutor postingRuleExecutor = new PostingRuleExecutor(postingRuleRepository);
        PostingRulesFacade postingRulesFacade = new PostingRulesFacade(postingRuleRepository, postingRuleExecutor, accountingFacade, clock);
        PostingRulesEventHandler eventHandler = new PostingRulesEventHandler(postingRulesFacade);

        eventPublisher.register(eventHandler);

        return new PostingRulesConfiguration(postingRuleRepository, postingRulesFacade, eventHandler);
    }

    public PostingRulesFacade facade() {
        return postingRulesFacade;
    }

    public PostingRuleRepository repository() {
        return postingRuleRepository;
    }

    public PostingRulesEventHandler eventHandler() {
        return eventHandler;
    }
}