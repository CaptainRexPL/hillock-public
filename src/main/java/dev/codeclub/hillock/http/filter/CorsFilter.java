package dev.codeclub.hillock.http.filter;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class CorsFilter extends OncePerRequestFilter {

    private final String allowedOrigin;
    private final String allowedMethods;
    private final String allowedHeaders;
    private final String allowCredentials;
    private final String maxAge;

    public CorsFilter(String allowedOrigin, String allowedMethods, String allowedHeaders, String allowCredentials, String maxAge) {
        this.allowedOrigin = allowedOrigin;
        this.allowedMethods = allowedMethods;
        this.allowedHeaders = allowedHeaders;
        this.allowCredentials = allowCredentials;
        this.maxAge = maxAge;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", allowedOrigin);
        response.setHeader("Access-Control-Allow-Methods", allowedMethods);
        response.setHeader("Access-Control-Allow-Headers", allowedHeaders);
        response.setHeader("Access-Control-Allow-Credentials", allowCredentials);
        response.setHeader("Access-Control-Max-Age", maxAge);

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_OK);
        } else {
            filterChain.doFilter(request, response);
        }
    }
}