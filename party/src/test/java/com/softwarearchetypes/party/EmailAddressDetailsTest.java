package com.softwarearchetypes.party;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailAddressDetailsTest {

    @Test
    void shouldCreateValidEmailAddress() {
        //when
        EmailAddressDetails email = new EmailAddressDetails("john.doe@example.com");

        //then
        assertEquals("john.doe@example.com", email.email());
    }

    @Test
    void shouldAcceptVariousValidEmailFormats() {
        //expect
        assertDoesNotThrow(() -> new EmailAddressDetails("user@domain.com"));
        assertDoesNotThrow(() -> new EmailAddressDetails("user.name@domain.com"));
        assertDoesNotThrow(() -> new EmailAddressDetails("user+tag@domain.co.uk"));
        assertDoesNotThrow(() -> new EmailAddressDetails("user_123@sub.domain.org"));
        assertDoesNotThrow(() -> new EmailAddressDetails("first-last@example-domain.com"));
    }

    @Test
    void shouldRejectNullEmail() {
        //expect
        assertThrows(IllegalArgumentException.class, () -> new EmailAddressDetails(null));
    }

    @Test
    void shouldRejectEmptyEmail() {
        //expect
        assertThrows(IllegalArgumentException.class, () -> new EmailAddressDetails(""));
        assertThrows(IllegalArgumentException.class, () -> new EmailAddressDetails("   "));
    }

    @Test
    void shouldRejectInvalidEmailFormat() {
        //expect
        assertThrows(IllegalArgumentException.class, () -> new EmailAddressDetails("invalid"));
        assertThrows(IllegalArgumentException.class, () -> new EmailAddressDetails("@domain.com"));
        assertThrows(IllegalArgumentException.class, () -> new EmailAddressDetails("user@"));
        assertThrows(IllegalArgumentException.class, () -> new EmailAddressDetails("user domain.com"));
        assertThrows(IllegalArgumentException.class, () -> new EmailAddressDetails("user@domain"));
        assertThrows(IllegalArgumentException.class, () -> new EmailAddressDetails("user@@domain.com"));
    }

    @Test
    void shouldCreateEmailUsingFactoryMethod() {
        //when
        EmailAddressDetails email = EmailAddressDetails.of("test@example.com");

        //then
        assertEquals("test@example.com", email.email());
    }
}
