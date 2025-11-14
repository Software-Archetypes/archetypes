package com.softwarearchetypes.accounting.postingrules;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.softwarearchetypes.accounting.AccountId;
import com.softwarearchetypes.accounting.AccountView;
import com.softwarearchetypes.accounting.AccountingConfiguration;
import com.softwarearchetypes.accounting.AccountingFacade;
import com.softwarearchetypes.accounting.Transaction;
import com.softwarearchetypes.quantity.money.Money;

import static com.softwarearchetypes.accounting.EntryView.EntryType.CREDIT;
import static com.softwarearchetypes.accounting.postingrules.EligibilityCondition.accountEquals;
import static com.softwarearchetypes.accounting.postingrules.EligibilityCondition.entryTypeEquals;
import static java.time.Clock.fixed;
import static org.assertj.core.api.Assertions.assertThat;

class PostingRulesScenarios {

    static final Instant TUESDAY_10_00 = LocalDateTime.of(2022, 2, 2, 10, 0).atZone(ZoneId.systemDefault()).toInstant();
    static final Instant NOW = LocalDateTime.of(2022, 2, 2, 12, 50).atZone(ZoneId.systemDefault()).toInstant();

    private static final Clock clock = fixed(NOW, ZoneId.systemDefault());

    EventPublishingConfiguration eventConfig = EventPublishingConfiguration.inMemory();
    AccountingConfiguration accountingConfig = AccountingConfiguration.inMemory(clock, eventConfig.publisher());
    PostingRulesConfiguration postingRulesConfig = PostingRulesConfiguration.inMemory(accountingConfig.facade(), eventConfig.publisher(), clock);

    AccountingFacade accountingFacade = accountingConfig.facade();
    PostingRulesFacade postingRulesFacade = postingRulesConfig.facade();

    @Test
    void should_execute_posting_rule_when_entry_is_created() {
        //given
        AccountId incomingPaymentsAccount = generateAssetAccount();
        AccountId receivablesAccount = generateAssetAccount();
        AccountId commissionAccount = generateOffBalanceAccount();

        //and commission rule: 2% of credit entries to receivables account goes to commission tracking
        PostingRule commissionRule = PostingRuleBuilder.createRule("Commission tracking")
                                                       .when(accountEquals(receivablesAccount).and(entryTypeEquals(CREDIT)))
                                                       .calculateUsing(new CommissionCalculator(Percentage.of(2)))
                                                       .transferTo("commission", commissionAccount)
                                                       .build();

        //and
        assertTrue(postingRulesFacade.saveRule(commissionRule).isSuccessful());

        //when client payment of 1000 PLN is credited to receivables
        assertTrue(accountingFacade.transfer(incomingPaymentsAccount, receivablesAccount, pln(1000), TUESDAY_10_00, TUESDAY_10_00).isSuccessful());

        //then commission entry should be created automatically
        assertThat(accountingFacade.balance(commissionAccount)).hasValue(pln(20));
    }

    @Test
    void should_execute_posting_rules_in_priority_order_when_order_matters() {
        //given
        AccountId receivablesAccount = generateAssetAccount();
        AccountId taxAccount = generateAssetAccount();
        AccountId bonusAccount = generateOffBalanceAccount();

        //and tax rule with high priority (10) - takes 50 PLN immediately
        PostingRule taxRule = PostingRuleBuilder
                .createRule("Tax deduction")
                .when(accountEquals(receivablesAccount).and(entryTypeEquals(CREDIT)))
                .calculateUsing(new FixedAmountCalculator(pln(50)))
                .transferTo("target", taxAccount)
                .priority(10)
                .build();

        //and bonus rule with lower priority (20) - only executes if account balance >= 1000 PLN
        PostingRule bonusRule = PostingRuleBuilder
                .createRule("Bonus tracking")
                .when(accountEquals(receivablesAccount).and(entryTypeEquals(CREDIT)).and(new MinimumBalanceCondition(pln(1000))))
                .calculateUsing(new PercentageCalculator(Percentage.of(1)))
                .transferTo("target", bonusAccount)
                .priority(20)
                .build();

        //and
        assertTrue(postingRulesFacade.saveRule(taxRule).isSuccessful());
        assertTrue(postingRulesFacade.saveRule(bonusRule).isSuccessful());

        //when 1000 PLN is credited to receivables
        assertTrue(accountingFacade.transfer(generateAssetAccount(), receivablesAccount, pln(1000), TUESDAY_10_00, TUESDAY_10_00).isSuccessful());

        //then tax rule executes first (taking 50 PLN), reducing balance to 950 PLN
        assertThat(accountingFacade.balance(taxAccount)).hasValue(pln(50));
        assertThat(accountingFacade.balance(receivablesAccount)).hasValue(pln(950));

        //and bonus rule doesn't execute because balance < 1000 PLN after tax deduction
        assertThat(accountingFacade.balance(bonusAccount)).hasValue(Money.zeroPln());
    }

