package dev.codeclub.hillock.security;

import com.google.common.io.BaseEncoding;
import org.jasypt.util.binary.AES256BinaryEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Service
public class TokenCrypter {

    private final AES256BinaryEncryptor encryptor;

    public TokenCrypter(@Value("${security.token.password:insecureTokenCrypterPassword}") String password) {
        encryptor = new AES256BinaryEncryptor();
        encryptor.setPassword(password);
    }

    public byte[] decrypt(byte[] bytes) {
        return encryptor.decrypt(bytes);
    }

    public byte[] crypt(byte[] bytes) {
        return encryptor.encrypt(bytes);
    }

    private static final SecureRandom SECURERANDOM;

    static {
        try {
            SECURERANDOM = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static byte[] byteToken(int size) {
        byte[] bytes = new byte[size];
        SECURERANDOM.nextBytes(bytes);
        return bytes;
    }

    public static String inviteCode() {
        return BaseEncoding.base64Url().encode(byteToken(8)).replace("==", "");
    }
}
