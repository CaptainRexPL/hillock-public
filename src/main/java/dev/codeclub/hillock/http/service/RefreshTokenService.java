package dev.codeclub.hillock.http.service;

import dev.codeclub.hillock.database.model.RefreshToken;
import dev.codeclub.hillock.database.repository.RefreshTokenRepository;
import dev.codeclub.hillock.database.model.User;
import dev.codeclub.hillock.http.model.AuthResponse;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.Logger;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final Logger LOGGER = LogManager.getLogger(RefreshTokenService.class);
    public static final String TOKEN_NOT_FOUND = "Token not found";
    public static final String TOKEN_EXPIRED = "Token expired";

    @Value("${security.refresh-token.expiration-days:7}")
    private long expirationDays = 7;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    public AuthResponse generateRefreshToken(User user) {
        String refreshToken = UUID.randomUUID().toString();

        RefreshToken token = new RefreshToken(user, refreshToken, LocalDateTime.now(), LocalDateTime.now().plusDays(expirationDays));

        refreshTokenRepository.save(token);
        return new AuthResponse(jwtService.generateToken(user.getEmail()), refreshToken);
    }

    public AuthResponse generateRefreshToken(String refreshToken) {
        LOGGER.info("Generating new refresh token for: {}", refreshToken);
        RefreshToken storedRefreshToken = refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElse(null);

        if (storedRefreshToken == null) {
            LOGGER.info("Token not found: " + refreshToken);
            return new AuthResponse(TOKEN_NOT_FOUND, TOKEN_NOT_FOUND);
        }
        if (storedRefreshToken.getExpiresAt().isBefore(LocalDateTime.now())){
            LOGGER.info("Token expired at {}", storedRefreshToken.getExpiresAt());
            return new AuthResponse(TOKEN_EXPIRED, TOKEN_EXPIRED);
        }


        User user = storedRefreshToken.getUser();

        if (user == null || user.getDisabled()){
            LOGGER.info("User not found or disabled for token: {}", refreshToken);
            return new AuthResponse(TOKEN_EXPIRED, TOKEN_EXPIRED);
        }
        String newRefreshToken = UUID.randomUUID().toString();
        RefreshToken newToken = new RefreshToken(user, newRefreshToken, LocalDateTime.now(), LocalDateTime.now().plusDays(expirationDays));
        refreshTokenRepository.save(newToken);
        refreshTokenRepository.delete(storedRefreshToken);
        return new AuthResponse(jwtService.generateToken(user.getEmail()), newRefreshToken);
    }

    public boolean validateRefreshToken(String refreshToken) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByRefreshToken(refreshToken);
        if (tokenOpt.isEmpty()) {
            return false;
        }

        RefreshToken token = tokenOpt.get();
        return token.getExpiresAt().isAfter(LocalDateTime.now());
    }

    public void deleteRefreshToken(String refreshToken) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByRefreshToken(refreshToken);
        tokenOpt.ifPresent(refreshTokenRepository::delete);
    }
}

