package com.ubb.events.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubb.events.OrganizationAuthorizationService;
import com.ubb.events.common.errors.ForbiddenException;
import com.ubb.events.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.List;
import java.util.Map;

public class OrganizationAuthorizationInterceptor implements HandlerInterceptor {
    private final OrganizationAuthorizationService authorizationService;
    private final ObjectMapper objectMapper;

    public OrganizationAuthorizationInterceptor(OrganizationAuthorizationService authorizationService,
                                                 ObjectMapper objectMapper) {
        this.authorizationService = authorizationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod method)) return true;
        RequireOrgPermission required = method.getMethodAnnotation(RequireOrgPermission.class);
        NoOrgCheck noCheck = method.getMethodAnnotation(NoOrgCheck.class);
        if ((required == null) == (noCheck == null)) {
            return forbidden(response, "Organization endpoint must declare exactly one authorization annotation");
        }
        if (noCheck != null) return true;

        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication() == null ? null : org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        Long userId = principal instanceof Number n ? n.longValue() : parseLong(principal);
        if (userId == null) return unauthorized(response);

        Map<String, String> variables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        Long organizationId = variables == null ? null : parseLong(variables.get("organizationId"));
        if (organizationId == null) return forbidden(response, "Organization access is forbidden");
        try {
            if (!authorizationService.hasAtLeast(userId, organizationId, required.value())) {
                return forbidden(response, "Organization access is forbidden");
            }
        } catch (ForbiddenException ignored) {
            return forbidden(response, "Organization access is forbidden");
        }
        return true;
    }

    private Long parseLong(Object value) {
        try { return value == null ? null : Long.valueOf(value.toString()); }
        catch (NumberFormatException ignored) { return null; }
    }

    private boolean unauthorized(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), new ErrorResponse(401, "Authentication required", List.of()));
        return false;
    }

    private boolean forbidden(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), new ErrorResponse(403, message, List.of()));
        return false;
    }
}
