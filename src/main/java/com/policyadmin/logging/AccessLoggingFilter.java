package com.policyadmin.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class AccessLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AccessLoggingFilter.class);

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        int status = 500;
        String userId = resolveUser();
        if (userId != null) {
            MDC.put("userId", userId);
        }
        try {
            filterChain.doFilter(request, response);
            status = response.getStatus();
        } catch (Exception ex) {
            status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            throw ex;
        } finally {
            long durationMs = System.currentTimeMillis() - start;
            log.info("access",
                    StructuredArguments.keyValue("method", request.getMethod()),
                    StructuredArguments.keyValue("path", request.getRequestURI()),
                    StructuredArguments.keyValue("status", status),
                    StructuredArguments.keyValue("durationMs", durationMs),
                    StructuredArguments.keyValue("remoteIp", request.getRemoteAddr()),
                    StructuredArguments.keyValue("userId", userId),
                    StructuredArguments.keyValue("correlationId", MDC.get(CorrelationIdFilter.CORRELATION_ID_KEY))
            );
            if (userId != null) {
                MDC.remove("userId");
            }
        }
    }

    private String resolveUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal != null && !"anonymousUser".equals(principal)) {
                return authentication.getName();
            }
        }
        return null;
    }
}
