package com.softwarearchetypes.accounting;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.softwarearchetypes.quantity.money.Money;

import static com.softwarearchetypes.accounting.EntryFilter.ENTRY_OF_ACCOUNT;
import static com.softwarearchetypes.accounting.EntryFilter.ENTRY_OF_AMOUNT;
import static com.softwarearchetypes.accounting.EntryFilter.ENTRY_OF_DATE;
import static com.softwarearchetypes.accounting.EntryFilter.ENTRY_OF_METADATA;

public class AccountEntryFilter {

    private Predicate<String> accountDescPredicate = accountDesc -> true;
    private Predicate<AccountId> accountIdPredicate = accountId -> true;
    private Predicate<Instant> occuredAtPredicate = date -> true;
    private Predicate<Money> amountPredicate = amount -> true;
    private Predicate<Map<String, String>> metadataPredicate = metaData -> true;

    private AccountEntryFilter() {
    }

    public static AccountEntryFilter filtering() {
        return new AccountEntryFilter();
    }

    public AccountEntryFilter onDateOlderThan(Instant date) {
        this.occuredAtPredicate = occuredAtPredicate.and(test -> !test.isAfter(date));
        return this;
    }

    public AccountEntryFilter onDateYoungerThan(Instant date) {
        this.occuredAtPredicate = occuredAtPredicate.and(test -> !test.isBefore(date));
        return this;
    }

    public AccountEntryFilter onDate(Predicate<Instant> datePredicate) {
        this.occuredAtPredicate = occuredAtPredicate.and(datePredicate);
        return this;
    }

    public AccountEntryFilter onAccountsIn(Set<AccountId> idSet) {
        this.accountIdPredicate = accountIdPredicate.and(idSet::contains);
        return this;
    }

    public AccountEntryFilter havingMetadata(String key, String value) {
        this.metadataPredicate = metadataPredicate.and(metadata -> metadata.containsKey(key) && metadata.get(key).equals(value));
        return this;
    }

    public AccountEntryFilter onAccountEquals(AccountId accountId) {
        return onAccountsIn(Set.of(accountId));
    }

    public AccountEntryFilter onAmount(Predicate<Money> amountPredicate) {
        this.amountPredicate = amountPredicate.and(amountPredicate);
        return this;
    }

    public AccountEntryFilter onAccountDescriptionContaining(String desc) {
        this.accountDescPredicate = accountDescPredicate.and(accountDesc -> accountDesc.contains(desc));
        return this;
    }

    //koniecznie niepubliczna
    Filter toFilter() {
        //to sql
        Predicate<Entry> entryFilter = ENTRY_OF_ACCOUNT(accountIdPredicate)
                .and(ENTRY_OF_METADATA(metadataPredicate))
                .and(ENTRY_OF_DATE(occuredAtPredicate))
                .and(ENTRY_OF_AMOUNT(amountPredicate));
        Predicate<Account> accountFilter = account -> accountDescPredicate.test(account.name());

        return new Filter(entryFilter, accountFilter);
    }


}

class EntryFilter {

    static Predicate<Map<String, String>> HAVING_VALUES(String key, String value) {
        return metadata -> metadata.containsKey(key) && metadata.get(key).equals(value);
    }

    static Predicate<Entry> ENTRY_HAVING_METADATA(Predicate<Map<String, String>> metadataPredicate) {
        return entry -> metadataPredicate.test(entry.metadata().metadata());
    }

    static Predicate<Entry> ENTRY_OF_ACCOUNT(AccountId accountId) {
        return entry -> entry.accountId().equals(accountId);
    }

    static Predicate<Entry> ENTRY_OF_ACCOUNT(Predicate<AccountId> accountPredicate) {
        return entry -> accountPredicate.test(entry.accountId());
    }

    static Predicate<Entry> ENTRY_OF_AMOUNT(Predicate<Money> amountPredicate) {
        return entry -> amountPredicate.test(entry.amount());
    }

    static Predicate<Entry> ENTRY_OF_DATE(Predicate<Instant> datePredicate) {
        return entry -> datePredicate.test(entry.occurredAt());
    }

    static Predicate<Entry> ENTRY_OF_METADATA(Predicate<Map<String, String>> metadataPredicate) {
        return entry -> metadataPredicate.test(entry.metadata().metadata());
    }
}
