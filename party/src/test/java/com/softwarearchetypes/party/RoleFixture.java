package com.softwarearchetypes.party;

import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

final class RoleFixture {

    static String someRoleName() {
        return randomAlphabetic(10);
    }

    static Role someRole() {
        return Role.of(someRoleName());
    }

    static Set<Role> someRoleSetOfSize(int size) {
        return IntStream.range(0, size).mapToObj(it -> someRole()).collect(toSet());
    }

}
