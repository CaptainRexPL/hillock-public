package dev.codeclub.hillock.http.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.codeclub.hillock.database.model.User;

public record LoginResponse(@JsonProperty("user") UserResponse user, @JsonProperty("token") String token) {
}
