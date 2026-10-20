package com.ubb.events.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubb.events.OrganizationAuthorizationService;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnBean(OrganizationAuthorizationService.class)
public class OrganizationAuthorizationWebConfig implements WebMvcConfigurer {
    private final OrganizationAuthorizationService authorizationService;
    private final ObjectMapper objectMapper;

    public OrganizationAuthorizationWebConfig(OrganizationAuthorizationService authorizationService, ObjectMapper objectMapper) {
        this.authorizationService = authorizationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new OrganizationAuthorizationInterceptor(authorizationService, objectMapper))
                .addPathPatterns("/api/organizations/{organizationId}/**");
    }
}
