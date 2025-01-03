package dev.codeclub.hillock.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class PublicUserProfile {
    @JsonProperty("id")
    public final Long id;
    @JsonProperty("name")
    public final String name;
    @JsonProperty("role")
    public final String role;
    @JsonProperty("pokerScore")
    public final Integer pokerScore;
    @JsonProperty("discordId")
    public final Long discordId;

    public PublicUserProfile(@JsonProperty("id") Long id,
                         @JsonProperty("name") String name,
                         @JsonProperty("role") String role,
                         @JsonProperty("pokerScore") Integer pokerScore,
                         @JsonProperty("discordId") Long discordId) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.pokerScore = pokerScore;
        this.discordId = discordId;
    }
}
