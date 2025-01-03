package dev.codeclub.hillock.http.filter;

import dev.codeclub.hillock.annotations.NoAuth;
import dev.codeclub.hillock.database.model.User;
import dev.codeclub.hillock.database.service.BrutusService;
import dev.codeclub.hillock.database.service.UserService;
import dev.codeclub.hillock.security.ApiToken;
import dev.codeclub.hillock.security.TokenCrypter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;

public class AuthFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthFilter.class);
    private static final String X_BRUTUS_TOKEN = "X-Brutus-Token";

    private final UserService userService;
    private final BrutusService brutusService;
    private final TokenCrypter tokenCrypter;
    private final RequestMappingHandlerMapping handlerMapping;

    public AuthFilter(UserService userService, BrutusService brutusService, TokenCrypter tokenCrypter, RequestMappingHandlerMapping handlerMapping) {
        this.userService = userService;
        this.brutusService = brutusService;
        this.tokenCrypter = tokenCrypter;
        this.handlerMapping = handlerMapping;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = request.getHeader(X_BRUTUS_TOKEN);
        String clientIp = getClientIp(request);

        if (token == null) {
            handleLoginAttempt(clientIp, null, false);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "No token");
            return;
        }

        try {
            ApiToken apiToken = ApiToken.deserialize(tokenCrypter, token, userService);
            if (apiToken.isExpired()) {
                handleLoginAttempt(clientIp, apiToken.profileId, false);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
            } else {
                User user = userService.getUserById(apiToken.profileId).orElse(null);
                if (user == null) {
                    handleLoginAttempt(clientIp, apiToken.profileId, false);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                    return;
                } else if (user.getDisabled()) {
                    handleLoginAttempt(clientIp, apiToken.profileId, false);
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Account banned");
                    return;
                }
                request.setAttribute("user", user);
                handleLoginAttempt(clientIp, apiToken.profileId, true);
            }
        } catch (Exception e) {
            handleLoginAttempt(clientIp, null, false);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        HandlerMethod method = null;
        try {
            HandlerExecutionChain handlerExecutionChain = handlerMapping.getHandler(request);
            if(handlerExecutionChain == null){
                LOGGER.info("HandlerExecutionChain is null");
                return true;
            }
            method = (HandlerMethod) handlerExecutionChain.getHandler();
            return method.getMethod().isAnnotationPresent(NoAuth.class) || method.getBeanType().isAnnotationPresent(NoAuth.class);
        } catch (Exception e) {
            LOGGER.error("Error while checking if filter should be applied, returning false", e);
            return  false;
        }
    }

    private void handleLoginAttempt(String clientIp, Long profileId, boolean successful) {
        LOGGER.info("Login attempt from IP: {} for profileId: {}, successful: {}", clientIp, profileId, successful);
        if (!successful) {
            brutusService.handleUnauthorizedAttempt(profileId);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        return clientIp != null ? clientIp.split(",")[0].trim() : request.getRemoteAddr();
    }
}