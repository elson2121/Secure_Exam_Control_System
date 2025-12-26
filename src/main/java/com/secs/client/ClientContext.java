package com.secs.client;

import com.secs.security.ExamSecurityManager;
import com.secs.security.core.SecurityCore;
import com.secs.shared.User;
import javafx.stage.Stage;

public class ClientContext {
    private static ClientContext instance;
    private User currentUser;
    private Stage primaryStage;
    private RMIClient rmiClient;
    private boolean loggedIn = false;
    private ExamSecurityManager securityManager;
    private SecurityCore securityCore;
    private String sessionId;
    private long loginTime;

    private ClientContext() {
        try {
            System.out.println("Initializing Client Context...");

            securityManager = ExamSecurityManager.getInstance();
            securityCore = SecurityCore.getInstance();

            rmiClient = new RMIClient("localhost", 1099);
            System.out.println("✓ RMI Client connected");

            generateSessionId();
            System.out.println("✓ Session ID generated");

        } catch (Exception e) {
            System.err.println("✗ Failed to initialize: " + e.getMessage());
            rmiClient = null;
        }
    }

    public static ClientContext getInstance() {
        if (instance == null) {
            instance = new ClientContext();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        if (currentUser != null) {
            loggedIn = true;
            loginTime = System.currentTimeMillis();
            System.out.println("✓ User logged in: " + currentUser.getName());
        } else {
            loggedIn = false;
            loginTime = 0;
            System.out.println("✓ User logged out");
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public RMIClient getRmiClient() {
        return rmiClient;
    }

    public boolean isConnected() {
        return rmiClient != null && rmiClient.isConnected();
    }

    public boolean isLoggedIn() {
        return loggedIn && currentUser != null;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public ExamSecurityManager getSecurityManager() {
        return securityManager;
    }

    public SecurityCore getSecurityCore() {
        return securityCore;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getFormattedSessionDuration() {
        if (!isLoggedIn() || loginTime == 0) return "Not logged in";
        long seconds = (System.currentTimeMillis() - loginTime) / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    public boolean canLogout(String overridePassword) {
        if (!isLoggedIn()) return true;
        return securityManager.canLogout(currentUser, overridePassword);
    }

    public void logout() {
        if (currentUser != null) {
            System.out.println("Logging out user: " + currentUser.getUsername());
            System.out.println("Session duration: " + getFormattedSessionDuration());
            generateSessionId();
        }
        currentUser = null;
        loggedIn = false;
        loginTime = 0;
    }

    private void generateSessionId() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.valueOf((int)(Math.random() * 1000000));
        sessionId = "SESS-" + timestamp.substring(timestamp.length() - 6) + "-" + random;
    }
}