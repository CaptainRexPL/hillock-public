package dev.codeclub.hillock.http.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateUserRequest(
        @JsonProperty("username") String username,
        @JsonProperty("email") String email,
        @JsonProperty("password") String password,
        @JsonProperty("pokerScore") Integer pokerScore,
        @JsonProperty("discordId") Long discordId,
        @JsonProperty("role") String role,
        @JsonProperty("disabled") Boolean disabled) {
}
