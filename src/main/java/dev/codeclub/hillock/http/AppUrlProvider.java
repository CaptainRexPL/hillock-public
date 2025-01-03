package dev.codeclub.hillock.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.util.UUID;

@Component
public class AppUrlProvider {
    private final URI baseUri;

    public AppUrlProvider(@Value("${hillock.baseurl:http://localhost:8080}") String baseUrl) throws URISyntaxException {
        this.baseUri = new URI(baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl);
    }

    public String homeUrl() {
        return UriComponentsBuilder.fromUri(baseUri)
                .path("/")
                .toUriString();
    }

    public String loginUrl() {
        return UriComponentsBuilder.fromUri(baseUri)
                .path("/login")
                .toUriString();
    }

    public String signupUrl() {
        return UriComponentsBuilder.fromUri(baseUri)
                .path("/signup")
                .toUriString();
    }

    public String emailVerificationUrl(String token, UUID namespace) {
        return UriComponentsBuilder.fromUri(baseUri)
                .path("/api/account/verify/{namespace}/{token}")
                .buildAndExpand(
                        encodeURIComponent(namespace.toString()),
                        encodeURIComponent(token))
                .toUriString();
    }

    public String logoUrl(int size) {
        return UriComponentsBuilder.fromUri(baseUri)
                .path("/logo.png")
                .queryParam("size", size)
                .toUriString();
    }

    public String resetPassword(String token, UUID namespace) {
        return UriComponentsBuilder.fromUri(baseUri)
                .path("/api/account/reset/{namespace}/{token}")
                .buildAndExpand(
                        encodeURIComponent(namespace.toString()),
                        encodeURIComponent(token))
                .toUriString();
    }

    /**
     * Helper method to encode URL components properly.
     *
     * @param value String to encode
     * @return Encoded string
     */
    private String encodeURIComponent(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