    @Test
    void should_save_and_find_posting_rule() {
        //given
        PostingRule rule = PostingRuleBuilder
                .createRule("Test rule")
                .when(accountEquals(generateAssetAccount()))
                .calculateUsing(new PercentageCalculator(Percentage.of(1)))
                .transferTo("target", generateOffBalanceAccount())
                .build();

        //when
        assertTrue(postingRulesFacade.saveRule(rule).isSuccessful());

        //then
        assertThat(postingRulesFacade.findRule(rule.id())).hasValue(rule);
        assertThat(postingRulesFacade.findAllRules()).contains(rule);
    }

    @Test
    void should_delete_posting_rule() {
        //given
        PostingRule rule = PostingRuleBuilder
                .createRule("Test rule")
                .when(accountEquals(generateAssetAccount()))
                .calculateUsing(new PercentageCalculator(Percentage.of(1)))
                .transferTo("target", generateOffBalanceAccount())
                .build();

        //and
        assertTrue(postingRulesFacade.saveRule(rule).isSuccessful());
        assertThat(postingRulesFacade.findRule(rule.id())).hasValue(rule);

        //when
        assertTrue(postingRulesFacade.deleteRule(rule.id()).isSuccessful());

        //then
        assertThat(postingRulesFacade.findRule(rule.id())).isEmpty();
        assertThat(postingRulesFacade.findAllRules()).doesNotContain(rule);
    }

    @Test
    void should_update_existing_posting_rule() {
        //given
        AccountId account = generateAssetAccount();
        AccountId targetAccount = generateOffBalanceAccount();

        PostingRule originalRule = PostingRuleBuilder
                .createRule("Original rule")
                .when(accountEquals(account))
                .calculateUsing(new PercentageCalculator(Percentage.of(1)))
                .transferTo("target", targetAccount)
                .build();

        //and
        assertTrue(postingRulesFacade.saveRule(originalRule).isSuccessful());

        //when - save rule with same ID but different properties
        ConfigurablePostingRule updatedRule = new ConfigurablePostingRule(
                originalRule.id(),
                "Updated rule",
                accountEquals(account),
                AccountFinder.fixed("target", targetAccount),
                new PercentageCalculator(Percentage.of(2)),
                50
        );

        assertTrue(postingRulesFacade.saveRule(updatedRule).isSuccessful());

        //then
        assertThat(postingRulesFacade.findRule(originalRule.id()))
                .hasValue(updatedRule)
                .get()
                .extracting(PostingRule::name, PostingRule::priority)
                .containsExactly("Updated rule", 50);
    }

