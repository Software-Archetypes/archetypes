package com.softwarearchetypes.accounting;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.quantity.money.Money;

import static com.softwarearchetypes.accounting.AccountType.ASSET;
import static com.softwarearchetypes.accounting.AccountType.OFF_BALANCE;
import static com.softwarearchetypes.accounting.TransactionAssert.assertThat;
import static com.softwarearchetypes.accounting.TransactionEntriesConstraint.MIN_2_ACCOUNTS_INVOLVED_CONSTRAINT;
import static com.softwarearchetypes.accounting.TransactionEntriesConstraint.MIN_2_ENTRIES_CONSTRAINT;
import static com.softwarearchetypes.accounting.TransactionType.REVERSAL;
import static com.softwarearchetypes.accounting.TransactionType.TRANSFER;
import static java.time.Clock.fixed;
import static org.apache.commons.lang3.RandomStringUtils.insecure;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionScenarios {

    static final Instant TUESDAY_10_00 = LocalDateTime.of(2022, 2, 2, 10, 0).atZone(ZoneId.systemDefault()).toInstant();
    static final Instant TUESDAY_11_00 = LocalDateTime.of(2022, 2, 2, 11, 0).atZone(ZoneId.systemDefault()).toInstant();
    static final Instant TUESDAY_12_00 = LocalDateTime.of(2022, 2, 2, 12, 0).atZone(ZoneId.systemDefault()).toInstant();
    static final Instant NOW = LocalDateTime.of(2022, 2, 2, 12, 50).atZone(ZoneId.systemDefault()).toInstant();

    AccountingConfiguration configuration = AccountingConfiguration.inMemory(fixed(NOW, ZoneId.systemDefault()));
    AccountingFacade facade = configuration.facade();
    TransactionBuilderFactory transactionBuilderFactory = configuration.transactionBuilderFactory();
    AccountRepository accountRepository = configuration.repository();

    @Test
    void can_create_transaction_with_two_entries() {
        //given
        Account jan = generateAssetAccount();
        Account maria = generateAssetAccount();

        //and
        String transactionType = "opening_balance";

        //when
        Transaction transaction = transactionBuilderFactory.transaction()
                                                           .occurredAt(TUESDAY_11_00)
                                                           .appliesAt(TUESDAY_12_00)
                                                           .withTypeOf(transactionType)
                                                           .executing()
                                                           .creditTo(jan.id(), Money.pln(50))
                                                           .debitFrom(maria.id(), Money.pln(50))
                                                           .build();

        //then
        assertThat(transaction)
                .occurredAt(TUESDAY_11_00)
                .appliesAt(TUESDAY_12_00)
                .hasTypeEqualTo(TransactionType.of(transactionType))
                .hasExactlyOneCreditEntryFor(jan, Money.pln(50))
                .hasExactlyOneDebitEntryFor(maria, Money.pln(50));
    }

    @Test
    void can_execute_transaction() {
        //given
        Account jan = generateAssetAccount();
        Account maria = generateAssetAccount();
        Account rokita = generateOffBalanceAccount();

        //and
        Transaction transaction = transactionBuilderFactory.transaction()
                                                           .occurredAt(TUESDAY_11_00)
                                                           .appliesAt(TUESDAY_12_00)
                                                           .withTypeOf("opening_balance")
                                                           .executing()
                                                           .creditTo(jan.id(), Money.pln(50))
                                                           .debitFrom(maria.id(), Money.pln(50))
                                                           .debitFrom(rokita.id(), Money.pln(20))
                                                           .build();

        //when
        transaction.execute();

        //then
        assertThat(transaction)
                .occurredAt(TUESDAY_11_00)
                .appliesAt(TUESDAY_12_00)
                .hasExactlyOneCreditEntryFor(jan, Money.pln(50))
                .hasExactlyOneDebitEntryFor(maria, Money.pln(50))
                .hasExactlyOneDebitEntryFor(rokita, Money.pln(20));
    }

    @Test
    void can_execute_reverse_transaction() {
        //given
        Account jan = generateAssetAccount();
        Account maria = generateAssetAccount();
        Account rokita = generateOffBalanceAccount();

        //and
        Transaction toBeReversedTx = transactionBuilderFactory.transaction()
                                                              .occurredAt(TUESDAY_10_00)
                                                              .appliesAt(TUESDAY_11_00)
                                                              .withTypeOf("opening_balance")
                                                              .executing()
                                                              .creditTo(jan.id(), Money.pln(50))
                                                              .debitFrom(maria.id(), Money.pln(50))
                                                              .debitFrom(rokita.id(), Money.pln(20))
                                                              .build();

        //and
        toBeReversedTx.execute();

        //when
        Transaction revertingTx = transactionBuilderFactory.transaction()
                                                           .occurredAt(TUESDAY_12_00)
                                                           .appliesAt(TUESDAY_12_00)
                                                           .reverting(toBeReversedTx)
                                                           .build();
        revertingTx.execute();

        //then
        assertThat(revertingTx)
                .hasTypeEqualTo(REVERSAL)
                .hasExactlyOneCreditEntryFor(maria, Money.pln(50))
                .hasExactlyOneDebitEntryFor(jan, Money.pln(50))
                .hasExactlyOneCreditEntryFor(rokita, Money.pln(20));
    }

    @Test
    void can_execute_reverse_transaction_by_id() {
        //given
        Account jan = generateAssetAccount();
        Account maria = generateAssetAccount();
        Account rokita = generateOffBalanceAccount();

        //and
        Transaction toBeReversedTx = transactionBuilderFactory.transaction()
                                                              .occurredAt(TUESDAY_10_00)
                                                              .appliesAt(TUESDAY_11_00)
                                                              .withTypeOf("opening_balance")
                                                              .executing()
                                                              .creditTo(jan.id(), Money.pln(50))
                                                              .debitFrom(maria.id(), Money.pln(50))
                                                              .debitFrom(rokita.id(), Money.pln(20))
                                                              .build();

        //and
        Result revertedTxResult = facade.execute(toBeReversedTx);
        assertTrue(revertedTxResult.success());

        //and
        Transaction revertingTx = transactionBuilderFactory.transaction()
                                                           .occurredAt(TUESDAY_12_00)
                                                           .appliesAt(TUESDAY_12_00)
                                                           .reverting(toBeReversedTx.id())
                                                           .build();
        //when
        Result result = facade.execute(revertingTx);

        //then
        assertTrue(result.success());
        assertThat(revertingTx)
                .hasTypeEqualTo(REVERSAL)
                .hasExactlyOneCreditEntryFor(maria, Money.pln(50))
                .hasExactlyOneDebitEntryFor(jan, Money.pln(50))
                .hasExactlyOneCreditEntryFor(rokita, Money.pln(20));
    }

    @Test
    void cannot_execute_reverse_transaction_by_id_when_ref_transaction_does_not_exist() {
        //given
        Account jan = generateAssetAccount();
        Account maria = generateAssetAccount();
        Account rokita = generateOffBalanceAccount();

        //and
        Transaction transaction = transactionBuilderFactory.transaction()
                                                           .occurredAt(TUESDAY_10_00)
                                                           .appliesAt(TUESDAY_11_00)
                                                           .withTypeOf("opening_balance")
                                                           .executing()
                                                           .creditTo(jan.id(), Money.pln(50))
                                                           .debitFrom(maria.id(), Money.pln(50))
                                                           .debitFrom(rokita.id(), Money.pln(20))
                                                           .build();

        //and
        transaction.execute();

        //expect
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transactionBuilderFactory.transaction()
                                               .occurredAt(TUESDAY_12_00)
                                               .appliesAt(TUESDAY_12_00)
                                               .reverting(transaction.id())
                                               .build()
                                               .execute());
        assertEquals(String.format("Transaction %s does not exist", transaction.id().toString()), ex.getMessage());


    }

    @Test
    void can_create_transaction_with_validity_periods() {
        //given
        Account jan = generateAssetAccount();
        Account maria = generateAssetAccount();
        Validity validUntilEndOfDay = Validity.until(NOW.plusSeconds(86400));

        //when
        Transaction transaction = transactionBuilderFactory.transaction()
                                                           .occurredAt(TUESDAY_11_00)
                                                           .appliesAt(TUESDAY_12_00)
                                                           .withTypeOf("credit_pool")
                                                           .executing()
                                                           .creditTo(jan.id(), Money.pln(100), validUntilEndOfDay)
                                                           .debitFrom(maria.id(), Money.pln(100))
                                                           .build();

        //then
        assertThat(transaction)
                .occurredAt(TUESDAY_11_00)
                .appliesAt(TUESDAY_12_00)
                .hasExactlyOneCreditEntryFor(jan, Money.pln(100), validUntilEndOfDay)
                .hasExactlyOneDebitEntryFor(maria, Money.pln(100));
    }

    @Test
    void can_create_transaction_with_applied_to_references() {
        //given
        Account jan = generateAssetAccount();
        Account maria = generateAssetAccount();

        //and create original transaction with entry to reference
        Transaction originalTransaction = transactionBuilderFactory.transaction()
                                                                   .occurredAt(TUESDAY_10_00)
                                                                   .appliesAt(TUESDAY_11_00)
                                                                   .withTypeOf("original")
                                                                   .executing()
                                                                   .creditTo(jan.id(), Money.pln(100))
                                                                   .debitFrom(maria.id(), Money.pln(100))
                                                                   .build();

        assertTrue(facade.execute(originalTransaction).success());

        //and get reference to original entry
        EntryId originalEntryId = originalTransaction.entries().values().stream()
                                                     .flatMap(Collection::stream)
                                                     .filter(AccountCredited.class::isInstance)
                                                     .findFirst().map(Entry::id).orElseThrow();

        //when
        Transaction correctionTransaction = transactionBuilderFactory.transaction()
                                                                     .occurredAt(TUESDAY_11_00)
                                                                     .appliesAt(TUESDAY_12_00)
                                                                     .withTypeOf("correction")
                                                                     .executing()
                                                                     .creditTo(jan.id(), Money.pln(25), originalEntryId)
                                                                     .debitFrom(maria.id(), Money.pln(25))
                                                                     .build();

        //then
        assertThat(correctionTransaction)
                .occurredAt(TUESDAY_11_00)
                .appliesAt(TUESDAY_12_00)
                .hasExactlyOneCreditEntryFor(jan, Money.pln(25), originalEntryId)
                .hasExactlyOneDebitEntryFor(maria, Money.pln(25));
    }

    @Test
    void cannot_create_transaction_with_non_existing_applied_to_entry() {
        //given
        Account jan = generateAssetAccount();
        Account maria = generateAssetAccount();
        EntryId nonExistingEntryId = EntryId.generate();

        //expect
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transactionBuilderFactory.transaction()
                                               .occurredAt(TUESDAY_11_00)
                                               .appliesAt(TUESDAY_12_00)
                                               .withTypeOf("correction")
                                               .executing()
                                               .creditTo(jan.id(), Money.pln(25), nonExistingEntryId)
                                               .debitFrom(maria.id(), Money.pln(25))
                                               .build());

        assertEquals("No matching entry found for allocation", ex.getMessage());
    }

    @Test
    void cannot_create_transaction_with_applied_to_entry_not_valid_at_transaction_time() {
        //given
        Account jan = generateAssetAccount();
        Account maria = generateAssetAccount();

        //and create original transaction with limited validity
        Validity validOnlyUntilTuesday11 = Validity.until(TUESDAY_11_00);
        Transaction originalTransaction = transactionBuilderFactory.transaction()
                                                                   .occurredAt(TUESDAY_10_00)
                                                                   .appliesAt(TUESDAY_10_00)
                                                                   .withTypeOf(TRANSFER)
                                                                   .executing()
                                                                   .creditTo(jan.id(), Money.pln(100), validOnlyUntilTuesday11)
                                                                   .debitFrom(maria.id(), Money.pln(100))
                                                                   .build();
        assertTrue(facade.execute(originalTransaction).success());

        //and get reference to original entry
        EntryId originalEntryId = originalTransaction.entries().values().stream()
                                                     .flatMap(Collection::stream)
                                                     .filter(AccountCredited.class::isInstance)
                                                     .findFirst().map(Entry::id).orElseThrow();

        //expect
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transactionBuilderFactory.transaction()
                                               .occurredAt(TUESDAY_12_00)
                                               .appliesAt(TUESDAY_12_00) // after validity period
                                               .withTypeOf("correction")
                                               .executing()
                                               .creditTo(jan.id(), Money.pln(25), originalEntryId)
                                               .debitFrom(maria.id(), Money.pln(25))
                                               .build());

        assertEquals(String.format("Referenced entry %s is not valid at %s", originalEntryId, TUESDAY_12_00.toString()), ex.getMessage());
    }

    @Test
    void can_create_transaction_with_applied_to_entry_valid_at_transaction_time() {
        //given
        Account jan = generateAssetAccount();
        Account maria = generateAssetAccount();

        //and create original transaction with validity until NOW
        Validity validUntilNow = Validity.until(NOW);
        Transaction originalTransaction = transactionBuilderFactory.transaction()
                                                                   .occurredAt(TUESDAY_10_00)
                                                                   .appliesAt(TUESDAY_10_00)
                                                                   .withTypeOf("original")
                                                                   .executing()
                                                                   .creditTo(jan.id(), Money.pln(100), validUntilNow)
                                                                   .debitFrom(maria.id(), Money.pln(100))
                                                                   .build();
        facade.execute(originalTransaction);

        //and get reference to original entry
        EntryId originalEntryId = originalTransaction.entries().values().stream()
                                                     .flatMap(Collection::stream)
                                                     .filter(AccountCredited.class::isInstance)
                                                     .findFirst().map(Entry::id).orElseThrow();

        //when - create correction transaction within validity period
        Transaction correctionTransaction = transactionBuilderFactory.transaction()
                                                                     .occurredAt(TUESDAY_12_00)
                                                                     .appliesAt(TUESDAY_12_00) // within validity period (before NOW)
                                                                     .withTypeOf("correction")
                                                                     .executing()
                                                                     .creditTo(jan.id(), Money.pln(25), originalEntryId)
                                                                     .debitFrom(maria.id(), Money.pln(25))
                                                                     .build();

        //then
        assertThat(correctionTransaction)
                .occurredAt(TUESDAY_12_00)
                .appliesAt(TUESDAY_12_00)
                .hasExactlyOneCreditEntryFor(jan, Money.pln(25), originalEntryId)
                .hasExactlyOneDebitEntryFor(maria, Money.pln(25));
    }

    @Test
    void revert_transaction_creates_applied_to_linkage() {
        //given
        Account jan = generateAssetAccount();
        Account maria = generateAssetAccount();

        //and
        Transaction originalTransaction = transactionBuilderFactory.transaction()
                                                                   .occurredAt(TUESDAY_10_00)
                                                                   .appliesAt(TUESDAY_11_00)
                                                                   .withTypeOf("opening_balance")
                                                                   .executing()
                                                                   .creditTo(jan.id(), Money.pln(50))
                                                                   .debitFrom(maria.id(), Money.pln(50))
                                                                   .build();

        //and
        facade.execute(originalTransaction);

        //when
        Transaction reverseTransaction = transactionBuilderFactory.transaction()
                                                                  .occurredAt(TUESDAY_12_00)
                                                                  .appliesAt(TUESDAY_12_00)
                                                                  .reverting(originalTransaction)
                                                                  .build();

        //then
        assertThat(reverseTransaction)
                .hasTypeEqualTo(REVERSAL)
                .hasExactlyOneCreditEntryFor(maria, Money.pln(50))
                .hasExactlyOneDebitEntryFor(jan, Money.pln(50));

        //and check applied to linkage
        EntryId originalCreditId = originalTransaction.entries().values().stream()
                                                      .flatMap(Collection::stream)
                                                      .filter(AccountCredited.class::isInstance)
                                                      .findFirst().map(Entry::id).orElseThrow();

        EntryId originalDebitId = originalTransaction.entries().values().stream()
                                                     .flatMap(Collection::stream)
                                                     .filter(AccountDebited.class::isInstance)
                                                     .findFirst().map(Entry::id).orElseThrow();

        assertThat(reverseTransaction)
                .hasExactlyOneDebitEntryFor(jan, Money.pln(50), originalCreditId)
                .hasExactlyOneCreditEntryFor(maria, Money.pln(50), originalDebitId);
    }

    @Test
    void transaction_entries_inherit_details_from_parent_transaction() {
        //given
        Account jan = generateAssetAccount();
        Account maria = generateAssetAccount();

        //and
        String transactionType = "opening_balance";

        //when
        Transaction transaction = transactionBuilderFactory.transaction()
                                                           .occurredAt(TUESDAY_11_00)
                                                           .appliesAt(TUESDAY_12_00)
                                                           .withTypeOf(transactionType)
                                                           .executing()
                                                           .creditTo(jan.id(), Money.pln(50))
                                                           .debitFrom(maria.id(), Money.pln(50))
                                                           .build();

        //then
        assertThat(transaction).containsEntries()
                               .allOccurredAt(TUESDAY_11_00)
                               .allAppliesAt(TUESDAY_12_00)
                               .allReferencedTo(transaction.id());
    }

    @Test
    void can_create_multi_legged_transaction_with_more_than_two_entries() {
        //given
        Account jan = generateAssetAccount();
        Account maria = generateAssetAccount();
        Account rokita = generateAssetAccount();

        //when
        Transaction transaction = transactionBuilderFactory.transaction()
                                                           .occurredAt(TUESDAY_11_00)
                                                           .appliesAt(TUESDAY_12_00)
                                                           .withTypeOf("opening_balance")
                                                           .executing()
                                                           .creditTo(jan.id(), Money.pln(50))
                                                           .debitFrom(maria.id(), Money.pln(30))
                                                           .debitFrom(rokita.id(), Money.pln(20))
                                                           .build();

        //then
        assertThat(transaction)
                .occurredAt(TUESDAY_11_00)
                .appliesAt(TUESDAY_12_00)
                .hasExactlyOneCreditEntryFor(jan, Money.pln(50))
                .hasExactlyOneDebitEntryFor(maria, Money.pln(30))
                .hasExactlyOneDebitEntryFor(rokita, Money.pln(20));
    }

    @Test
    void can_create_transaction_with_no_balancing_constraint_for_off_balance_accounts() {
        //given
        Account cash = generateAssetAccount();
        Account mainPrincipal = generateAssetAccount();

        //and
        Account mariaPrincipal = generateOffBalanceAccount();
        Account janPrincipal = generateOffBalanceAccount();

        //when
        Transaction transaction = transactionBuilderFactory.transaction()
                                                           .occurredAt(TUESDAY_11_00)
                                                           .appliesAt(TUESDAY_12_00)
                                                           .withTypeOf("opening_balance")
                                                           .executing()
                                                           //maria repays 100 on principal
                                                           .debitFrom(cash.id(), Money.pln(100))
                                                           .creditTo(mainPrincipal.id(), Money.pln(100))
                                                           //maria and jan get their off balance principal accounts entries according to their sharesPerComponent
                                                           .creditTo(mariaPrincipal.id(), Money.pln(100))
                                                           .creditTo(janPrincipal.id(), Money.pln(20))
                                                           .build();

        //then
        assertThat(transaction)
                .occurredAt(TUESDAY_11_00)
                .appliesAt(TUESDAY_12_00)
                .hasExactlyOneDebitEntryFor(cash, Money.pln(100))
                .hasExactlyOneCreditEntryFor(mainPrincipal, Money.pln(100))
                .hasExactlyOneCreditEntryFor(mariaPrincipal, Money.pln(100))
                .hasExactlyOneCreditEntryFor(janPrincipal, Money.pln(20));
    }

    @Test
    void cannot_create_transaction_with_no_balancing_constraint_fulfilled() {
        //given
        Account jan = generateAssetAccount();
        Account maria = generateAssetAccount();

        //expect
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transactionBuilderFactory.transaction()
                                               .occurredAt(TUESDAY_11_00)
                                               .appliesAt(TUESDAY_12_00)
                                               .withTypeOf("opening_balance")
                                               .executing()
                                               .debitFrom(jan.id(), Money.pln(100))
                                               .creditTo(maria.id(), Money.pln(80))
                                               .build());
        assertEquals("Entry balance within transaction must always be 0", ex.getMessage());
    }

    @Test
    void cannot_create_transaction_with_balancing_constraint_fulfilled_but_for_off_balance_accounts() {
        //given
        Account jan = generateAssetAccount();
        Account maria = generateOffBalanceAccount();

        //expect
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transactionBuilderFactory.transaction()
                                               .occurredAt(TUESDAY_11_00)
                                               .appliesAt(TUESDAY_12_00)
                                               .withTypeOf("opening_balance")
                                               .executing()
                                               .debitFrom(jan.id(), Money.pln(100))
                                               .creditTo(maria.id(), Money.pln(100))
                                               .build());
        assertEquals("Entry balance within transaction must always be 0", ex.getMessage());
    }

    @Test
    void cannot_create_transaction_with_single_entry_when_the_constraint_is_enabled() {
        //given
        Account jan = generateAssetAccount();

        //expect
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transactionBuilderFactory.transaction()
                                               .occurredAt(TUESDAY_11_00)
                                               .appliesAt(TUESDAY_12_00)
                                               .withTypeOf("opening_balance")
                                               .withTransactionEntriesConstraint(MIN_2_ENTRIES_CONSTRAINT)
                                               .executing()
                                               .creditTo(jan.id(), Money.pln(50))
                                               .build());
        assertEquals("Transaction must have at least 2 entries", ex.getMessage());
    }

    @Test
    void cannot_create_transaction_with_non_balanced_entries() {
        //given
        Account jan = generateAssetAccount();
        Account maria = generateAssetAccount();

        //expect
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transactionBuilderFactory.transaction()
                                               .occurredAt(TUESDAY_11_00)
                                               .appliesAt(TUESDAY_12_00)
                                               .withTypeOf("opening_balance")
                                               .executing()
                                               .creditTo(jan.id(), Money.pln(50))
                                               .debitFrom(maria.id(), Money.pln(60))
                                               .build());
        assertEquals("Entry balance within transaction must always be 0", ex.getMessage());


    }

    @Test
    void cannot_create_transaction_with_2_entries_addressing_single_account_when_the_constraint_is_enabled() {
        //given
        Account jan = generateAssetAccount();

        //expect
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transactionBuilderFactory.transaction()
                                               .occurredAt(TUESDAY_11_00)
                                               .appliesAt(TUESDAY_12_00)
                                               .withTypeOf("opening_balance")
                                               .withTransactionEntriesConstraint(MIN_2_ACCOUNTS_INVOLVED_CONSTRAINT)
                                               .executing()
                                               .creditTo(jan.id(), Money.pln(50))
                                               .debitFrom(jan.id(), Money.pln(50))
                                               .build());
        assertEquals("Transaction must involve at least 2 accounts", ex.getMessage());

    }

    @Test
    void cannot_create_transaction_without_occurence_time() {
        //given
        Account jan = generateAssetAccount();
        Account maria = generateAssetAccount();

        //expect
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transactionBuilderFactory.transaction()
                                               .appliesAt(TUESDAY_12_00)
                                               .withTypeOf("opening_balance")
                                               .executing()
                                               .creditTo(jan.id(), Money.pln(50))
                                               .debitFrom(maria.id(), Money.pln(50))
                                               .build());
        assertEquals("Transaction must have its occurrence time", ex.getMessage());
    }

    @Test
    void cannot_create_transaction_without_application_time() {
        //given
        Account jan = generateAssetAccount();
        Account maria = generateAssetAccount();

        //expect
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transactionBuilderFactory.transaction()
                                               .occurredAt(TUESDAY_11_00)
                                               .withTypeOf("opening_balance")
                                               .executing()
                                               .creditTo(jan.id(), Money.pln(50))
                                               .debitFrom(maria.id(), Money.pln(50))
                                               .build());
        assertEquals("Transaction must have its application time", ex.getMessage());
    }

    @Test
    void cannot_create_transaction_without_type() {
        //given
        Account jan = generateAssetAccount();
        Account maria = generateAssetAccount();

        //expect
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transactionBuilderFactory.transaction()
                                               .occurredAt(TUESDAY_11_00)
                                               .appliesAt(TUESDAY_12_00)
                                               .executing()
                                               .creditTo(jan.id(), Money.pln(50))
                                               .debitFrom(maria.id(), Money.pln(50))
                                               .build());
        assertEquals("Transaction must have its type", ex.getMessage());
    }

    @Test
    void can_revert_transaction() {
        //given
        Account cash = generateAssetAccount();
        Account mainPrincipal = generateAssetAccount();

        //and
        Account mariaPrincipal = generateOffBalanceAccount();
        Account janPrincipal = generateOffBalanceAccount();

        //and
        Transaction toBeReverted = transactionBuilderFactory.transaction()
                                                            .occurredAt(TUESDAY_10_00)
                                                            .appliesAt(TUESDAY_10_00)
                                                            .withTypeOf("opening_balance")
                                                            .executing()
                                                            .debitFrom(cash.id(), Money.pln(100))
                                                            .creditTo(mainPrincipal.id(), Money.pln(100))
                                                            .creditTo(mariaPrincipal.id(), Money.pln(100))
                                                            .creditTo(janPrincipal.id(), Money.pln(20))
                                                            .build();

        //when
        Transaction revertingTransaction = transactionBuilderFactory.transaction()
                                                                    .occurredAt(TUESDAY_11_00)
                                                                    .appliesAt(TUESDAY_12_00)
                                                                    .reverting(toBeReverted)
                                                                    .build();

        //then
        assertThat(revertingTransaction)
                .occurredAt(TUESDAY_11_00)
                .appliesAt(TUESDAY_12_00)
                .hasReferenceTo(toBeReverted.id())
                .hasTypeEqualTo(REVERSAL)
                .hasExactlyOneCreditEntryFor(cash, Money.pln(100))
                .hasExactlyOneDebitEntryFor(mainPrincipal, Money.pln(100))
                .hasExactlyOneDebitEntryFor(mariaPrincipal, Money.pln(100))
                .hasExactlyOneDebitEntryFor(janPrincipal, Money.pln(20));
    }

    @Test
    void can_create_transaction_provided_transaction_id() {
        //given
        Account jan = generateAssetAccount();
        Account maria = generateAssetAccount();

        //and
        TransactionId clientProvidedId = TransactionId.generate();
        String transactionType = "client_payment";

        //when
        Transaction transaction = transactionBuilderFactory.transaction()
                                                           .id(clientProvidedId)
                                                           .occurredAt(TUESDAY_11_00)
                                                           .appliesAt(TUESDAY_12_00)
                                                           .withTypeOf(transactionType)
                                                           .executing()
                                                           .creditTo(jan.id(), Money.pln(100))
                                                           .debitFrom(maria.id(), Money.pln(100))
                                                           .build();

        //then
        assertThat(transaction)
                .hasIdEqualTo(clientProvidedId);
    }

    @Test
    void can_create_transaction_with_fifo_allocation_filter() {
        //given
        Account jan = generateAssetAccount();
        Account maria = generateAssetAccount();

        //and create transactions with entries in different times
        Transaction tx1 = transactionBuilderFactory.transaction()
                                                   .occurredAt(TUESDAY_10_00)
                                                   .appliesAt(TUESDAY_10_00)
                                                   .withTypeOf("setup")
                                                   .executing()
                                                   .creditTo(jan.id(), Money.pln(100))
                                                   .debitFrom(maria.id(), Money.pln(100))
                                                   .build();
        assertTrue(facade.execute(tx1).success());

        Transaction tx2 = transactionBuilderFactory.transaction()
                                                   .occurredAt(TUESDAY_11_00)
                                                   .appliesAt(TUESDAY_11_00)
                                                   .withTypeOf("setup")
                                                   .executing()
                                                   .creditTo(jan.id(), Money.pln(200))
                                                   .debitFrom(maria.id(), Money.pln(200))
                                                   .build();
        assertTrue(facade.execute(tx2).success());

        //when
        Transaction transaction = transactionBuilderFactory.transaction()
                                                           .occurredAt(TUESDAY_12_00)
                                                           .appliesAt(TUESDAY_12_00)
                                                           .withTypeOf("allocation_test")
                                                           .executing()
                                                           .creditTo(jan.id(), Money.pln(50), EntryAllocationFilterBuilder.fifo(jan.id()).build())
                                                           .debitFrom(maria.id(), Money.pln(50))
                                                           .build();

        //then - should reference the oldest entry (from tx1)
        Entry oldestEntry = tx1.entries().get(jan).get(0);
        assertThat(transaction)
                .occurredAt(TUESDAY_12_00)
                .appliesAt(TUESDAY_12_00)
                .hasExactlyOneCreditEntryFor(jan, Money.pln(50), oldestEntry.id());
    }

    @Test
    void can_create_transaction_with_lifo_allocation_filter() {
        //given
        Account jan = generateAssetAccount();
        Account maria = generateAssetAccount();

        //and create transactions with entries in different times
        Transaction tx1 = transactionBuilderFactory.transaction()
                                                   .occurredAt(TUESDAY_10_00)
                                                   .appliesAt(TUESDAY_10_00)
                                                   .withTypeOf("setup")
                                                   .executing()
                                                   .creditTo(jan.id(), Money.pln(100))
                                                   .debitFrom(maria.id(), Money.pln(100))
                                                   .build();
        assertTrue(facade.execute(tx1).success());

        Transaction tx2 = transactionBuilderFactory.transaction()
                                                   .occurredAt(TUESDAY_11_00)
                                                   .appliesAt(TUESDAY_11_00)
                                                   .withTypeOf("setup")
                                                   .executing()
                                                   .creditTo(jan.id(), Money.pln(200))
                                                   .debitFrom(maria.id(), Money.pln(200))
                                                   .build();
        assertTrue(facade.execute(tx2).success());

        //when
        Transaction transaction = transactionBuilderFactory.transaction()
                                                           .occurredAt(TUESDAY_12_00)
                                                           .appliesAt(TUESDAY_12_00)
                                                           .withTypeOf("allocation_test")
                                                           .executing()
                                                           .debitFrom(jan.id(), Money.pln(50), EntryAllocationFilterBuilder.lifo(jan.id()).build())
                                                           .creditTo(maria.id(), Money.pln(50))
                                                           .build();

        //then - should reference the newest entry (from tx2)
        Entry newestEntry = tx2.entries().get(jan).get(0);
        assertThat(transaction)
                .occurredAt(TUESDAY_12_00)
                .appliesAt(TUESDAY_12_00)
                .hasExactlyOneDebitEntryFor(jan, Money.pln(50), newestEntry.id());
    }

    @Test
    void should_reject_allocation_filter_when_no_matching_entry_found() {
        //given
        Account jan = generateAssetAccount();
        Account maria = generateAssetAccount();
        AccountId nonExistingAccount = AccountId.generate();

        //when/then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                transactionBuilderFactory.transaction()
                                         .occurredAt(TUESDAY_12_00)
                                         .appliesAt(TUESDAY_12_00)
                                         .withTypeOf("allocation_test")
                                         .executing()
                                         .creditTo(jan.id(), Money.pln(50), EntryAllocationFilterBuilder.fifo(nonExistingAccount).build())
                                         .debitFrom(maria.id(), Money.pln(50))
                                         .build());

        assertEquals("No matching entry found for allocation", exception.getMessage());
    }

    @Test
    void can_compensate_fully_expired_entry() {
        //given
        Account creditAccount = generateAssetAccount();
        Account offsetAccount = generateAssetAccount();
        Validity expiredValidity = Validity.until(TUESDAY_11_00);

        //and create transaction with expired entry
        Transaction originalTransaction = transactionBuilderFactory.transaction()
                                                                   .occurredAt(TUESDAY_10_00)
                                                                   .appliesAt(TUESDAY_10_00)
                                                                   .withTypeOf("credit_with_expiry")
                                                                   .executing()
                                                                   .creditTo(creditAccount.id(), Money.pln(100), expiredValidity)
                                                                   .debitFrom(offsetAccount.id(), Money.pln(100))
                                                                   .build();
        assertTrue(facade.execute(originalTransaction).success());

        //and get the expired entry
        EntryId expiredEntryId = originalTransaction.entries().values().stream()
                                                    .flatMap(Collection::stream)
                                                    .filter(entry -> entry.validity().equals(expiredValidity))
                                                    .findFirst().map(Entry::id).orElseThrow();

        //when compensating the expired entry (at NOW which is after TUESDAY_11_00)
        Transaction compensationTransaction = transactionBuilderFactory.transaction()
                                                                       .occurredAt(NOW)
                                                                       .appliesAt(NOW)
                                                                       .compensatingExpired(expiredEntryId)
                                                                       .withCompensationAccount(offsetAccount.id())
                                                                       .build()
                                                                       .orElseThrow(() -> new AssertionError("Compensation transaction should be created"));

        //then
        assertThat(compensationTransaction)
                .hasTypeEqualTo(TransactionType.EXPIRATION_COMPENSATION)
                .hasExactlyOneDebitEntryFor(creditAccount, Money.pln(100), expiredEntryId)
                .hasExactlyOneCreditEntryFor(offsetAccount, Money.pln(100));
    }

    @Test
    void can_compensate_partially_used_expired_credit_entry() {
        //given
        Account creditAccount = generateAssetAccount();
        Account offsetAccount = generateAssetAccount();
        Validity expiredValidity = Validity.until(TUESDAY_11_00);

        //and create transaction with expired credit entry
        Transaction originalTransaction = transactionBuilderFactory.transaction()
                                                                   .occurredAt(TUESDAY_10_00)
                                                                   .appliesAt(TUESDAY_10_00)
                                                                   .withTypeOf("credit_with_expiry")
                                                                   .executing()
                                                                   .creditTo(creditAccount.id(), Money.pln(100), expiredValidity)
                                                                   .debitFrom(offsetAccount.id(), Money.pln(100))
                                                                   .build();
        assertTrue(facade.execute(originalTransaction).success());

        //and get the expired entry
        EntryId expiredEntryId = originalTransaction.entries().values().stream()
                                                    .flatMap(Collection::stream)
                                                    .filter(entry -> entry.validity().equals(expiredValidity))
                                                    .findFirst().map(Entry::id).orElseThrow();

        //and partially use the expired entry
        Transaction usageTransaction = transactionBuilderFactory.transaction()
                                                                .occurredAt(TUESDAY_10_00.plusSeconds(30))
                                                                .appliesAt(TUESDAY_10_00.plusSeconds(30))
                                                                .withTypeOf("partial_usage")
                                                                .executing()
                                                                .debitFrom(creditAccount.id(), Money.pln(30), expiredEntryId)
                                                                .creditTo(offsetAccount.id(), Money.pln(30))
                                                                .build();
        assertTrue(facade.execute(usageTransaction).success());

        //when compensating the expired entry (at NOW which is after TUESDAY_11_00)
        Optional<Transaction> compensationTransaction = transactionBuilderFactory.transaction()
                                                                                 .occurredAt(NOW)
                                                                                 .appliesAt(NOW)
                                                                                 .compensatingExpired(expiredEntryId)
                                                                                 .withCompensationAccount(offsetAccount.id())
                                                                                 .build();

        //then - should compensate remaining 70 PLN
        assertTrue(compensationTransaction.isPresent());
        assertThat(compensationTransaction.get())
                .hasTypeEqualTo(TransactionType.EXPIRATION_COMPENSATION)
                .hasExactlyOneDebitEntryFor(creditAccount, Money.pln(70), expiredEntryId)
                .hasExactlyOneCreditEntryFor(offsetAccount, Money.pln(70));
    }

    @Test
    void can_compensate_partially_used_expired_debit_entry() {
        //given
        Account debitAccount = generateAssetAccount();
        Account compensationAccount = generateAssetAccount();
        Validity expiredValidity = Validity.until(TUESDAY_11_00);

        //and create transaction with expired debit entry
        Transaction originalTransaction = transactionBuilderFactory.transaction()
                                                                   .occurredAt(TUESDAY_10_00)
                                                                   .appliesAt(TUESDAY_10_00)
                                                                   .withTypeOf("debit_with_expiry")
                                                                   .executing()
                                                                   .debitFrom(debitAccount.id(), Money.pln(100), expiredValidity)
                                                                   .creditTo(compensationAccount.id(), Money.pln(100))
                                                                   .build();
        assertTrue(facade.execute(originalTransaction).success());

        //and get the expired debit entry
        EntryId expiredEntryId = originalTransaction.entries().values().stream()
                                                    .flatMap(Collection::stream)
                                                    .filter(entry -> entry.validity().equals(expiredValidity))
                                                    .findFirst().map(Entry::id).orElseThrow();

        //and partially reduce the expired debit entry with a credit
        Transaction usageTransaction = transactionBuilderFactory.transaction()
                                                                .occurredAt(TUESDAY_10_00.plusSeconds(30))
                                                                .appliesAt(TUESDAY_10_00.plusSeconds(30))
                                                                .withTypeOf("partial_credit_usage")
                                                                .executing()
                                                                .creditTo(debitAccount.id(), Money.pln(25), expiredEntryId)
                                                                .debitFrom(compensationAccount.id(), Money.pln(25))
                                                                .build();
        assertTrue(facade.execute(usageTransaction).success());

        //when compensating the expired entry (at NOW which is after TUESDAY_11_00)
        Optional<Transaction> compensationTransaction = transactionBuilderFactory.transaction()
                                                                                 .occurredAt(NOW)
                                                                                 .appliesAt(NOW)
                                                                                 .compensatingExpired(expiredEntryId)
                                                                                 .withCompensationAccount(compensationAccount.id())
                                                                                 .build();

        //then - should compensate remaining 75 PLN debit with credit
        assertTrue(compensationTransaction.isPresent());
        assertThat(compensationTransaction.get())
                .hasTypeEqualTo(TransactionType.EXPIRATION_COMPENSATION)
                .hasExactlyOneCreditEntryFor(debitAccount, Money.pln(75), expiredEntryId)
                .hasExactlyOneDebitEntryFor(compensationAccount, Money.pln(75));
    }

    @Test
    void cannot_compensate_fully_used_expired_entry() {
        //given
        Account creditAccount = generateAssetAccount();
        Account offsetAccount = generateAssetAccount();
        Validity expiredValidity = Validity.until(TUESDAY_11_00);

        //and create transaction with expired entry
        Transaction originalTransaction = transactionBuilderFactory.transaction()
                                                                   .occurredAt(TUESDAY_10_00)
                                                                   .appliesAt(TUESDAY_10_00)
                                                                   .withTypeOf("credit_with_expiry")
                                                                   .executing()
                                                                   .creditTo(creditAccount.id(), Money.pln(100), expiredValidity)
                                                                   .debitFrom(offsetAccount.id(), Money.pln(100))
                                                                   .build();
        assertTrue(facade.execute(originalTransaction).success());

        //and get the expired entry
        EntryId expiredEntryId = originalTransaction.entries().values().stream()
                                                    .flatMap(Collection::stream)
                                                    .filter(entry -> entry.validity().equals(expiredValidity))
                                                    .findFirst().map(Entry::id).orElseThrow();

        //and fully use the expired entry
        Transaction usageTransaction = transactionBuilderFactory.transaction()
                                                                .occurredAt(TUESDAY_10_00.plusSeconds(30))
                                                                .appliesAt(TUESDAY_10_00.plusSeconds(30))
                                                                .withTypeOf("full_usage")
                                                                .executing()
                                                                .debitFrom(creditAccount.id(), Money.pln(100), expiredEntryId)
                                                                .creditTo(offsetAccount.id(), Money.pln(100))
                                                                .build();
        assertTrue(facade.execute(usageTransaction).success());

        //when trying to compensate fully used expired entry
        Optional<Transaction> compensationTransaction = transactionBuilderFactory.transaction()
                                                                                 .occurredAt(NOW)
                                                                                 .appliesAt(NOW)
                                                                                 .compensatingExpired(expiredEntryId)
                                                                                 .withCompensationAccount(offsetAccount.id())
                                                                                 .build();

        //then - should return empty as nothing to compensate
        assertTrue(compensationTransaction.isEmpty());
    }

    @Test
    void cannot_compensate_non_expired_entry() {
        //given
        Account creditAccount = generateAssetAccount();
        Account offsetAccount = generateAssetAccount();
        Validity validValidity = Validity.until(NOW.plusSeconds(3600)); // valid for another hour

        //and create transaction with valid entry
        Transaction originalTransaction = transactionBuilderFactory.transaction()
                                                                   .occurredAt(TUESDAY_10_00)
                                                                   .appliesAt(TUESDAY_10_00)
                                                                   .withTypeOf("credit_with_validity")
                                                                   .executing()
                                                                   .creditTo(creditAccount.id(), Money.pln(100), validValidity)
                                                                   .debitFrom(offsetAccount.id(), Money.pln(100))
                                                                   .build();
        assertTrue(facade.execute(originalTransaction).success());

        //and get the valid entry
        EntryId validEntryId = originalTransaction.entries().values().stream()
                                                  .flatMap(Collection::stream)
                                                  .filter(entry -> entry.validity().equals(validValidity))
                                                  .findFirst().map(Entry::id).orElseThrow();

        //when/then trying to compensate non-expired entry should fail
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                transactionBuilderFactory.transaction()
                                         .occurredAt(NOW)
                                         .appliesAt(NOW)
                                         .compensatingExpired(validEntryId)
                                         .withCompensationAccount(offsetAccount.id())
                                         .build());

        assertTrue(exception.getMessage().contains("has not expired yet"));
    }

    @Test
    void cannot_compensate_without_compensation_account_for_double_entry_booking() {
        //given
        Account creditAccount = generateAssetAccount(); // ASSET category requires double-entry booking
        Account offsetAccount = generateAssetAccount();
        Validity expiredValidity = Validity.until(TUESDAY_11_00);

        //and create transaction with expired entry
        Transaction originalTransaction = transactionBuilderFactory.transaction()
                                                                   .occurredAt(TUESDAY_10_00)
                                                                   .appliesAt(TUESDAY_10_00)
                                                                   .withTypeOf("credit_with_expiry")
                                                                   .executing()
                                                                   .creditTo(creditAccount.id(), Money.pln(100), expiredValidity)
                                                                   .debitFrom(offsetAccount.id(), Money.pln(100))
                                                                   .build();
        assertTrue(facade.execute(originalTransaction).success());

        //and get the expired entry
        EntryId expiredEntryId = originalTransaction.entries().values().stream()
                                                    .flatMap(Collection::stream)
                                                    .filter(entry -> entry.validity().equals(expiredValidity))
                                                    .findFirst().map(Entry::id).orElseThrow();

        //when/then trying to compensate without compensation account should fail
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                transactionBuilderFactory.transaction()
                                         .occurredAt(NOW)
                                         .appliesAt(NOW)
                                         .compensatingExpired(expiredEntryId)
                                         .build());

        assertEquals("Entry balance within transaction must always be 0", exception.getMessage());
    }

    @Test
    void cannot_compensate_non_existing_entry() {
        //given
        EntryId nonExistingEntryId = EntryId.generate();

        //when/then trying to compensate non-existing entry should fail
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                transactionBuilderFactory.transaction()
                                         .occurredAt(NOW)
                                         .appliesAt(NOW)
                                         .compensatingExpired(nonExistingEntryId)
                                         .withCompensationAccount(generateAssetAccount().id())
                                         .build());

        assertEquals("Entry " + nonExistingEntryId + " does not exist", exception.getMessage());
    }

    private Account generateOffBalanceAccount() {
        AccountId accountId = AccountId.generate();
        Account account = new Account(accountId, OFF_BALANCE, AccountName.of(insecure().nextAlphabetic(10)));
        accountRepository.save(account);
        return account;
    }

    private Account generateAssetAccount() {
        AccountId accountId = AccountId.generate();
        Account account = new Account(accountId, ASSET, AccountName.of(insecure().nextAlphabetic(10)));
        accountRepository.save(account);
        return account;
    }

}
