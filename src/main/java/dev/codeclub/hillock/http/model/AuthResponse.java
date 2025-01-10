package dev.codeclub.hillock.http.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthResponse {
    private final String accessToken;
    private final String refreshToken;

    @JsonCreator
    public AuthResponse(@JsonProperty("accessToken") String accessToken,
                        @JsonProperty("refreshToken") String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
