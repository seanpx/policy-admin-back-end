package com.policyadmin.logging;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.MDC;

public final class SafeLogging {

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password",
            "secret",
            "token",
            "authorization",
            "auth",
            "ssn",
            "dob",
            "dateofbirth",
            "surname",
            "givenname",
            "givname",
            "idnumber",
            "pan",
            "cvv"
    );

    private static final int DEFAULT_TRUNCATE_LENGTH = 512;

    private SafeLogging() {
    }

    public static Map<String, Object> sanitize(Map<String, ?> payload) {
        return sanitize(payload, DEFAULT_TRUNCATE_LENGTH);
    }

    public static Map<String, Object> sanitize(Map<String, ?> payload, int maxLength) {
        if (payload == null || payload.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> sanitized = new LinkedHashMap<>();
        payload.forEach((key, value) -> {
            if (value == null) {
                return;
            }
            if (isSensitiveKey(key)) {
                sanitized.put(key, "***");
            } else {
                sanitized.put(key, truncate(String.valueOf(value), maxLength));
            }
        });
        return sanitized;
    }

    public static boolean isDebugEnabled() {
        String debugFlag = MDC.get("debugEnabled");
        return "true".equalsIgnoreCase(debugFlag);
    }

    public static void debug(Logger log, String message, Object... arguments) {
        if (log.isDebugEnabled() && isDebugEnabled()) {
            log.debug(message, arguments);
        }
    }

    private static boolean isSensitiveKey(String key) {
        if (key == null) {
            return false;
        }
        return SENSITIVE_KEYS.contains(key.toLowerCase(Locale.ROOT));
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...(truncated)";
    }
}
