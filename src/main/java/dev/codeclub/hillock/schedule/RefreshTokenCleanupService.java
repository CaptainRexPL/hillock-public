package dev.codeclub.hillock.schedule;

import dev.codeclub.hillock.database.repository.RefreshTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RefreshTokenCleanupService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenCleanupService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Scheduled(fixedRateString = "${refresh.token.cleanup.interval:3600000}")
    public void cleanUpExpiredRefreshTokens() {
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.deleteAllByExpiresAtBefore(now);
    }
}