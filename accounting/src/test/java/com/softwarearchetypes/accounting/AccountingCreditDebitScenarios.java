package com.softwarearchetypes.accounting;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import io.pillopl.common.Money;
import io.pillopl.common.Result;

import static io.pillopl.accounting.EntryView.EntryType.CREDIT;
import static io.pillopl.accounting.EntryView.EntryType.DEBIT;
import static io.pillopl.accounting.TransactionType.TRANSFER;
import static io.pillopl.common.Money.pln;
import static io.pillopl.common.Money.zeroPln;
import static java.time.Clock.fixed;
import static org.assertj.core.api.Assertions.assertThat;

class AccountingCreditDebitScenarios {

    static final Instant TUESDAY_10_00 = LocalDateTime.of(2022, 2, 2, 10, 0).atZone(ZoneId.systemDefault()).toInstant();
    static final Instant TUESDAY_11_00 = LocalDateTime.of(2022, 2, 2, 11, 0).atZone(ZoneId.systemDefault()).toInstant();
    static final Instant TUESDAY_12_00 = LocalDateTime.of(2022, 2, 2, 12, 0).atZone(ZoneId.systemDefault()).toInstant();
    static final Instant NOW = LocalDateTime.of(2022, 2, 2, 12, 50).atZone(ZoneId.systemDefault()).toInstant();

    AccountingFacade facade = AccountingConfiguration.inMemory(fixed(NOW, ZoneId.systemDefault())).facade();

    @Test
    void should_return_zero_balance_for_empty_account() {
        //given
        AccountId accountId = AccountId.generate();
        //and
        assertTrue(facade.createAccount(CreateAccount.generateAssetAccount(accountId)).isSuccessful());

        //when
        Optional<Money> balance = facade.balance(accountId);

        //then
        assertThat(balance).hasValue(zeroPln());
    }

    @Test
    void should_have_no_transactions_registered_for_empty_account() {
        //given
        AccountId accountId = AccountId.generate();
        //and
        assertTrue(facade.createAccount(CreateAccount.generateAssetAccount(accountId)).isSuccessful());

        //when
        List<TransactionId> transactions = facade.findTransactionIdsFor(accountId);

        //then
        assertThat(transactions).isEmpty();
    }

    @Test
    void should_return_single_entry_balance() {
        //given
        AccountId creditedAccount = generateAssetAccount();
        //and
        AccountId debitedAccount = generateAssetAccount();

        //and
        Result result = facade.transfer(debitedAccount, creditedAccount, pln(100), TUESDAY_10_00, TUESDAY_10_00);
        assertTrue(result.isSuccessful());

        //when
        Optional<Money> creditAccountBalance = facade.balance(creditedAccount);
        Optional<Money> debitAccountBalance = facade.balance(debitedAccount);

        //then
        assertThat(creditAccountBalance).hasValue(pln(100));
        assertThat(debitAccountBalance).hasValue(pln(-100));

    }

    @Test
    void should_find_the_same_transaction_executed_on_accounts() {
        //given
        AccountId creditedAccount = generateAssetAccount();
        //and
        AccountId debitedAccount = generateAssetAccount();

        //and
        Result result = facade.transfer(debitedAccount, creditedAccount, pln(100), TUESDAY_10_00, TUESDAY_10_00);
        assertTrue(result.isSuccessful());

        //when
        List<TransactionId> creditedAccountTransactions = facade.findTransactionIdsFor(creditedAccount);
        List<TransactionId> debitedAccountTransactions = facade.findTransactionIdsFor(debitedAccount);

        //then
        assertThat(creditedAccountTransactions).hasSize(1);
        assertThat(debitedAccountTransactions).hasSize(1);
        assertThat(creditedAccountTransactions.getFirst()).isEqualTo(debitedAccountTransactions.getFirst());
    }

