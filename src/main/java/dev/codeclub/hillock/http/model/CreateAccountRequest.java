package dev.codeclub.hillock.http.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateAccountRequest(@JsonProperty("email") String email, @JsonProperty("username") String username, @JsonProperty("password") String password, @JsonProperty("confirmPassword") String confirmPassword, @JsonProperty("invite") String invite) {
}
