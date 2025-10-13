package com.softwarearchetypes.accounting.postingrules;

import java.util.UUID;

public record PostingRuleId(UUID uuid) {

    public static PostingRuleId generate() {
        return new PostingRuleId(UUID.randomUUID());
    }

    public static PostingRuleId of(String uuid) {
        return new PostingRuleId(UUID.fromString(uuid));
    }

    @Override
    public String toString() {
        return uuid.toString();
    }
}