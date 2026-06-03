package com.hotelpms.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Bao ve /api/* bang header X-API-KEY. Admin UI (/admin/**) la public trong dev.
 */
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-API-KEY";

    private final String validKey;

    public ApiKeyAuthFilter(@Value("${app.api-key}") String validKey) {
        this.validKey = validKey;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getRequestURI().startsWith("/api/")) {
            String provided = request.getHeader(HEADER);
            if (validKey == null || !validKey.equals(provided)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"error\":\"Invalid API key\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
