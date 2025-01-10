package dev.codeclub.hillock.config;

import dev.codeclub.hillock.http.filter.CorsFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
@Profile("!excludeConfig")
public class FilterConfig {

    @Qualifier("requestMappingHandlerMapping")
    private final RequestMappingHandlerMapping handlerMapping;

    @Value("${cors.allowed-origin:*}")
    private String allowedOrigin;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${cors.allowed-headers:Origin, X-Requested-With, Content-Type, Accept, Authorization}")
    private String allowedHeaders;

    @Value("${cors.allow-credentials:true}")
    private String allowCredentials;

    @Value("${cors.amax-age:3600}")
    private String maxAge;

    public FilterConfig(RequestMappingHandlerMapping handlerMapping) {
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
}