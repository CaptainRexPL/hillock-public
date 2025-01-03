package dev.codeclub.hillock.http.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GetUserRequest(@JsonProperty("userId") Long userId, @JsonProperty("email") String email, @JsonProperty("username") String username, @JsonProperty("discordId") Long discordId) {
}
