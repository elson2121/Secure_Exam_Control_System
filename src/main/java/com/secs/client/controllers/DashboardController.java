package com.secs.client.controllers;

import com.secs.client.ClientContext;
import com.secs.shared.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DashboardController {

    @FXML private Label lblWelcome;

    @FXML
    private void initialize() {
        User user = ClientContext.getInstance().getCurrentUser();
        if (user != null) {
            lblWelcome.setText("Welcome, " + user.getName() + " (" + user.getRole() + ")");
        }
    }

    @FXML
    private void handleLogout() {
        ClientContext.getInstance().logout();
        navigateToLogin();
    }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();
            Stage stage = ClientContext.getInstance().getPrimaryStage();
            Scene scene = new Scene(root, 1000, 700);
            stage.setTitle("SECS - Secure Exam Control System");
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}