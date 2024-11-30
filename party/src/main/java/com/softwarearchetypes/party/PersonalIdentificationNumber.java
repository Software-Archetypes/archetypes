package com.softwarearchetypes.party;

import java.util.regex.Pattern;

import static java.util.Optional.ofNullable;

record PersonalIdentificationNumber(String value) implements RegisteredIdentifier {

    private static final Pattern PATTERN = Pattern.compile("\\d{11}");
    //can be any other pattern
    private static final String TYPE = "PERSONAL_IDENTIFICATION_NUMBER";

    PersonalIdentificationNumber {
        if (ofNullable(value).filter(it -> PATTERN.matcher(it).matches()).isEmpty()) {
            throw new IllegalArgumentException("Personal identification number does not meet syntax criteria");
        }
    }

    static PersonalIdentificationNumber of(String value) {
        return new PersonalIdentificationNumber(value);
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public String asString() {
        return value;
    }
}
