package com.softwarearchetypes.party;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PhoneAddressDetailsTest {

    @Test
    void shouldCreateValidPhoneNumber() {
        //when
        PhoneAddressDetails phone = new PhoneAddressDetails("+48123456789");

        //then
        assertEquals("+48123456789", phone.phoneNumber());
    }

    @Test
    void shouldAcceptVariousValidPhoneFormats() {
        //expect - international formats
        assertDoesNotThrow(() -> new PhoneAddressDetails("+48123456789"));
        assertDoesNotThrow(() -> new PhoneAddressDetails("+1 234 567 8900"));
        assertDoesNotThrow(() -> new PhoneAddressDetails("+44-20-1234-5678"));
        assertDoesNotThrow(() -> new PhoneAddressDetails("+33 (1) 23 45 67 89"));

        //expect - local formats
        assertDoesNotThrow(() -> new PhoneAddressDetails("123456789"));
        assertDoesNotThrow(() -> new PhoneAddressDetails("123 456 789"));
        assertDoesNotThrow(() -> new PhoneAddressDetails("(123) 456-789"));
    }

    @Test
    void shouldRejectNullPhoneNumber() {
        //expect
        assertThrows(IllegalArgumentException.class, () -> new PhoneAddressDetails(null));
    }

    @Test
    void shouldRejectEmptyPhoneNumber() {
        //expect
        assertThrows(IllegalArgumentException.class, () -> new PhoneAddressDetails(""));
        assertThrows(IllegalArgumentException.class, () -> new PhoneAddressDetails("   "));
    }

    @Test
    void shouldRejectTooShortPhoneNumber() {
        //expect - less than 7 digits
        assertThrows(IllegalArgumentException.class, () -> new PhoneAddressDetails("123"));
        assertThrows(IllegalArgumentException.class, () -> new PhoneAddressDetails("+48 12"));
    }

    @Test
    void shouldRejectTooLongPhoneNumber() {
        //expect - more than 20 digits
        assertThrows(IllegalArgumentException.class, () -> new PhoneAddressDetails("123456789012345678901"));
    }

    @Test
    void shouldRejectInvalidPhoneFormat() {
        //expect
        assertThrows(IllegalArgumentException.class, () -> new PhoneAddressDetails("abc"));
        assertThrows(IllegalArgumentException.class, () -> new PhoneAddressDetails("12-abc-456"));
        assertThrows(IllegalArgumentException.class, () -> new PhoneAddressDetails("phone number"));
    }

    @Test
    void shouldCreatePhoneUsingFactoryMethod() {
        //when
        PhoneAddressDetails phone = PhoneAddressDetails.of("+48 123 456 789");

        //then
        assertEquals("+48 123 456 789", phone.phoneNumber());
    }

    @Test
    void shouldNormalizePhoneNumber() {
        //given
        PhoneAddressDetails phone1 = new PhoneAddressDetails("+48 123 456 789");
        PhoneAddressDetails phone2 = new PhoneAddressDetails("+48-123-456-789");
        PhoneAddressDetails phone3 = new PhoneAddressDetails("+48 (123) 456-789");
        PhoneAddressDetails phone4 = new PhoneAddressDetails("123456789");

        //expect
        assertEquals("+48123456789", phone1.normalized());
        assertEquals("+48123456789", phone2.normalized());
        assertEquals("+48123456789", phone3.normalized());
        assertEquals("123456789", phone4.normalized());
    }
}
