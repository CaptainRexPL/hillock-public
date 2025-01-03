package dev.codeclub.hillock.database.model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(name = "unauthorized_attempts", schema = "public")
public class UnauthorizedAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "unauthorized_attempts_id_gen")
    @SequenceGenerator(name = "unauthorized_attempts_id_gen", sequenceName = "unauthorized_attempts_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "ip_address", nullable = false, length = 25)
    private String ipAddress;

    @Column(name = "profile_id")
    private Long profileId;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "\"timestamp\"", nullable = false, insertable = false)
    private Instant timestamp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public static UnauthorizedAttempt create(String ipAddress, Long profileId) {
        UnauthorizedAttempt unathorizedAttempt = new UnauthorizedAttempt();
        unathorizedAttempt.setIpAddress(ipAddress);
        unathorizedAttempt.setProfileId(profileId);
        return unathorizedAttempt;
    }

}