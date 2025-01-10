package dev.codeclub.hillock.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.codeclub.hillock.database.service.BrutusService;
import dev.codeclub.hillock.http.filter.JwtAuthFilter;
import dev.codeclub.hillock.http.model.FailureInfo;
import dev.codeclub.hillock.http.service.JwtService;
import dev.codeclub.hillock.security.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final BrutusService brutusService;
    private final RequestMappingHandlerMapping handlerMapping;
    private final List<String> excludedUrlPatterns;



    public SecurityConfig(
            CustomUserDetailsService userDetailsService,
            @Value("${security.jwt.excluded-urls:/api/account/**,/swagger-ui/**,/v3/api-docs/**,/swagger-resources/**,/webjars/**}") String excludedUrls,
            JwtService jwtService,
            BrutusService brutusService,
            RequestMappingHandlerMapping handlerMapping
    ) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.brutusService = brutusService;
        this.handlerMapping = handlerMapping;
        this.excludedUrlPatterns = Arrays.asList(excludedUrls.split(","));
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new AccessDeniedHandler() {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
                FailureInfo failureInfo = new FailureInfo("You do not have permission to use this endpoint.");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(new ObjectMapper().writeValueAsString(failureInfo));
            }
        };
    }

    @Bean
    public AuthenticationEntryPoint jwtAuthenticationEntryPoint() {
        return (HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.AuthenticationException authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            FailureInfo failureInfo = new FailureInfo("Unauthorized: " + authException.getMessage());
            PrintWriter writer = response.getWriter();
            writer.write(new ObjectMapper().writeValueAsString(failureInfo));
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(excludedUrlPatterns.stream()
                                        .map(AntPathRequestMatcher::new).toArray(AntPathRequestMatcher[]::new)).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(new JwtAuthFilter(jwtService, userDetailsService, brutusService, handlerMapping, excludedUrlPatterns), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedHandler(accessDeniedHandler())
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint())
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());

        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(authenticationProvider)
                .build();
    }
}
