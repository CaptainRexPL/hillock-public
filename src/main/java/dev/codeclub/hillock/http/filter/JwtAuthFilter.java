package dev.codeclub.hillock.http.filter;

import dev.codeclub.hillock.annotations.NoAuth;
import dev.codeclub.hillock.database.service.BrutusService;
import dev.codeclub.hillock.http.service.JwtService;
import dev.codeclub.hillock.security.CustomUserDetails;
import dev.codeclub.hillock.security.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final String AUTHORIZATION_TOKEN = "Authorization";

    private final List<String> excludedUrlPatterns;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final BrutusService brutusService;
    private final RequestMappingHandlerMapping handlerMapping;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthFilter(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService,
            BrutusService brutusService,
            RequestMappingHandlerMapping handlerMapping,
            List<String> excludedUrlPatterns
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.brutusService = brutusService;
        this.handlerMapping = handlerMapping;
        this.excludedUrlPatterns = excludedUrlPatterns;
    }

    private boolean isExcludedRequest(HttpServletRequest request){
        String requestURI = request.getRequestURI();
        boolean result = false;
        LOGGER.info("Searching matching path, request url: {}, paths: {}", requestURI, String.join(", ", this.excludedUrlPatterns));
        for (String path : this.excludedUrlPatterns) {
            if (pathMatcher.match(path, requestURI)) {
                result = true;
                break;
            }
        }
        return result;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;
        String clientIp = getClientIp(request);

        if (authHeader == null) {
            handleLoginAttempt(clientIp, null, false);
            throw new InsufficientAuthenticationException("No Authorization header found");
        }

        if (authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            username = jwtService.extractUsername(token);
        } else {
            handleLoginAttempt(clientIp, null, false);
            throw new InsufficientAuthenticationException("Invalid Authorization header");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            CustomUserDetails userDetails = (CustomUserDetails)userDetailsService.loadUserByUsername(username);
            if (userDetails == null || userDetails.getUser() == null) {
                LOGGER.warn("User not found: {}", username);
                handleLoginAttempt(clientIp, null, false);
                throw new BadCredentialsException("User from token not found");
            }
            if (userDetails.getUser().getDisabled()) {
                LOGGER.warn("User is disabled: {}", username);
                handleLoginAttempt(clientIp, userDetails.getUser().getId(), false);
                throw new BadCredentialsException("User from token is disabled");
            }
            if (jwtService.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                String message = "Token expired or invalid";
                handleLoginAttempt(clientIp, userDetails.getUser().getId(), false);
                throw new CredentialsExpiredException(message);
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        //return isExcludedRequest(request) || isNoAuthAnnotated(request, handlerMapping);
        //TODO: This is a temporary solution, hopefully there is a better way to handle this
        if (isExcludedRequest(request) || isNoAuthAnnotated(request, handlerMapping)) {
            LOGGER.info("Request without filter for: {}", getClientIp(request));
            return true;
        }
        return false;
    }

    private boolean isNoAuthAnnotated(HttpServletRequest request, RequestMappingHandlerMapping handlerMapping) {
        try {
            HandlerExecutionChain handlerExecutionChain = handlerMapping.getHandler(request);
            if(handlerExecutionChain == null){
                LOGGER.info("HandlerExecutionChain is null");
                return true;
            }
            HandlerMethod method = (HandlerMethod) handlerExecutionChain.getHandler();
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

    private static String getClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        return clientIp != null ? clientIp.split(",")[0].trim() : request.getRemoteAddr();
    }

    private static String failureInfoAsJson(String message) {
        return "{\"message\":\"" + message + "\"}";
    }
}
