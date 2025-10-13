package com.softwarearchetypes.accounting.postingrules;

import java.util.Map;
import java.util.Optional;

import com.softwarearchetypes.accounting.AccountView;

public record TargetAccounts(Map<String, AccountView> accounts) {

    public Optional<AccountView> get(String tag) {
        return Optional.ofNullable(accounts.get(tag));
    }

    public AccountView getRequired(String tag) {
        return get(tag).orElseThrow(() -> new IllegalArgumentException("Required account with tag '" + tag + "' not found"));
    }

    public static TargetAccounts of(Map<String, AccountView> accounts) {
        return new TargetAccounts(Map.copyOf(accounts));
    }
}