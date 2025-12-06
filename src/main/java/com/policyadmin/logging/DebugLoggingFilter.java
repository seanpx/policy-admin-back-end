package com.policyadmin.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class DebugLoggingFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Debug-Log";

    @Value("${logging.debug.always-on:false}")
    private boolean debugAlwaysOn;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        boolean enabled = debugAlwaysOn
                || "true".equalsIgnoreCase(request.getHeader(HEADER))
                || "true".equalsIgnoreCase(request.getParameter("debugLog"));
        if (enabled) {
            MDC.put("debugEnabled", "true");
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (enabled) {
                MDC.remove("debugEnabled");
            }
        }
    }
}
