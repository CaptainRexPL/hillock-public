package dev.codeclub.hillock.security;

import com.google.common.io.BaseEncoding;
import dev.codeclub.hillock.database.service.UserService;
import dev.codeclub.hillock.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ApiToken {

    private static final int VERSION = 2;

    public final long profileId;
    public final long expiresAt;
    public final Role role;
    private final UserService userService;

    public ApiToken(long profileId, long expiresAt, Role role, UserService userService) {
        this.profileId = profileId;
        this.expiresAt = expiresAt;
        this.role = role;
        this.userService = userService;
    }

    public ApiToken(long profileId, Role role, UserService userService) {
        this.profileId = profileId;
        this.role = role;
        this.userService = userService;
        this.expiresAt = new Date().getTime() + TimeUnit.DAYS.toMillis(7);
    }

    public boolean isExpired() {
        return new Date().after(new Date(expiresAt));
    }

    public String serialize(TokenCrypter crypter) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (DataOutputStream dataStream = new DataOutputStream(outputStream)) {
                dataStream.write(VERSION);
                dataStream.writeLong(profileId);
                dataStream.writeLong(expiresAt);
                dataStream.writeUTF(role.name());
            }
            return BaseEncoding.base64Url().encode(crypter.crypt(outputStream.toByteArray()));
        }
    }

    public static ApiToken deserialize(TokenCrypter crypter, String token, UserService userService) throws IOException {
        byte[] bytes = BaseEncoding.base64Url().decode(token);
        byte[] decryptedBytes = crypter.decrypt(bytes);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(decryptedBytes)) {
            try (DataInputStream dataStream = new DataInputStream(inputStream)) {
                int version = dataStream.read();
                long profileId;
                long expiresAt;
                Role role;
                switch (version) {
                    case 1 -> {
                        profileId = dataStream.readLong();
                        expiresAt = dataStream.readLong();
                        role = getRole(userService, profileId);
                    }
                    case 2 -> {
                        profileId = dataStream.readLong();
                        expiresAt = dataStream.readLong();
                        role = Role.valueOf(dataStream.readUTF());
                    }
                    default -> throw new IllegalStateException("unknown version " + version);
                }
                return new ApiToken(profileId, expiresAt, role, userService);
            }
        }
    }

    public static Role getRole(UserService userService, long profileId) {
        return userService.getUserById(profileId)
                .map(profile -> Role.valueOf(profile.getRole()))
                .orElse(Role.GUEST);
    }
}
