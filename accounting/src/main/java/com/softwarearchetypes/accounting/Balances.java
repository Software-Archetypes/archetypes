package com.softwarearchetypes.accounting;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.softwarearchetypes.quantity.money.Money;

public record Balances(Map<AccountId, Money> balances) {

    public static Balances empty() {
        return new Balances(Map.of());
    }

    public Optional<Money> get(AccountId accountId) {
        return Optional.ofNullable(balances.get(accountId));
    }

    public Money sum() {
        return balances
                .values()
                .stream()
                .reduce(Money.zeroPln(), Money::add);
    }

    public int size() {
        return balances.size();
    }

    public Set<AccountId> accounts() {
        return balances().keySet();
    }
}

