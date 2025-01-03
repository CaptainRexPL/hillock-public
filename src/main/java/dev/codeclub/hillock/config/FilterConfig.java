package dev.codeclub.hillock.config;

import dev.codeclub.hillock.database.service.BrutusService;
import dev.codeclub.hillock.database.service.UserService;
import dev.codeclub.hillock.http.filter.AuthFilter;
import dev.codeclub.hillock.http.filter.CorsFilter;
import dev.codeclub.hillock.security.TokenCrypter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
public class FilterConfig {

    private final UserService userService;
    private final TokenCrypter tokenCrypter;
    private final BrutusService brutusService;
    @Qualifier("requestMappingHandlerMapping")
    private final RequestMappingHandlerMapping handlerMapping;

    @Value("${cors.allowed-origin:*}")
    private String allowedOrigin;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${cors.allowed-headers:Origin, X-Requested-With, Content-Type, Accept, X-Brutus-Token}")
    private String allowedHeaders;

    @Value("${cors.allow-credentials:true}")
    private String allowCredentials;

    @Value("${cors.amax-age:3600}")
    private String maxAge;

    public FilterConfig(UserService userService, TokenCrypter tokenCrypter, BrutusService brutusService, RequestMappingHandlerMapping handlerMapping) {
        this.userService = userService;
        this.tokenCrypter = tokenCrypter;
        this.brutusService = brutusService;
        this.handlerMapping = handlerMapping;
    }

    @Bean
    @ConditionalOnProperty(name = "cors.enabled", havingValue = "true", matchIfMissing = false)
    public FilterRegistrationBean<CorsFilter> customCorsFilter() {
        FilterRegistrationBean<CorsFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CorsFilter(allowedOrigin, allowedMethods, allowedHeaders, allowCredentials, maxAge));
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(Integer.MIN_VALUE);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<AuthFilter> authFilter() {
        FilterRegistrationBean<AuthFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new AuthFilter(userService, brutusService, tokenCrypter, handlerMapping));

        registrationBean.addUrlPatterns("/api/*");

        return registrationBean;
    }
}