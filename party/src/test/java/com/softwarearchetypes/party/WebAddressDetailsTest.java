package com.softwarearchetypes.party;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebAddressDetailsTest {

    @Test
    void shouldCreateValidWebAddress() {
        //when
        WebAddressDetails webAddress = new WebAddressDetails("https://example.com");

        //then
        assertEquals("https://example.com", webAddress.url());
    }

    @Test
    void shouldAcceptVariousValidUrlFormats() {
        //expect - https URLs
        assertDoesNotThrow(() -> new WebAddressDetails("https://example.com"));
        assertDoesNotThrow(() -> new WebAddressDetails("https://www.example.com"));
        assertDoesNotThrow(() -> new WebAddressDetails("https://sub.domain.example.com"));
        assertDoesNotThrow(() -> new WebAddressDetails("https://example.com:8080"));
        assertDoesNotThrow(() -> new WebAddressDetails("https://example.com/path"));
        assertDoesNotThrow(() -> new WebAddressDetails("https://example.com/path/to/resource"));
        assertDoesNotThrow(() -> new WebAddressDetails("https://example.com/path?query=param"));

        //expect - http URLs
        assertDoesNotThrow(() -> new WebAddressDetails("http://example.com"));
        assertDoesNotThrow(() -> new WebAddressDetails("http://localhost:8080"));

        //expect - ftp URLs
        assertDoesNotThrow(() -> new WebAddressDetails("ftp://ftp.example.com"));
    }

    @Test
    void shouldRejectNullUrl() {
        //expect
        assertThrows(IllegalArgumentException.class, () -> new WebAddressDetails(null));
    }

    @Test
    void shouldRejectEmptyUrl() {
        //expect
        assertThrows(IllegalArgumentException.class, () -> new WebAddressDetails(""));
        assertThrows(IllegalArgumentException.class, () -> new WebAddressDetails("   "));
    }

    @Test
    void shouldRejectInvalidUrlFormat() {
        //expect - no protocol
        assertThrows(IllegalArgumentException.class, () -> new WebAddressDetails("example.com"));
        assertThrows(IllegalArgumentException.class, () -> new WebAddressDetails("www.example.com"));

        //expect - invalid protocol
        assertThrows(IllegalArgumentException.class, () -> new WebAddressDetails("htp://example.com"));
        assertThrows(IllegalArgumentException.class, () -> new WebAddressDetails("://example.com"));

        //expect - malformed
        assertThrows(IllegalArgumentException.class, () -> new WebAddressDetails("https://"));
        assertThrows(IllegalArgumentException.class, () -> new WebAddressDetails("not a url"));
        assertThrows(IllegalArgumentException.class, () -> new WebAddressDetails("https:// example.com"));
    }

    @Test
    void shouldCreateWebAddressUsingFactoryMethod() {
        //when
        WebAddressDetails webAddress = WebAddressDetails.of("https://example.com");

        //then
        assertEquals("https://example.com", webAddress.url());
    }

    @Test
    void shouldExtractProtocol() {
        //given
        WebAddressDetails httpsAddress = new WebAddressDetails("https://example.com");
        WebAddressDetails httpAddress = new WebAddressDetails("http://example.com");
        WebAddressDetails ftpAddress = new WebAddressDetails("ftp://ftp.example.com");

        //expect
        assertEquals("https", httpsAddress.protocol());
        assertEquals("http", httpAddress.protocol());
        assertEquals("ftp", ftpAddress.protocol());
    }

    @Test
    void shouldExtractHost() {
        //given
        WebAddressDetails address1 = new WebAddressDetails("https://example.com");
        WebAddressDetails address2 = new WebAddressDetails("https://www.example.com:8080");
        WebAddressDetails address3 = new WebAddressDetails("https://sub.domain.example.com/path");

        //expect
        assertEquals("example.com", address1.host());
        assertEquals("www.example.com", address2.host());
        assertEquals("sub.domain.example.com", address3.host());
    }
}
