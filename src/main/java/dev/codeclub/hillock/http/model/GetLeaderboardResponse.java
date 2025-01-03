package dev.codeclub.hillock.http.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GetLeaderboardResponse(@JsonProperty("leaderboard") List<ProfileScore> leaderboard) {
    public static record ProfileScore(@JsonProperty("username") String username, @JsonProperty("pokerScore") int pokerScore) {
    }
}