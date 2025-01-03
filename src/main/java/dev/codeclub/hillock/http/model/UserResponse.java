package dev.codeclub.hillock.http.model;

import dev.codeclub.hillock.database.model.User;
import dev.codeclub.hillock.model.PublicUserProfile;

import java.time.Instant;

public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private Boolean emailverified ;
    private String role;
    private Boolean disabled;
    private Integer pokerscore;
    private Long discordid;
    private String lastLoginIp;
    private Instant lastLoginTimestamp;
    private String prevLoginIp;
    private Instant prevLoginTimestamp;
    private String lastFailedLoginIp;
    private Instant lastFailedLoginTimestamp;
    private Long loginCount;
    private Long failedLoginCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getEmailverified() {
        return emailverified;
    }

    public void setEmailverified(Boolean emailverified) {
        this.emailverified = emailverified;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public Integer getPokerscore() {
        return pokerscore;
    }

    public void setPokerscore(Integer pokerscore) {
        this.pokerscore = pokerscore;
    }

    public Long getDiscordid() {
        return discordid;
    }

    public void setDiscordid(Long discordid) {
        this.discordid = discordid;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public Instant getLastLoginTimestamp() {
        return lastLoginTimestamp;
    }

    public void setLastLoginTimestamp(Instant lastLoginTimestamp) {
        this.lastLoginTimestamp = lastLoginTimestamp;
    }

    public String getPrevLoginIp() {
        return prevLoginIp;
    }

    public void setPrevLoginIp(String prevLoginIp) {
        this.prevLoginIp = prevLoginIp;
    }

    public Instant getPrevLoginTimestamp() {
        return prevLoginTimestamp;
    }

    public void setPrevLoginTimestamp(Instant prevLoginTimestamp) {
        this.prevLoginTimestamp = prevLoginTimestamp;
    }

    public String getLastFailedLoginIp() {
        return lastFailedLoginIp;
    }

    public void setLastFailedLoginIp(String lastFailedLoginIp) {
        this.lastFailedLoginIp = lastFailedLoginIp;
    }

    public Instant getLastFailedLoginTimestamp() {
        return lastFailedLoginTimestamp;
    }

    public void setLastFailedLoginTimestamp(Instant lastFailedLoginTimestamp) {
        this.lastFailedLoginTimestamp = lastFailedLoginTimestamp;
    }

    public Long getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(Long loginCount) {
        this.loginCount = loginCount;
    }

    public Long getFailedLoginCount() {
        return failedLoginCount;
    }

    public void setFailedLoginCount(Long failedLoginCount) {
        this.failedLoginCount = failedLoginCount;
    }

    public static UserResponse fromDbUser(User dbModel) {
        UserResponse response = new UserResponse();
        response.id = dbModel.getId();
        response.username = dbModel.getUsername();
        response.email = dbModel.getEmail();
        response.emailverified = dbModel.getEmailverified();
        response.role = dbModel.getRole();
        response.disabled = dbModel.getDisabled();
        response.pokerscore = dbModel.getPokerscore();
        response.discordid = dbModel.getDiscordid();
        response.lastLoginIp = dbModel.getLastLoginIp();
        response.lastLoginTimestamp = dbModel.getLastLoginTimestamp();
        response.prevLoginIp = dbModel.getPrevLoginIp();
        response.prevLoginTimestamp = dbModel.getPrevLoginTimestamp();
        response.lastFailedLoginIp = dbModel.getLastFailedLoginIp();
        response.lastFailedLoginTimestamp = dbModel.getLastFailedLoginTimestamp();
        response.loginCount = dbModel.getLoginCount();
        response.failedLoginCount = dbModel.getFailedLoginCount();
        return response;
    }

    public static PublicUserProfile toPublic(UserResponse user) {
        return new PublicUserProfile(user.id, user.username,  user.role, user.pokerscore, user.discordid);
    }
}
