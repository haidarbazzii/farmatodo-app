package com.farmatodo.challenge.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // Se ejecuta antes que cualquier controlador
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    @Value("${app.security.api-key-header}")
    private String headerName;

    @Value("${app.security.api-key-secret}")
    private String secret;

    // Endpoints públicos (Whitelist)
    private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
            "/ping",
            "/error",
            "/swagger-ui",
            "/v3/api-docs"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Permitir acceso a endpoints públicos sin validación
        if (PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Validación de API Key
        String requestApiKey = request.getHeader(headerName);
        if (requestApiKey == null || !requestApiKey.equals(secret)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Invalid or missing API Key\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
