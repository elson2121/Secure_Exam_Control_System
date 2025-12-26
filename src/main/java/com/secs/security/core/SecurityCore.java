package com.secs.security.core;

import com.secs.shared.User;
import com.secs.client.ClientContext;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

public class SecurityCore {
    private static SecurityCore instance;
    private static final String APP_SIGNATURE = "SECS_v1.0_KueiPochKuei_DillaUniversity";

    // Developer Info
    private static final String DEVELOPER_NAME = "Kuei Poch Kuei";
    private static final String UNIVERSITY = "Dilla University";
    private static final String STUDENT_ID = "CS/0032/14";
    private static final String YEAR = "3rd Year Computer Science";
    private static final String CONTACT_EMAIL = "kueiyiee@gmail.com";
    private static final String GITHUB = "github.com/kueiyiee";
    private static final String LINKEDIN = "linkedin.com/in/kueiyieeyt";
    private static final String WEBSITE = "kueiyiee.tech";

    // Security
    private String machineFingerprint;
    private String adminOverridePassword = "SECS@ADMIN123";
    private Map<String, Integer> loginAttempts = new HashMap<>();
    private Map<String, Long> lockedAccounts = new HashMap<>();
    private List<SecurityEvent> securityEvents = new ArrayList<>();
    private FileWriter forensicLog;

    // Logger - Using Java's built-in logging (NO SLF4J!)
    private static final Logger logger = Logger.getLogger(SecurityCore.class.getName());
    private static final Logger forensicLogger = Logger.getLogger("FORENSIC");

    private SecurityCore() {
        initializeSecurityCore();
    }

    public static SecurityCore getInstance() {
        if (instance == null) {
            synchronized (SecurityCore.class) {
                if (instance == null) {
                    instance = new SecurityCore();
                }
            }
        }
        return instance;
    }

