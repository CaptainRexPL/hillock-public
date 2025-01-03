package dev.codeclub.hillock.database.model;

import dev.codeclub.hillock.enums.Role;
import dev.codeclub.hillock.model.PublicUserProfile;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(name = "users", schema = "public", uniqueConstraints = {
        @UniqueConstraint(name = "users_username_key", columnNames = {"username"}),
        @UniqueConstraint(name = "users_email_key", columnNames = {"email"}),
        @UniqueConstraint(name = "users_inviteid_key", columnNames = {"inviteid"})
})
public class User {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @ColumnDefault("false")
    @Column(name = "emailverified", nullable = false)
    private Boolean emailverified = false;

    @Column(name = "hashedpassword", nullable = false, length = 256)
    private String hashedpassword;

    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @ColumnDefault("false")
    @Column(name = "disabled", nullable = false)
    private Boolean disabled = false;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inviteid", nullable = false)
    private Invite inviteid;

    @ColumnDefault("0")
    @Column(name = "pokerscore", nullable = false)
    private Integer pokerscore;

    @Column(name = "discordid")
    private Long discordid;

    @Column(name = "last_login_ip", length = 25)
    private String lastLoginIp;

    @Column(name = "last_login_timestamp")
    private Instant lastLoginTimestamp;

    @Column(name = "prev_login_ip", length = 25)
    private String prevLoginIp;

    @Column(name = "prev_login_timestamp")
    private Instant prevLoginTimestamp;

    @Column(name = "last_failed_login_ip", length = 25)
    private String lastFailedLoginIp;

    @Column(name = "last_failed_login_timestamp")
    private Instant lastFailedLoginTimestamp;

    @ColumnDefault("0")
    @Column(name = "login_count", nullable = false)
    private Long loginCount = 0L;

    @ColumnDefault("0")
    @Column(name = "failed_login_count", nullable = false)
    private Long failedLoginCount = 0L;

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

    public String getHashedpassword() {
        return hashedpassword;
    }

    public void setHashedpassword(String hashedpassword) {
        this.hashedpassword = hashedpassword;
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

    public Invite getInviteid() {
        return inviteid;
    }

    public void setInviteid(Invite inviteid) {
        this.inviteid = inviteid;
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

    public User() {

    }

    public void setNewLogin(String ip) {
        this.setPrevLoginIp(this.getLastLoginIp());
        this.setLastLoginIp(ip);
        this.setPrevLoginTimestamp(this.getLastLoginTimestamp());
        this.setLastLoginTimestamp(Instant.now());
        this.setLoginCount(this.getLoginCount() + 1);
    }

    public void setNewFailedLogin(String ip) {
        this.setLastFailedLoginIp(ip);
        this.setLastFailedLoginTimestamp(Instant.now());
        this.setFailedLoginCount(this.getFailedLoginCount() + 1);
    }

    public User(Long id, String username, String email, Boolean emailverified, String hashedpassword, String role, Boolean disabled, Invite inviteid, Integer pokerscore) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.emailverified = emailverified;
        this.hashedpassword = hashedpassword;
        this.role = role;
        this.disabled = disabled;
        this.inviteid = inviteid;
        this.pokerscore = pokerscore;
    }
    
    public boolean isAtLeastInRole(Role role) {
        return Role.valueOf(this.getRole()).level >= role.level;
    }

    public PublicUserProfile toPublic() {
        return new PublicUserProfile(this.id, this.username, this.role, this.pokerscore, this.discordid);
    }
}