package com.secs.security;

import com.secs.shared.User;
import java.util.HashMap;
import java.util.Map;

public class ExamSecurityManager {
    private static ExamSecurityManager instance;
    private String adminOverridePassword = "SECS@ADMIN123";
    private Map<String, Integer> loginAttempts = new HashMap<>();
    private Map<String, Long> lockedAccounts = new HashMap<>();
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final long LOCK_DURATION = 15 * 60 * 1000;

    private ExamSecurityManager() {}

    public static ExamSecurityManager getInstance() {
        if (instance == null) {
            instance = new ExamSecurityManager();
        }
        return instance;
    }

    public boolean canLogout(User user, String enteredPassword) {
        if (user == null) return false;
        if (user.isAdmin()) return true;
        return enteredPassword != null && enteredPassword.equals(adminOverridePassword);
    }

    public boolean canPerformAction(User user, String action) {
        if (user == null || !user.isActive()) return false;
        if (user.isAdmin()) return true;
        return user.hasPermission(action);
    }

    public boolean validatePassword(String password) {
        if (password == null || password.length() < 8) return false;

        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        String specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?";

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
            if (specialChars.indexOf(c) >= 0) hasSpecial = true;
        }

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    public String generateSecurePassword() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*";

        StringBuilder password = new StringBuilder();
        password.append(upper.charAt((int)(Math.random() * upper.length())));
        password.append(lower.charAt((int)(Math.random() * lower.length())));
        password.append(digits.charAt((int)(Math.random() * digits.length())));
        password.append(special.charAt((int)(Math.random() * special.length())));

        for (int i = 0; i < 4; i++) {
            String all = upper + lower + digits + special;
            password.append(all.charAt((int)(Math.random() * all.length())));
        }

        return password.toString();
    }

    public boolean canAttemptLogin(String username) {
        String key = username.toLowerCase();
        if (lockedAccounts.containsKey(key)) {
            long lockTime = lockedAccounts.get(key);
            if (System.currentTimeMillis() - lockTime < LOCK_DURATION) {
                return false;
            } else {
                lockedAccounts.remove(key);
                loginAttempts.remove(key);
            }
        }
        return true;
    }

    public void recordLoginAttempt(String username, boolean success) {
        String key = username.toLowerCase();
        if (success) {
            loginAttempts.remove(key);
            lockedAccounts.remove(key);
        } else {
            int attempts = loginAttempts.getOrDefault(key, 0) + 1;
            loginAttempts.put(key, attempts);
            if (attempts >= MAX_LOGIN_ATTEMPTS) {
                lockedAccounts.put(key, System.currentTimeMillis());
                System.out.println("Account locked: " + username);
            }
        }
    }

    public void setAdminOverridePassword(String newPassword, User requester) {
        if (requester != null && requester.isAdmin() && validatePassword(newPassword)) {
            this.adminOverridePassword = encryptPassword(newPassword);
            System.out.println("Admin override password changed by: " + requester.getUsername());
        }
    }

    private String encryptPassword(String password) {
        // Simple encryption - use BCrypt in production
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return password;
        }
    }

    public String getAdminOverridePassword() {
        return adminOverridePassword;
    }

    public void resetLoginAttempts(String username) {
        String key = username.toLowerCase();
        loginAttempts.remove(key);
        lockedAccounts.remove(key);
    }

    public boolean isAccountLocked(String username) {
        String key = username.toLowerCase();
        if (lockedAccounts.containsKey(key)) {
            long lockTime = lockedAccounts.get(key);
            return System.currentTimeMillis() - lockTime < LOCK_DURATION;
        }
        return false;
    }
}