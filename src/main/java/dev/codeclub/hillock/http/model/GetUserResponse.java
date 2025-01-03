package dev.codeclub.hillock.http.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.codeclub.hillock.database.model.User;

public record GetUserResponse(@JsonProperty("user")User user) {
}