    @Test
    void should_not_execute_posting_rule_when_eligibility_condition_is_not_met() {
        //given
        AccountId receivablesAccount = generateAssetAccount();
        AccountId otherAccount = generateAssetAccount();
        AccountId commissionAccount = generateOffBalanceAccount();

        //and commission rule only for receivables account
        PostingRule commissionRule = PostingRuleBuilder
                .createRule("Commission tracking")
                .when(accountEquals(receivablesAccount).and(entryTypeEquals(CREDIT)))
                .calculateUsing(new PercentageCalculator(Percentage.of(2)))
                .transferTo("target", commissionAccount)
                .build();

        //and
        assertTrue(postingRulesFacade.saveRule(commissionRule).isSuccessful());

        //when payment to different account
        assertTrue(accountingFacade.transfer(generateAssetAccount(), otherAccount, pln(1000), TUESDAY_10_00, TUESDAY_10_00).isSuccessful());

        //then no commission should be created
        assertThat(accountingFacade.balance(commissionAccount)).hasValue(Money.zeroPln());
    }

    private AccountId generateAssetAccount() {
        AccountId accountId = AccountId.generate();
        assertTrue(accountingFacade.createAccount(CreateAccount.generateAssetAccount(accountId)).isSuccessful());
        return accountId;
    }

    private AccountId generateOffBalanceAccount() {
        AccountId accountId = AccountId.generate();
        assertTrue(accountingFacade.createAccount(accountId, io.pillopl.accounting.GroupId.generate(), OFF_BALANCE, "Test off-balance account").isSuccessful());
        return accountId;
    }

    // Sample PostingCalculator implementations for tests
    static class CommissionCalculator implements PostingCalculator {

        private final Percentage rate;

        CommissionCalculator(Percentage rate) {
            this.rate = rate;
        }

        @Override
        public List<Transaction> calculate(TargetAccounts accounts, PostingContext context) {
            AccountView commissionAccount = accounts.getRequired("commission");

            Money totalAmount = context.triggeringEntries().stream()
                                       .map(EntryView::amount)
                                       .reduce(Money.zeroPln(), Money::add);

            Money commissionAmount = totalAmount.multiply(rate);

            return List.of(
                    context.accountingFacade().transaction()
                           .occurredAt(context.executionTime())
                           .appliesAt(context.executionTime())
                           .withTypeOf("commission")
                           .executing()
                           .creditTo(commissionAccount.id(), commissionAmount)
                           .build()
            );
        }
    }

    static class PercentageCalculator implements PostingCalculator {

        private final Percentage rate;

        PercentageCalculator(Percentage rate) {
            this.rate = rate;
        }

        @Override
        public List<Transaction> calculate(TargetAccounts accounts, PostingContext context) {
            AccountView targetAccount = accounts.getRequired("target");

            Money totalAmount = context.triggeringEntries().stream()
                                       .map(EntryView::amount)
                                       .reduce(Money.zeroPln(), Money::add);

            Money calculatedAmount = totalAmount.multiply(rate);

            return List.of(
                    context.accountingFacade().transaction()
                           .occurredAt(context.executionTime())
                           .appliesAt(context.executionTime())
                           .withTypeOf("percentage_allocation")
                           .executing()
                           .creditTo(targetAccount.id(), calculatedAmount)
                           .build()
            );
        }
    }

    static class FixedAmountCalculator implements PostingCalculator {

        private final Money amount;

        FixedAmountCalculator(Money amount) {
            this.amount = amount;
        }

        @Override
        public List<Transaction> calculate(TargetAccounts accounts, PostingContext context) {
            AccountView targetAccount = accounts.getRequired("target");

            return List.of(
                    context.accountingFacade().transaction()
                           .occurredAt(context.executionTime())
                           .appliesAt(context.executionTime())
                           .withTypeOf("fixed_amount_deduction")
                           .executing()
                           .debitFrom(context.triggeringEntries().getFirst().accountId(), amount)
                           .creditTo(targetAccount.id(), amount)
                           .build()
            );
        }
    }

    static class MinimumBalanceCondition implements EligibilityCondition {

        private final Money minimumBalance;

        MinimumBalanceCondition(Money minimumBalance) {
            this.minimumBalance = minimumBalance;
        }

        @Override
        public boolean test(PostingContext context) {
            AccountId accountId = context.triggeringEntries().getFirst().accountId();
            return context.accountingFacade().balance(accountId)
                          .map(balance -> balance.isGreaterThanOrEqualTo(minimumBalance))
                          .orElse(false);
        }
    }
}