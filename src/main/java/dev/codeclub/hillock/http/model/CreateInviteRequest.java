package dev.codeclub.hillock.http.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.codeclub.hillock.database.model.Invite;

public record CreateInviteRequest(@JsonProperty("invite") String invite) {
}
