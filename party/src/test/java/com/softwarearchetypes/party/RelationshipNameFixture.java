package com.softwarearchetypes.party;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

final class RelationshipNameFixture {

    static String someRelationshipNameValue() {
        return randomAlphabetic(10);
    }

    static RelationshipName someRelationshipName() {
        return RelationshipName.of(someRelationshipNameValue());
    }
}
