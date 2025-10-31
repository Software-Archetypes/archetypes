package com.softwarearchetypes.accounting;

public record CreateAccount(AccountId accountId, String name, String type) {

    public static CreateAccount generateAssetAccount(AccountId accountId) {
        return new CreateAccount(accountId, "", "ASSET");
    }

    public static CreateAccount generateAssetAccount(String name) {
        return new CreateAccount(AccountId.generate(), name, "ASSET");
    }

    public static CreateAccount generate(String type) {
        return new CreateAccount(AccountId.generate(), "", type);
    }

}
