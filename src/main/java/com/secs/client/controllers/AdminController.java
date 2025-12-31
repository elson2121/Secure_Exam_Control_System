package com.secs.client.controllers;

import com.secs.client.ClientContext;
import com.secs.exam.ExamSystem;
import com.secs.security.ExamSecurityManager;
import com.secs.shared.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class AdminController {
    
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> colUsername, colName, colRole;
    @FXML private TableColumn<User, Boolean> colActive;
    @FXML private TextField txtUsername, txtPassword, txtName;
    @FXML private ComboBox<String> cmbRole;
    @FXML private Label lblStatus;
    
    private ObservableList<User> users = FXCollections.observableArrayList();
    
    @FXML
    private void initialize() {
        System.out.println("Admin Controller initialized");
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));
        
        cmbRole.getItems().addAll("ADMIN", "TEACHER", "STUDENT");
        cmbRole.setValue("STUDENT");
        loadUsers();
        
        User currentUser = ClientContext.getInstance().getCurrentUser();
        if (currentUser != null) {
            lblStatus.setText("Welcome, Admin " + currentUser.getName());
        }
    }
    
    private void loadUsers() {
        users.clear();
        users.addAll(
            new User("admin", "admin123", "System Administrator", "ADMIN", "A001"),
            new User("teacher1", "admin123", "Math Teacher", "TEACHER", "T001"),
            new User("teacher2", "admin123", "Science Teacher", "TEACHER", "T002"),
            new User("student1", "pass123", "Student One", "STUDENT", "S001"),
            new User("student2", "pass123", "Student Two", "STUDENT", "S002"),
            new User("student3", "pass123", "Student Three", "STUDENT", "S003")
        );
        userTable.setItems(users);
    }
    
    @FXML
    private void handleCreateUser() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();
        String name = txtName.getText().trim();
        String role = cmbRole.getValue();
        
        if (username.isEmpty() || password.isEmpty() || name.isEmpty()) {
            showAlert("Error", "Please fill all fields");
            return;
        }
        
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                showAlert("Error", "Username already exists");
                return;
            }
        }
        
        String userId = "U" + (users.size() + 100);
        User newUser = new User(username, password, name, role, userId);
        newUser.setCreatedBy(ClientContext.getInstance().getCurrentUser().getUsername());
        users.add(newUser);
        userTable.refresh();
        clearForm();
        showAlert("Success", "User created: " + username);
    }
    
    @FXML
    private void handleDeleteUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a user");
            return;
        }
        
        if (selected.getUsername().equals(ClientContext.getInstance().getCurrentUser().getUsername())) {
            showAlert("Error", "Cannot delete your own account");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete User: " + selected.getUsername());
        confirm.setContentText("Are you sure?");
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            users.remove(selected);
            userTable.refresh();
            showAlert("Success", "User deleted: " + selected.getUsername());
        }
    }
    
    @FXML
    private void handleToggleActive() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a user");
            return;
        }
        
        selected.setActive(!selected.isActive());
        userTable.refresh();
        String status = selected.isActive() ? "activated" : "deactivated";
        showAlert("Success", "User " + status + ": " + selected.getUsername());
    }
    
    @FXML
    private void handleViewExams() {
        var exams = ExamSystem.getInstance().getExamsForUser(
            ClientContext.getInstance().getCurrentUser()
        );
        
        StringBuilder sb = new StringBuilder();
        sb.append("Total Exams: ").append(exams.size()).append("\n\n");
        for (var exam : exams) {
            sb.append("• ").append(exam.getTitle())
              .append(" (ID: ").append(exam.getExamId()).append(")\n")
              .append("  Created by: ").append(exam.getCreatedBy()).append("\n")
              .append("  Duration: ").append(exam.getDurationMinutes()).append(" mins\n")
              .append("  Marks: ").append(exam.getTotalMarks()).append("\n\n");
        }
        showAlert("System Exams", sb.toString());
    }
    
    @FXML
    private void handleSystemSettings() {
        TextInputDialog dialog = new TextInputDialog(
            ExamSecurityManager.getInstance().getAdminOverridePassword()
        );
        dialog.setTitle("Admin Override Password");
        dialog.setHeaderText("Change Admin Override Password");
        dialog.setContentText("New password (min 8 chars):");
        
        dialog.showAndWait().ifPresent(newPassword -> {
            if (newPassword.length() >= 8) {
                ExamSecurityManager.getInstance().setAdminOverridePassword(
                    newPassword, 
                    ClientContext.getInstance().getCurrentUser()
                );
                showAlert("Success", "Override password updated");
            } else {
                showAlert("Error", "Password must be at least 8 characters");
            }
        });
    }
    
    @FXML
    private void handleForceLogout() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a user");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Force Logout");
        confirm.setHeaderText("Force User Logout");
        confirm.setContentText("Force logout user: " + selected.getUsername() + "?");
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            showAlert("Success", "User " + selected.getUsername() + " logged out (simulated)");
        }
    }
    
    @FXML
    private void handleBackup() {
        showAlert("Backup", "System backup initiated (simulated)");
    }
    
    @FXML
    private void handleLogout() {
        navigateToLogin();
    }
    
    private void clearForm() {
        txtUsername.clear();
        txtPassword.clear();
        txtName.clear();
        cmbRole.setValue("STUDENT");
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void navigateToLogin() {
        try {
            ClientContext.getInstance().logout();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/views/login.fxml")
            );
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = ClientContext.getInstance().getPrimaryStage();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1000, 700);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}