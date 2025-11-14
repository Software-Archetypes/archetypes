package com.softwarearchetypes.accounting;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import io.pillopl.common.Money;

import static io.pillopl.accounting.AccountType.ASSET;
import static java.time.Clock.fixed;
import static org.assertj.core.api.Assertions.assertThat;

class AccountsFindScenarios {

    static final Instant NOW = LocalDateTime.of(2022, 2, 2, 12, 50).atZone(ZoneId.systemDefault()).toInstant();

    AccountingFacade facade = AccountingConfiguration.inMemory(fixed(NOW, ZoneId.systemDefault())).facade();

    @Test
    void should_find_existing_account() {
        //given
        AccountId accountId = AccountId.generate();
        GroupId groupId = GroupId.generate();

        //and
        facade.createAccount(CreateAccount.generateAssetAccount(accountId));

        //when
        Optional<AccountView> result = facade.findAccount(accountId);

        //then
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(accountId);
        assertThat(result.get().category()).isEqualTo(ASSET);
        assertThat(result.get().entries()).isEmpty();
        assertThat(result.get().groupId()).isEqualTo(groupId);
    }

    @Test
    void should_return_empty_for_non_existing_account() {
        //given
        AccountId nonExistingAccountId = AccountId.generate();

        //when
        Optional<AccountView> result = facade.findAccount(nonExistingAccountId);

        //then
        assertThat(result).isEmpty();
    }

    @Test
    void should_find_account_with_transactions() {
        //given
        AccountId accountId = AccountId.generate();
        GroupId groupId = GroupId.generate();

        //and
        AccountId paymentAccountId = AccountId.generate();
        facade.createAccount(CreateAccount.generateAssetAccount(paymentAccountId));

        //and
        facade.createAccount(CreateAccount.generateAssetAccount(accountId));

        //and - transfer some money from payment account
        facade.transfer(paymentAccountId, accountId, Money.pln(100), NOW, NOW);

        //when
        Optional<AccountView> result = facade.findAccount(accountId);

        //then
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(accountId);
        assertThat(result.get().category()).isEqualTo(ASSET);
        assertThat(result.get().entries()).hasSize(1);
        assertThat(result.get().entries().get(0).amount()).isEqualTo(Money.pln(100));
    }

    @Test
    void should_find_account_with_description() {
        //given
        AccountId accountId = AccountId.generate();
        GroupId groupId = GroupId.generate();
        String description = "Test Account Description";

        //and
        facade.createAccount(accountId, groupId, ASSET, description);

        //when
        Optional<AccountView> result = facade.findAccount(accountId);

        //then
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(accountId);
        assertThat(result.get().desc()).isEqualTo(description);
        assertThat(result.get().category()).isEqualTo(ASSET);
    }

    @Test
    void should_find_all_accounts() {
        //given
        AccountId accountId1 = AccountId.generate();
        AccountId accountId2 = AccountId.generate();
        String description = "Test Account Description";

        //and
        facade.createAccount(accountId1, GroupId.generate(), ASSET, description);
        facade.createAccount(accountId2, GroupId.generate(), ASSET, description);

        //when
        List<AccountView> result = facade.findAccounts(Set.of(accountId1, accountId2));

        //then
        assertThat(result).hasSize(2);
        assertThat(result.stream().map(AccountView::id).toList()).containsExactlyInAnyOrder(accountId1, accountId2);
        assertThat(result.stream().map(AccountView::category).toList()).containsExactlyInAnyOrder(ASSET, ASSET);
        assertThat(result.stream().map(AccountView::desc).toList()).containsExactlyInAnyOrder("Test Account Description", "Test Account Description");
        assertThat(facade.findAccounts(Set.of(AccountId.generate(), AccountId.generate()))).hasSize(0);
    }

    @Test
    void should_find_some_accounts() {
        //given
        AccountId accountId1 = AccountId.generate();
        AccountId accountId2 = AccountId.generate();
        String description = "Test Account Description";

        //and
        facade.createAccount(accountId1, GroupId.generate(), ASSET, description);
        facade.createAccount(accountId2, GroupId.generate(), ASSET, description);

        //when
        List<AccountView> result = facade.findAccounts(Set.of(accountId1, accountId2, AccountId.generate()));

        //then
        assertThat(result).hasSize(2);
        assertThat(result.stream().map(AccountView::id).toList()).containsExactlyInAnyOrder(accountId1, accountId2);
        assertThat(result.stream().map(AccountView::category).toList()).containsExactlyInAnyOrder(ASSET, ASSET);
        assertThat(result.stream().map(AccountView::desc).toList()).containsExactlyInAnyOrder("Test Account Description", "Test Account Description");
        assertThat(facade.findAccounts(Set.of(AccountId.generate(), AccountId.generate()))).hasSize(0);
    }

    @Test
    void should_return_empty_list_when_accounts_not_present() {
        //given
        AccountId accountId1 = AccountId.generate();
        AccountId accountId2 = AccountId.generate();
        String description = "Test Account Description";

        //and
        facade.createAccount(accountId1, GroupId.generate(), ASSET, description);
        facade.createAccount(accountId2, GroupId.generate(), ASSET, description);

        //when
        List<AccountView> result = facade.findAccounts(Set.of(AccountId.generate(), AccountId.generate()));

        //then
        assertThat(result).isEmpty();
    }
}