package dev.codeclub.hillock.listeners;

import dev.codeclub.hillock.database.service.BrutusService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationFailureDisabledEvent;
import org.springframework.security.authentication.event.AuthenticationFailureLockedEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import dev.codeclub.hillock.database.service.UserService;
import dev.codeclub.hillock.database.model.User;

@Component
public class AuthenticationEventListener implements ApplicationListener<ApplicationEvent> {

    private static final Logger LOGGER = LogManager.getLogger(AuthenticationEventListener.class);
    private final UserService userService;
    private final BrutusService brutusService;

    public AuthenticationEventListener(UserService userService, BrutusService brutusService) {
        this.userService = userService;
        this.brutusService = brutusService;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof AuthenticationSuccessEvent successEvent) {
            String email = successEvent.getAuthentication().getName();
            String ipAddress = brutusService.getClientIp();
            User user = userService.getUserByEmail(email).orElse(null);
            if (user != null) {
                user.setNewLogin(ipAddress);
                userService.updateUser(user.getId(), user);
            }
        } else if (event instanceof AuthenticationFailureBadCredentialsEvent failureEvent) {
            handleAuthenticationFailure(failureEvent.getAuthentication().getName());
        } else if (event instanceof AuthenticationFailureLockedEvent failureEvent) {
            handleAuthenticationFailure(failureEvent.getAuthentication().getName());
        } else if (event instanceof AuthenticationFailureDisabledEvent failureEvent) {
            handleAuthenticationFailure(failureEvent.getAuthentication().getName());
        }
    }

    private void handleAuthenticationFailure(String email) {
        String ipAddress = brutusService.getClientIp();
        brutusService.handleFailedLoginAttempt(email == null || email.isBlank() ? null : email);
        User user = userService.getUserByEmail(email).orElse(null);
        if (user != null) {
            user.setNewFailedLogin(ipAddress);
            userService.updateUser(user.getId(), user);
        }
    }
}