    private void initializeSecurityCore() {
        try {
            printDeveloperBanner();

            // Generate machine fingerprint
            machineFingerprint = generateMachineFingerprint();

            // Configure logging
            setupLogging();

            // Initialize forensic logging
            initForensicLogging();

            logger.info("Security Core initialized successfully");
            logSecurityEvent("SECURITY_CORE_INIT", "Security Core started", "HIGH");

        } catch (Exception e) {
            System.err.println("CRITICAL: Security Core initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupLogging() {
        // Set logging format
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$-7s [%2$s] %5$s%6$s%n");
    }

    private void printDeveloperBanner() {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║          SECS SECURITY CORE v1.0                   ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║  Developer: " + DEVELOPER_NAME + "                    ");
        System.out.println("║  Institution: " + UNIVERSITY + " - " + YEAR + "       ");
        System.out.println("║  Student ID: " + STUDENT_ID + "                       ");
        System.out.println("║  Contact: " + CONTACT_EMAIL + "                       ");
        System.out.println("║  GitHub: " + GITHUB + "                               ");
        System.out.println("║  Website: " + WEBSITE + "                             ");
        System.out.println("╚══════════════════════════════════════════════════════╝\n");
    }

    private String generateMachineFingerprint() {
        try {
            String os = System.getProperty("os.name") + " " + System.getProperty("os.version");
            String arch = System.getProperty("os.arch");
            String user = System.getProperty("user.name");
            String mac = getMacAddress();

            String raw = os + arch + user + mac + APP_SIGNATURE;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.getBytes());

            return bytesToHex(hash);
        } catch (Exception e) {
            logger.warning("Failed to generate machine fingerprint: " + e.getMessage());
            return "UNKNOWN_" + UUID.randomUUID().toString().substring(0, 20);
        }
    }

    private String getMacAddress() {
        try {
            NetworkInterface network = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            if (network != null) {
                byte[] mac = network.getHardwareAddress();
                if (mac != null) {
                    StringBuilder sb = new StringBuilder();
                    for (byte b : mac) {
                        sb.append(String.format("%02X:", b));
                    }
                    return sb.toString().substring(0, sb.length() - 1);
                }
            }
        } catch (Exception e) {
            logger.warning("Failed to get MAC address: " + e.getMessage());
        }
        return "00:00:00:00:00:00";
    }

    private void initForensicLogging() {
        try {
            File logDir = new File("forensic_logs");
            if (!logDir.exists()) {
                boolean created = logDir.mkdirs();
                if (!created) {
                    logger.severe("Failed to create forensic logs directory");
                }
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File logFile = new File(logDir, "secs_forensic_" + timestamp + ".log");
            forensicLog = new FileWriter(logFile, true);

            writeForensicHeader();
            logger.info("Forensic logging initialized at: " + logFile.getAbsolutePath());

        } catch (IOException e) {
            logger.severe("Failed to initialize forensic logging: " + e.getMessage());
        }
    }

    private void writeForensicHeader() throws IOException {
        if (forensicLog != null) {
            forensicLog.write("╔══════════════════════════════════════════════════════╗\n");
            forensicLog.write("║              SECS FORENSIC LOG FILE                 ║\n");
            forensicLog.write("╠══════════════════════════════════════════════════════╣\n");
            forensicLog.write("║  Application: Secure Exam Control System (SECS)     ║\n");
            forensicLog.write("║  Version: 1.0 Enterprise Edition                    ║\n");
            forensicLog.write("║  Developer: " + DEVELOPER_NAME + "\n");
            forensicLog.write("║  University: " + UNIVERSITY + "\n");
            forensicLog.write("║  Student ID: " + STUDENT_ID + "\n");
            forensicLog.write("║  Machine ID: " + (machineFingerprint != null ?
                    machineFingerprint.substring(0, 16) + "..." : "UNKNOWN") + "\n");
            forensicLog.write("║  Start Time: " + LocalDateTime.now() + "\n");
            forensicLog.write("║  Security Level: MAXIMUM                            ║\n");
            forensicLog.write("╚══════════════════════════════════════════════════════╝\n\n");
            forensicLog.flush();
        }
    }

    public void logSecurityEvent(String eventType, String description, String severity) {
        User currentUser = null;
        try {
            // Try to get current user, but handle if ClientContext doesn't exist
            currentUser = ClientContext.getInstance() != null ?
                    ClientContext.getInstance().getCurrentUser() : null;
        } catch (Exception e) {
            // ClientContext might not be initialized yet
            logger.warning("ClientContext not available: " + e.getMessage());
        }

        String userId = currentUser != null ? currentUser.getUsername() : "SYSTEM";

        SecurityEvent event = new SecurityEvent(
                UUID.randomUUID().toString().substring(0, 8),
                eventType,
                description,
                severity,
                LocalDateTime.now(),
                currentUser
        );

        securityEvents.add(event);

        // Log with appropriate level
        switch (severity) {
            case "CRITICAL":
                logger.severe("[SECURITY] " + eventType + " - " + description + " (User: " + userId + ")");
                break;
            case "HIGH":
                logger.warning("[SECURITY] " + eventType + " - " + description + " (User: " + userId + ")");
                break;
            case "MEDIUM":
                logger.info("[SECURITY] " + eventType + " - " + description + " (User: " + userId + ")");
                break;
            default:
                logger.fine("[SECURITY] " + eventType + " - " + description + " (User: " + userId + ")");
        }

        // Write to forensic file
        try {
            if (forensicLog != null) {
                forensicLog.write(event.toLogEntry() + "\n");
                forensicLog.flush();
            }
        } catch (IOException e) {
            logger.severe("Failed to write forensic log: " + e.getMessage());
        }

        // Console output for critical events
        if ("CRITICAL".equals(severity) || "HIGH".equals(severity)) {
            System.out.println("🔴 SECURITY EVENT: " + eventType + " - " + description);
        }
    }

    public boolean canLogout(User user, String enteredPassword) {
        if (user == null) {
            logger.fine("Logout allowed: null user");
            return true;
        }
        if (user.isAdmin()) {
            logger.fine("Logout allowed: admin user");
            return true;
        }

        boolean allowed = enteredPassword != null && enteredPassword.equals(adminOverridePassword);
        if (!allowed) {
            logger.warning("Logout denied for user: " + user.getUsername());
        } else {
            logger.fine("Logout allowed with override password for user: " + user.getUsername());
        }
        return allowed;
    }

    public boolean canPerformAction(User user, String action) {
        if (user == null) {
            logger.warning("Action denied: null user for action: " + action);
            return false;
        }
        if (user.isAdmin()) {
            logger.fine("Action allowed: admin user for action: " + action);
            return true;
        }

        boolean hasPermission = user.hasPermission(action);
        if (!hasPermission) {
            logger.warning("Action denied: user " + user.getUsername() +
                    " lacks permission for: " + action);
        } else {
            logger.fine("Action allowed: user " + user.getUsername() +
                    " has permission for: " + action);
        }
        return hasPermission;
    }

    public boolean canAttemptLogin(String username) {
        if (username == null || username.trim().isEmpty()) {
            logger.warning("Login attempt with empty username");
            return false;
        }

        String key = username.toLowerCase().trim();
        if (lockedAccounts.containsKey(key)) {
            long lockTime = lockedAccounts.get(key);
            long currentTime = System.currentTimeMillis();

            if (currentTime - lockTime < 15 * 60 * 1000) { // 15 minutes
                logger.warning("Login blocked: account locked - " + username);
                return false;
            } else {
                // Lock expired
                lockedAccounts.remove(key);
                loginAttempts.remove(key);
                logger.info("Account lock expired: " + username);
            }
        }
        return true;
    }

    public void recordLoginAttempt(String username, boolean success) {
        if (username == null || username.trim().isEmpty()) {
            logger.warning("Invalid username for login attempt recording");
            return;
        }

        String key = username.toLowerCase().trim();
        if (success) {
            loginAttempts.remove(key);
            lockedAccounts.remove(key);
            logger.info("Successful login recorded: " + username);
        } else {
            int attempts = loginAttempts.getOrDefault(key, 0) + 1;
            loginAttempts.put(key, attempts);

            logger.warning("Failed login attempt " + attempts + " for user: " + username);

            if (attempts >= 3) {
                lockedAccounts.put(key, System.currentTimeMillis());
                logger.severe("Account locked due to maximum failed attempts: " + username);
                logSecurityEvent("ACCOUNT_LOCKED",
                        "Account locked due to multiple failed attempts: " + username, "HIGH");
            }
        }
    }

    public boolean validatePassword(String password) {
        if (password == null || password.length() < 8) {
            logger.warning("Password validation failed: too short or null");
            return false;
        }

        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        String specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?";

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
            if (specialChars.indexOf(c) >= 0) hasSpecial = true;
        }

        boolean isValid = hasUpper && hasLower && hasDigit && hasSpecial;
        if (!isValid) {
            logger.warning("Password validation failed: missing required character types");
        } else {
            logger.fine("Password validation passed");
        }
        return isValid;
    }

    public String getMachineFingerprint() {
        return machineFingerprint;
    }

    public String getAdminOverridePassword() {
        return adminOverridePassword;
    }

    public void setAdminOverridePassword(String newPassword, User requester) {
        if (requester != null && requester.isAdmin()) {
            if (validatePassword(newPassword)) {
                this.adminOverridePassword = newPassword;
                logger.info("Admin override password changed by: " + requester.getUsername());
                logSecurityEvent("OVERRIDE_PASSWORD_CHANGED",
                        "Admin override password changed by: " + requester.getUsername(), "HIGH");
            } else {
                logger.warning("Invalid new password format for admin override");
            }
        } else {
            logger.warning("Unauthorized attempt to change admin override password");
        }
    }

    public void resetLoginAttempts(String username) {
        if (username != null) {
            String key = username.toLowerCase().trim();
            loginAttempts.remove(key);
            lockedAccounts.remove(key);
            logger.info("Login attempts reset for user: " + username);
        }
    }

    public boolean isAccountLocked(String username) {
        if (username == null) return false;
        String key = username.toLowerCase().trim();

        if (lockedAccounts.containsKey(key)) {
            long lockTime = lockedAccounts.get(key);
            return System.currentTimeMillis() - lockTime < 15 * 60 * 1000;
        }
        return false;
    }

    public int getRemainingAttempts(String username) {
        if (username == null) return 0;
        String key = username.toLowerCase().trim();
        int attempts = loginAttempts.getOrDefault(key, 0);
        return Math.max(0, 3 - attempts);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    public void printSecurityReport() {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║               SECURITY STATUS REPORT                ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║  Machine ID: " +
                (machineFingerprint != null ? machineFingerprint.substring(0, 16) + "..." : "UNKNOWN"));
        System.out.println("║  Security Events: " + securityEvents.size() + " logged");
        System.out.println("║  Locked Accounts: " + lockedAccounts.size());
        System.out.println("║  Developer: " + DEVELOPER_NAME);
        System.out.println("║  University: " + UNIVERSITY);
        System.out.println("╚══════════════════════════════════════════════════════╝");

        logger.info("Security report generated: " + securityEvents.size() + " events, " +
                lockedAccounts.size() + " locked accounts");
    }

    public void shutdown() {
        try {
            logger.info("Security Core shutting down");
            logSecurityEvent("SHUTDOWN", "Security Core shutting down", "MEDIUM");

            if (forensicLog != null) {
                forensicLog.write("\n╔══════════════════════════════════════════════════════╗\n");
                forensicLog.write("║                 END OF FORENSIC LOG                 ║\n");
                forensicLog.write("║                 " + LocalDateTime.now() + "                ║\n");
                forensicLog.write("╚══════════════════════════════════════════════════════╝\n");
                forensicLog.close();
            }

            System.out.println("Security Core shutdown completed");

        } catch (IOException e) {
            logger.severe("Error during security core shutdown: " + e.getMessage());
        }
    }

    // SecurityEvent inner class
    public static class SecurityEvent {
        private String id;
        private String eventType;
        private String description;
        private String severity;
        private LocalDateTime timestamp;
        private User user;

        public SecurityEvent(String id, String eventType, String description,
                             String severity, LocalDateTime timestamp, User user) {
            this.id = id;
            this.eventType = eventType;
            this.description = description;
            this.severity = severity;
            this.timestamp = timestamp;
            this.user = user;
        }

        public String toLogEntry() {
            String username = user != null ? user.getUsername() : "SYSTEM";
            return String.format("[%s] [%s] [User: %s] - %s",
                    timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    severity,
                    username,
                    description);
        }

        // Getters
        public String getId() { return id; }
        public String getEventType() { return eventType; }
        public String getDescription() { return description; }
        public String getSeverity() { return severity; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public User getUser() { return user; }
    }

    // Additional helper methods
    public List<SecurityEvent> getSecurityEvents() {
        return new ArrayList<>(securityEvents);
    }

    public int getFailedLoginAttempts(String username) {
        if (username == null) return 0;
        return loginAttempts.getOrDefault(username.toLowerCase().trim(), 0);
    }

    public String generateSecureToken() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String input = UUID.randomUUID().toString() + System.currentTimeMillis() + APP_SIGNATURE;
            byte[] hash = md.digest(input.getBytes());
            return bytesToHex(hash).substring(0, 32);
        } catch (Exception e) {
            logger.warning("Failed to generate secure token: " + e.getMessage());
            return UUID.randomUUID().toString().replace("-", "");
        }
    }
}