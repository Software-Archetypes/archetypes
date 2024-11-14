package com.softwarearchetypes.party;

import java.util.regex.Pattern;

record PersonalIdentificationNumber(String value) implements RegisteredIdentifier {

    private static final Pattern PATTERN = Pattern.compile("\\d{11}");
    //can be any other pattern
    private static final String TYPE = "PERSONAL_IDENTIFICATION_NUMBER";

    PersonalIdentificationNumber {
        if (!PATTERN.matcher(value).matches()) {
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
