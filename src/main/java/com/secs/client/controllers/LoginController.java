package com.secs.controller;

import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginController {

    public VBox createLoginUI() {
        Label titleLabel = new Label("SECS LOGIN");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setStyle("-fx-font-size: 14px; -fx-padding: 8px;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-font-size: 14px; -fx-padding: 8px;");

        Label statusLabel = new Label("Enter credentials");
        statusLabel.setStyle("-fx-font-size: 12px;");

        ProgressIndicator progress = new ProgressIndicator();
        progress.setVisible(false);

        Button loginButton = new Button("LOGIN");
        loginButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 20px;");
        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Please enter both fields");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            progress.setVisible(true);
            loginButton.setDisable(true);
            statusLabel.setText("Authenticating...");

            new Thread(() -> {
                try {
                    Thread.sleep(1000);

                    boolean success = username.equals("admin") && password.equals("admin123");

                    javafx.application.Platform.runLater(() -> {
                        progress.setVisible(false);
                        loginButton.setDisable(false);

                        if (success) {
                            statusLabel.setText("Login successful!");
                            statusLabel.setStyle("-fx-text-fill: green;");

                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                    javafx.scene.control.Alert.AlertType.INFORMATION);
                            alert.setTitle("Success");
                            alert.setContentText("Welcome to SECS!");
                            alert.showAndWait();

                            Stage stage = (Stage) loginButton.getScene().getWindow();
                            stage.close();
                        } else {
                            statusLabel.setText("Invalid credentials");
                            statusLabel.setStyle("-fx-text-fill: red;");
                            passwordField.clear();
                        }
                    });
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        });

        Button cancelButton = new Button("CANCEL");
        cancelButton.setOnAction(e -> {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        });

        VBox loginBox = new VBox(15,
                titleLabel,
                new Label("Username:"), usernameField,
                new Label("Password:"), passwordField,
                statusLabel,
                loginButton, cancelButton,
                progress
        );

        loginBox.setStyle("-fx-padding: 30px; -fx-alignment: center;");
        loginBox.setPrefSize(400, 400);

        return loginBox;
    }
}