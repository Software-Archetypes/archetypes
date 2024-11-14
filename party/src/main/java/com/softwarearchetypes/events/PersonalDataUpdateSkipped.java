package com.softwarearchetypes.events;

import java.util.Objects;

public final class PersonalDataUpdateSkipped implements PersonalDataUpdateSucceeded {

    private static final String NO_CHANGE_IDENTIFIED_REASON = "NO_CHANGE_IDENTIFIED";

    private final String firstName;
    private final String lastName;
    private final String reason;

    private PersonalDataUpdateSkipped(String firstName, String lastName, String reason) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.reason = reason;
    }

    public static PersonalDataUpdateSkipped dueToNoChangeIdentifiedFor(String firstName, String lastName) {
        return new PersonalDataUpdateSkipped(firstName, lastName, NO_CHANGE_IDENTIFIED_REASON);
    }

    String fFirstName() {
        return firstName;
    }

    String lastName() {
        return lastName;
    }

    String reason() {
        return reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PersonalDataUpdateSkipped that)) {
            return false;
        }
        return Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(reason, that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, reason);
    }
}
