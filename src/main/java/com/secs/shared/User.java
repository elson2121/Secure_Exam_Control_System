package com.secs.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private String username;
    private String password;
    private String name;
    private String role; // ADMIN, TEACHER, STUDENT
    private String id;
    private boolean active;
    private List<String> permissions;
    private String createdBy; // Who created this user
    private String creationDate;

    public User(String username, String password, String name, String role, String id) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
        this.id = id;
        this.active = true;
        this.permissions = getDefaultPermissions(role);
        this.createdBy = "SYSTEM";
        this.creationDate = java.time.LocalDate.now().toString();
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) {
        this.role = role;
        this.permissions = getDefaultPermissions(role);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getCreationDate() { return creationDate; }
    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }

    // Helper methods
    public boolean hasPermission(String permission) {
        return permissions.contains(permission) || permissions.contains("ALL");
    }

    public boolean isAdmin() { return "ADMIN".equals(role); }
    public boolean isTeacher() { return "TEACHER".equals(role); }
    public boolean isStudent() { return "STUDENT".equals(role); }

    private List<String> getDefaultPermissions(String role) {
        List<String> perms = new ArrayList<>();

        switch (role.toUpperCase()) {
            case "ADMIN":
                perms.add("ALL");
                perms.add("MANAGE_USERS");
                perms.add("MANAGE_EXAMS");
                perms.add("VIEW_REPORTS");
                perms.add("SYSTEM_SETTINGS");
                perms.add("FORCE_LOGOUT");
                perms.add("BACKUP_RESTORE");
                break;

            case "TEACHER":
                perms.add("CREATE_EXAM");
                perms.add("EDIT_EXAM");
                perms.add("DELETE_EXAM");
                perms.add("VIEW_STUDENTS");
                perms.add("GRADE_EXAMS");
                perms.add("VIEW_REPORTS");
                perms.add("MANAGE_QUESTIONS");
                break;

            case "STUDENT":
                perms.add("TAKE_EXAM");
                perms.add("VIEW_RESULTS");
                perms.add("VIEW_EXAMS");
                break;
        }

        return perms;
    }

    @Override
    public String toString() {
        return String.format("User{username='%s', name='%s', role='%s', active=%s}",
                username, name, role, active);
    }
}