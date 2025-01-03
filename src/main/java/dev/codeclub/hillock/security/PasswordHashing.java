package dev.codeclub.hillock.security;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class PasswordHashing {

    private final byte[] salt;

    public PasswordHashing(@Value("${security.password.salt:VSZL*bR8-=r]r5P_}")String salt) {
        this.salt = salt.getBytes(StandardCharsets.UTF_8);
    }

    public String hash(String input) {
        byte[] hash = BCrypt.withDefaults().hash(12, salt, input.getBytes(StandardCharsets.UTF_8));
        return new String(hash, StandardCharsets.UTF_8);
    }

    public boolean verify(char[] password, char[] hash) {
        return BCrypt.verifyer().verify(password, hash).verified;
    }
}
