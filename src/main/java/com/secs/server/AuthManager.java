package com.secs.server;

import com.secs.shared.User;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthManager {
    private Map<String, User> users = new HashMap<>();
    private Map<String, String> sessions = new HashMap<>();

    public AuthManager() {
        initializeUsers();
    }

    private void initializeUsers() {
        // Student users
        users.put("student1", new User("S001", "student1", "pass123", "John Doe", "student"));
        users.put("student2", new User("S002", "student2", "pass123", "Jane Smith", "student"));
        users.put("student3", new User("S003", "student3", "pass123", "Bob Johnson", "student"));

        // Teacher users
        users.put("teacher1", new User("T001", "teacher1", "admin123", "Dr. Smith", "teacher"));
        users.put("teacher2", new User("T002", "teacher2", "admin123", "Prof. Williams", "teacher"));
    }

    public User authenticate(String username, String password) {
        User user = users.get(username);
        if (user != null && user.getPassword().equals(password)) {
            System.out.println("Authentication successful for: " + username);
            return user;
        }
        System.out.println("Authentication failed for: " + username);
        return null;
    }

    public String createSession(String username) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, username);
        return token;
    }

    public boolean validateSession(String token) {
        return sessions.containsKey(token);
    }

    public String getUsernameFromToken(String token) {
        return sessions.get(token);
    }

    public void logout(String token) {
        sessions.remove(token);
    }

    public int getTotalUsers() {
        return users.size();
    }
}