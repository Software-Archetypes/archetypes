package com.softwarearchetypes.accounting;

import java.util.HashMap;
import java.util.Map;

import com.softwarearchetypes.quantity.money.Money;

import static com.softwarearchetypes.quantity.money.Money.zeroPln;
import static java.util.stream.Collectors.toMap;

public record AccountAmounts(Map<AccountId, Money> all) {

    public AccountAmounts(Map<AccountId, Money> all) {
        this.all = all
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() != null)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static AccountAmounts of(Map<AccountId, Money> amounts) {
        return new AccountAmounts(amounts);
    }

    public static AccountAmounts empty() {
        return of(new HashMap<>());
    }

    public AccountAmounts add(AccountId accountId, Money amount) {
        Map<AccountId, Money> newAmounts = new HashMap<>(all);
        newAmounts.put(accountId, amount);
        return new AccountAmounts(newAmounts);
    }

    public AccountAmounts subtract(AccountAmounts toSubtract) {
        Map<AccountId, Money> diff = new HashMap<>();
        for (Map.Entry<AccountId, Money> entry : all.entrySet()) {
            AccountId accountId = entry.getKey();
            Money cappedAmount = entry.getValue();
            Money notCappedAmount = toSubtract.all.getOrDefault(accountId, zeroPln());
            Money difference = cappedAmount.subtract(notCappedAmount);
            if (difference.isNegative()) {
                diff.put(accountId, difference);
            }
        }
        return new AccountAmounts(diff);
    }

    public AccountAmounts add(AccountAmounts toAdd) {
        Map<AccountId, Money> result = new HashMap<>(all);
        for (Map.Entry<AccountId, Money> entry : toAdd.all().entrySet()) {
            AccountId accountId = entry.getKey();
            Money amount = entry.getValue();
            Money updatedAmount = result.getOrDefault(accountId, zeroPln()).add(amount);
            result.put(accountId, updatedAmount);
        }
        return new AccountAmounts(result);
    }

    public Money sum() {
        return all.values().stream().reduce(zeroPln(), Money::add);
    }
}
