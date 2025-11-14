package com.softwarearchetypes.accounting;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

import org.junit.jupiter.api.Test;

import io.pillopl.common.Result;

import static io.pillopl.accounting.AccountType.ASSET;
import static io.pillopl.accounting.CreateAccount.generateAssetAccount;
import static io.pillopl.common.Money.zeroPln;
import static java.time.Clock.fixed;
import static org.assertj.core.api.Assertions.assertThat;

class AccountsCreatingScenarios {

    static final Instant NOW = LocalDateTime.of(2022, 2, 2, 12, 50).atZone(ZoneId.systemDefault()).toInstant();

    AccountingFacade facade = AccountingConfiguration.inMemory(fixed(NOW, ZoneId.systemDefault())).facade();

    @Test
    void can_create_without_group() {
        //given
        AccountId accountId = AccountId.generate();

        //when
        Result result = facade.createAccount(generateAssetAccount(accountId));

        //then
        assertThat(result.isSuccessful()).isTrue();
        assertThat(facade.balance(accountId)).hasValue(zeroPln());
    }

    @Test
    void cant_create_existing_account() {
        //given
        AccountId accountId = AccountId.generate();

        //and
        Result result = facade.createAccount(generateAssetAccount(accountId));

        //and
        assertThat(result.isSuccessful()).isTrue();

        //when
        Result result2ndtry = facade.createAccount(generateAssetAccount(accountId));

        assertThat(result2ndtry.isSuccessful()).isFalse();
    }

    @Test
    void can_create_account_with_a_group() {
        //given
        AccountId accountId = AccountId.generate();
        AccountId accountId2 = AccountId.generate();
        AccountId accountId3 = AccountId.generate();
        GroupId group = GroupId.generate();
        GroupId group2 = GroupId.generate();

        //when
        Result result1 = facade.createAccount(accountId, group, ASSET);
        Result result2 = facade.createAccount(accountId2, group, ASSET);
        Result result3 = facade.createAccount(accountId3, group2, ASSET);

        //then
        assertThat(result1.isSuccessful()).isTrue();
        assertThat(result2.isSuccessful()).isTrue();
        assertThat(result3.isSuccessful()).isTrue();

        //and
        assertThat(facade.balances(group).balances()).containsOnlyKeys(accountId, accountId2);
        assertThat(facade.balances(group2).balances()).containsOnlyKeys(accountId3);
    }

    @Test
    void can_create_accounts_with_a_group() {
        //given
        AccountId accountId = AccountId.generate();
        AccountId accountId2 = AccountId.generate();
        GroupId group = GroupId.generate();

        //when
        Result result1 = facade.createAccounts(Set.of(generateAssetAccount(accountId, group), generateAssetAccount(accountId2, group)));

        //then
        assertThat(result1.isSuccessful()).isTrue();

        //and
        assertThat(facade.balances(group).balances()).containsOnlyKeys(accountId, accountId2);
    }

    @Test
    void cant_create_accounts_with_a_group_if_at_least_one_already_exists() {
        //given
        AccountId accountId = AccountId.generate();
        GroupId group = GroupId.generate();

        //and
        Result result = facade.createAccount(accountId, group, ASSET);
        //and
        assertThat(result.isSuccessful()).isTrue();

        //and
        AccountId accountId2 = AccountId.generate();

        //when
        Result resultGrouped = facade.createAccounts(Set.of(generateAssetAccount(accountId, group), generateAssetAccount(accountId2, group)));

        //then
        assertThat(resultGrouped.failed()).isTrue();

        //and
        assertThat(facade.balances(group).balances()).containsOnlyKeys(accountId);
    }


}