    @Test
    void should_find_transaction_executed_on_accounts() {
        // given
        AccountId creditedAccount = generateAssetAccount();
        AccountId debitedAccount1 = generateAssetAccount();
        AccountId debitedAccount2 = generateAssetAccount();

        Transaction transaction = facade.transaction()
                                        .withTypeOf(TRANSFER)
                                        .occurredAt(TUESDAY_10_00)
                                        .appliesAt(TUESDAY_10_00)
                                        .executing()
                                        .creditTo(creditedAccount, pln(100))
                                        .debitFrom(debitedAccount1, pln(60))
                                        .debitFrom(debitedAccount2, pln(40))
                                        .build();

        Result result = facade.execute(transaction);
        assertTrue(result.isSuccessful());

        // when
        TransactionView view = facade.findTransactionBy(transaction.id()).orElseThrow();

        // then
        TransactionViewAssert.assertThat(view)
                             .hasId(transaction.id())
                             .hasType(TRANSFER)
                             .occurredAt(TUESDAY_10_00)
                             .appliesAt(TUESDAY_10_00)
                             .containsEntries()
                             .containExactlyOneEntry(creditedAccount, CREDIT, pln(100))
                             .containExactlyOneEntry(debitedAccount1, DEBIT, pln(-60))
                             .containExactlyOneEntry(debitedAccount2, DEBIT, pln(-40))
                             .containExactly(3)
                             .allOccurredAt(TUESDAY_10_00)
                             .allHaveTransactionId(transaction.id());
    }


    @Test
    void should_support_going_back_in_time() {
        //given
        AccountId account = generateAssetAccount();
        //and
        AccountId paymentAccount = generateAssetAccount();

        //when
        Result resultTransaction = facade.transfer(paymentAccount, account, pln(100), TUESDAY_10_00, TUESDAY_10_00);
        Result resultTransaction2 = facade.transfer(account, paymentAccount, pln(30), TUESDAY_11_00, TUESDAY_11_00);
        Result resultTransaction3 = facade.transfer(account, paymentAccount, pln(30), TUESDAY_12_00, TUESDAY_12_00);

        //then
        assertThat(resultTransaction.isSuccessful()).isTrue();
        assertThat(resultTransaction2.isSuccessful()).isTrue();
        assertThat(resultTransaction3.isSuccessful()).isTrue();

        //and
        assertThat(facade.balanceAsOf(account, TUESDAY_10_00)).hasValue(pln(100));
        assertThat(facade.balanceAsOf(account, TUESDAY_11_00)).hasValue(pln(70));
        assertThat(facade.balance(account)).hasValue(pln(40));
    }

    @Test
    void should_return_balances_for_multiple_accounts_as_of_given_time() {
        //given
        AccountId acc1 = generateAssetAccount();
        AccountId acc2 = generateAssetAccount();

        //and
        AccountId paymentAccount = generateAssetAccount();

        //when
        Result resultTransaction1 = facade.transfer(paymentAccount, acc1, pln(100), TUESDAY_10_00, TUESDAY_10_00);
        Result resultTransaction2 = facade.transfer(paymentAccount, acc2, pln(200), TUESDAY_10_00, TUESDAY_10_00);
        Result resultTransaction3 = facade.transfer(acc2, paymentAccount, pln(50), TUESDAY_11_00, TUESDAY_11_00);

        //then
        assertThat(resultTransaction1.isSuccessful()).isTrue();
        assertThat(resultTransaction2.isSuccessful()).isTrue();
        assertThat(resultTransaction3.isSuccessful()).isTrue();

        //when
        Balances balances = facade.balancesAsOf(Set.of(acc1, acc2), TUESDAY_11_00);

        //then
        assertThat(balances.balances())
                .containsOnlyKeys(acc1, acc2)
                .containsEntry(acc1, pln(100))
                .containsEntry(acc2, pln(150));
    }

    AccountId generateAssetAccount() {
        CreateAccount accountCreation = CreateAccount.generate(String.ASSET);
        assertTrue(facade.createAccount(accountCreation).isSuccessful());
        return accountCreation.accountId();
    }

}