package com.softwarearchetypes.party;

import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

final class RegisteredIdentifierFixture {

    static String someType() {
        return randomAlphabetic(10);
    }

    static String someValue() {
        return randomAlphabetic(10);
    }

    static RegisteredIdentifier someRegisteredIdentifier() {
        return new RegisteredIdentifier() {

            private static final String TYPE = someType();
            private static final String VALUE = someValue();

            @Override
            public String type() {
                return TYPE;
            }

            @Override
            public String asString() {
                return VALUE;
            }
        };
    }

    static Set<RegisteredIdentifier> someIdentifierSetOfSize(int size) {
        return IntStream.range(0, size).mapToObj(it -> someRegisteredIdentifier()).collect(toSet());
    }

}
