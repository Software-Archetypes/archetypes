package com.softwarearchetypes.party;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

/**
 * Web address (URL) details with validation.
 * Validates URL format and structure.
 */
public record WebAddressDetails(String url) implements AddressDetails {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?|ftp)://[a-zA-Z0-9.-]+(:\\d+)?(/.*)?$"
    );

    public WebAddressDetails {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }

        if (!URL_PATTERN.matcher(url).matches()) {
            throw new IllegalArgumentException("Invalid URL format: " + url);
        }

        // Additional validation using Java's URI
        try {
            URI uri = new URI(url);
            uri.toURL(); // Validates that it can be converted to URL
        } catch (URISyntaxException | MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL: " + url, e);
        }
    }

    public static WebAddressDetails of(String url) {
        return new WebAddressDetails(url);
    }

    /**
     * Returns the protocol (scheme) of the URL.
     */
    public String protocol() {
        try {
            return new URI(url).getScheme();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("URL validation failed during construction", e);
        }
    }

    /**
     * Returns the host part of the URL.
     */
    public String host() {
        try {
            return new URI(url).getHost();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("URL validation failed during construction", e);
        }
    }
}
