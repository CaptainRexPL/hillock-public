package dev.codeclub.hillock.security;


import com.google.common.io.BaseEncoding;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public final class VerificationToken {

    private static final Logger LOGGER = LogManager.getLogger(VerificationToken.class.getName());

    private static final int VERSION = 1;

    public final long userId;
    public final long expiresAt;

    public VerificationToken(long userId, long expiresAt) {
        this.userId = userId;
        this.expiresAt = expiresAt;
    }

    public UUID getUUID() {
        String namespace = "6ba7b810-9dad-11d1-80b4-00c04fd430c8";
        String timestampString = String.valueOf(expiresAt);
        return UUID.nameUUIDFromBytes((namespace + timestampString).getBytes(StandardCharsets.UTF_8));
    }

    public boolean isExpired() {
        return new Date().after(new Date(expiresAt));
    }

    public static Optional<VerificationToken> from(TokenCrypter crypter, String token) {
        byte[] bytes; {
            try {
                bytes = crypter.decrypt(BaseEncoding.base64Url().decode(token));
            } catch (Throwable e) {
                LOGGER.error("Problem decoding token bytes", e);
                return Optional.empty();
            }
        }
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes)) {
            try (DataInputStream objectStream = new DataInputStream(byteStream)) {
                long userId;
                long expiresAt;
                int version = objectStream.readInt();
                switch (version) {
                    case 1:
                        userId = objectStream.readLong();
                        expiresAt = objectStream.readLong();
                        break;
                    default:
                        throw new IllegalStateException("unsupported version " + version);
                }
                Date expiredAt = new Date(expiresAt);
                if (new Date().after(expiredAt)) {
                    LOGGER.info("Token expired at " + expiredAt);
                    return Optional.empty();
                } else {
                    return Optional.of(new VerificationToken(userId, expiresAt));
                }
            }
        } catch (IOException e) {
            LOGGER.error("Decoding token structure", e);
            return Optional.empty();
        }
    }

    public String serialize(TokenCrypter crypter) throws IOException {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
            try (DataOutputStream objectStream = new DataOutputStream(byteStream)) {
                objectStream.writeInt(VERSION);
                objectStream.writeLong(userId);
                objectStream.writeLong(expiresAt);
            }
            return BaseEncoding.base64Url().encode(crypter.crypt(byteStream.toByteArray()));
        }
    }
}