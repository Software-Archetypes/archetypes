package com.softwarearchetypes.accounting;

import java.util.Arrays;

import com.softwarearchetypes.common.StringUtils;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

record AccountName(String value) {

    final static String DEFAULT_DELIMITER = ":";

    AccountName {
        checkArgument(StringUtils.isNotBlank(value), "Account name cannot be empty");
    }

    static AccountName of(String value) {
        return new AccountName(value);
    }

    static AccountName compositeFrom(String... components) {
        return compositeFrom(DEFAULT_DELIMITER, components);
    }

    static AccountName compositeFrom(String delimiter, String... components) {
        checkArgument(components != null && components.length > 0, "Account name cannot be built from an empty array");
        Arrays.stream(components).forEach(val -> checkArgument(StringUtils.isNotBlank(val), "Account name component cannot be empty"));
        return new AccountName(String.join(delimiter, components));
    }
}