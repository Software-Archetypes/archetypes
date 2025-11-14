package com.softwarearchetypes.accounting;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.pillopl.common.Money;
import io.pillopl.common.Result;

import static io.pillopl.accounting.AccountType.ASSET;
import static io.pillopl.accounting.EntryFilter.*;
import static java.time.Clock.fixed;
import static org.assertj.core.api.Assertions.assertThat;

class AccountsProjectionScenarios {

    static final Instant NOW = LocalDateTime.of(2022, 2, 2, 12, 50).atZone(ZoneId.systemDefault()).toInstant();

    AccountingFacade facade = AccountingConfiguration.inMemory(fixed(NOW, ZoneId.systemDefault())).facade();

    @Test
    void can_create_projecting_account() {
        //given
        AccountId cash = AccountId.generate();
        AccountId projected = AccountId.generate();
        AccountId projecting = AccountId.generate();
        //and
        assertThat(facade.createAccount(CreateAccount.generateAssetAccount(cash)).isSuccessful()).isTrue();
        assertThat(facade.createAccount(CreateAccount.generateAssetAccount(projected)).isSuccessful()).isTrue();
        //and
        Result projectingAccount = facade.createProjectingAccount(projecting, Filter.just(ENTRY_OF_ACCOUNT(projected).and(ENTRY_HAVING_METADATA(HAVING_VALUES("initiator", "ewa")))), "ewa-opis");
        assertThat(projectingAccount.isSuccessful()).isTrue();

        //when
        facade.transfer(projected, cash, Money.pln(40), NOW, NOW, new MetaData(Map.of("initiator", "ewa")));
        facade.transfer(projected, cash, Money.pln(10), NOW, NOW);
        facade.transfer(projected, cash, Money.pln(30), NOW, NOW, new MetaData(Map.of("initiator", "jacek milo")));

        //then
        assertThat(facade.balance(projected)).hasValue(Money.pln(-80));
        assertThat(facade.balance(projecting)).hasValue(Money.pln(-40));

        Optional<AccountView> projectedView = facade.findAccount(projected);
        Optional<AccountView> projectingView = facade.findAccount(projecting);

        assertThat(projectedView).isPresent();
        assertThat(projectingView).isPresent();

        assertThat(projectedView.get().entries()).hasSize(3);
        assertThat(projectingView.get().entries()).hasSize(1);
        assertThat(projectingView.get().desc()).isEqualTo("ewa-opis");
        assertThat(projectedView.get().desc()).isEmpty();

    }

    @Test
    void projecting_accounts_do_not_have_category_nor_groupId() {
        //given
        CreateAccount createAccountRequest = CreateAccount.generate(ASSET, GroupId.generate());
        AccountId projected = createAccountRequest.accountId();
        //and
        AccountId projecting = AccountId.generate();
        assertThat(facade.createAccount(createAccountRequest).isSuccessful()).isTrue();

        //when
        Result projectingAccount = facade.createProjectingAccount(projecting, Filter.just(ENTRY_OF_ACCOUNT(projected).and(ENTRY_HAVING_METADATA(HAVING_VALUES("initiator", "ewa")))), "ewa-opis");
        assertThat(projectingAccount.isSuccessful()).isTrue();

        //then
        Optional<AccountView> projectedView = facade.findAccount(projected);
        Optional<AccountView> projectingView = facade.findAccount(projecting);

        assertThat(projectedView).isPresent();
        assertThat(projectingView).isPresent();

        assertThat(projectedView.get().category()).isEqualTo(ASSET);
        assertThat(projectingView.get().category()).isNull();

        assertThat(projectedView.get().groupId()).isEqualTo(createAccountRequest.groupId());
        assertThat(projectingView.get().groupId()).isNull();

    }


}