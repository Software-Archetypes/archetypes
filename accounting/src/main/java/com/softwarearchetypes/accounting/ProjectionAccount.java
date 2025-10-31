package com.softwarearchetypes.accounting;

import java.util.function.Predicate;

import com.softwarearchetypes.common.Version;

class ProjectionAccount {

    //lock
    private AccountId accountId;
    //w bazie danych ten predicate to sql
    private Filter filter;
    private String name;
    private final Version version;

    ProjectionAccount(AccountId accountId, Filter filter, String name) {
        this(accountId, filter, name, Version.initial());
    }

    ProjectionAccount(AccountId accountId, Filter filter, String name, Version version) {
        this.accountId = accountId;
        this.filter = filter;
        this.name = name;
        this.version = version;
    }

    String desc() {
        return name;
    }

    AccountId id() {
        return accountId;
    }

    Filter filter() {
        return filter;
    }

    Version version() {
        return version;
    }
}


//wrzucone w jedna klase, zeby latwiej bylo tlumaczyc na sql
record Filter(Predicate<Entry> entryFilter, Predicate<Account> accountFilter) {

    static Filter just(Predicate<Entry> entryFilter) {
        return new Filter(entryFilter, account -> true);
    }
}
