package com.secs.security;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditLogger {

    public static void logEvent(String eventType, String userId, String description) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String message = String.format("[%s] [%s] User: %s - %s",
                timestamp, eventType, userId, description);
        System.out.println("📝 " + message);
    }

    public static void logSecurityEvent(String userId, String action, boolean success) {
        String status = success ? "SUCCESS" : "FAILED";
        logEvent("SECURITY", userId, action + " - " + status);
    }

    public static void logSystemEvent(String component, String event) {
        logEvent("SYSTEM", "SYSTEM", component + ": " + event);
    }

    public static void logError(String userId, String operation, String error) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.err.println("❌ [" + timestamp + "] [ERROR] User: " + userId +
                ", Operation: " + operation + " - " + error);
    }
}