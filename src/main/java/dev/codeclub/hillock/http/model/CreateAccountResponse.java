package dev.codeclub.hillock.http.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.codeclub.hillock.model.PublicUserProfile;

public record CreateAccountResponse(@JsonProperty("profile") PublicUserProfile publicProfile, @JsonProperty("accessToken") String accessToken, @JsonProperty("refreshToken") String refreshToken) {
}
