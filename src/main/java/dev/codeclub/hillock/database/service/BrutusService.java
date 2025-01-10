package dev.codeclub.hillock.database.service;

import dev.codeclub.hillock.database.model.FailedLoginAttempt;
import dev.codeclub.hillock.database.model.UnauthorizedAttempt;
import dev.codeclub.hillock.database.repository.FailedLoginAttemptRepository;
import dev.codeclub.hillock.database.repository.UnathorizedAttemptRepository;
import dev.codeclub.hillock.event.FailedLoginAttemptEvent;
import dev.codeclub.hillock.event.UnauthorizedAttemptEvent;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class BrutusService {
    private static final Logger LOGGER = LogManager.getLogger(BrutusService.class.getName());

    private final FailedLoginAttemptRepository failedLoginAttemptRepository;
    private final UnathorizedAttemptRepository unathorizedAttemptRepository;

    private final ApplicationEventPublisher eventPublisher;

    public BrutusService(FailedLoginAttemptRepository failedLoginAttemptRepository, UnathorizedAttemptRepository unathorizedAttemptRepository, ApplicationEventPublisher eventPublisher) {
        this.failedLoginAttemptRepository = failedLoginAttemptRepository;
        this.unathorizedAttemptRepository = unathorizedAttemptRepository;
        this.eventPublisher = eventPublisher;
    }

    public void handleFailedLoginAttempt(String email) {
        FailedLoginAttempt attempt = FailedLoginAttempt.create(getClientIp(), email);
        failedLoginAttemptRepository.save(attempt);
        LOGGER.warn("Failed login detected for email: {}", attempt.getIpAddress());
        eventPublisher.publishEvent(new FailedLoginAttemptEvent(this, attempt));
    }

    public void handleUnauthorizedAttempt(Long profileId) {
        UnauthorizedAttempt attempt = UnauthorizedAttempt.create(getClientIp(), profileId);
        unathorizedAttemptRepository.save(attempt);
        LOGGER.warn("Unauthorized attempt detected: {}", attempt.getIpAddress());
        eventPublisher.publishEvent(new UnauthorizedAttemptEvent(this, attempt));
    }

    public String getClientIp() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
            return extractClientIp(request);
        }
        return "UNKNOWN";
    }

    private String extractClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr();
        }
        return clientIp;
    }

}
