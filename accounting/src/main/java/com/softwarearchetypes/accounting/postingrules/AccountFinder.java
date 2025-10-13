package com.softwarearchetypes.accounting.postingrules;

import java.util.HashMap;
import java.util.Map;

import com.softwarearchetypes.accounting.AccountView;
import com.softwarearchetypes.accounting.AccountId;

public interface AccountFinder {

    TargetAccounts findAccounts(PostingContext context);

    static AccountFinder fixed(String tag, AccountId accountId) {
        return context -> {
            AccountView account = context.accountingFacade()
                    .findAccount(accountId)
                    .orElseThrow(() -> new IllegalArgumentException("Account " + accountId + " not found"));
            return TargetAccounts.of(Map.of(tag, account));
        };
    }

    static AccountFinder fixed(Map<String, AccountId> accountIds) {
        return context -> {
            Map<String, AccountView> accounts = new HashMap<>();
            for (Map.Entry<String, AccountId> entry : accountIds.entrySet()) {
                AccountView account = context.accountingFacade()
                        .findAccount(entry.getValue())
                        .orElseThrow(() -> new IllegalArgumentException("Account " + entry.getValue() + " not found"));
                accounts.put(entry.getKey(), account);
            }
            return TargetAccounts.of(accounts);
        };
    }
}