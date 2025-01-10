package dev.codeclub.hillock.http.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Cache<String, Bucket> buckets;
    private final Integer requestsPerSecond;

    public RateLimitFilter(@Value("${http.limits.expiry_minutes:60}")Integer rateLimitExpiry,
                           @Value("${http.limits.max_sessions:100}")Integer rateLimitMaxSessions,
                           @Value("${http.limits.requests_per_second:50}")Integer requestsPerSecond) {
        this.buckets = CacheBuilder.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(rateLimitExpiry))
                .maximumSize(rateLimitMaxSessions)
                .build();
        this.requestsPerSecond = requestsPerSecond;
    }


    private Bucket resolveBucket(String clientId) {
        try {
            return buckets.get(clientId, () -> Bucket.builder()
                    .addLimit(Bandwidth.simple(requestsPerSecond, Duration.ofSeconds(1)))
                    .build());
        } catch (ExecutionException e) {
            Bucket defaultBucket = Bucket.builder()
                    .addLimit(Bandwidth.simple(requestsPerSecond, Duration.ofSeconds(1)))
                    .build();
            buckets.put(clientId, defaultBucket);
            return defaultBucket;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String clientId = request.getRemoteAddr();

        Bucket bucket = resolveBucket(clientId);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Rate limit exceeded");
        }
    }
}