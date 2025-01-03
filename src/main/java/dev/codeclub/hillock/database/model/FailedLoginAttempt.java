package dev.codeclub.hillock.database.model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(name = "failed_login_attempts", schema = "public")
public class FailedLoginAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "failed_login_attempts_id_gen")
    @SequenceGenerator(name = "failed_login_attempts_id_gen", sequenceName = "failed_login_attempts_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "ip_address", nullable = false, length = 25)
    private String ipAddress;

    @Column(name = "email", length = 50)
    private String email;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public static FailedLoginAttempt create(String ipAddress, String email) {
        FailedLoginAttempt failedLoginAttempt = new FailedLoginAttempt();
        failedLoginAttempt.setIpAddress(ipAddress);
        failedLoginAttempt.setEmail(email);
        return failedLoginAttempt;
    }

}