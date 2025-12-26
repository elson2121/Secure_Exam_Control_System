package com.secs.client.controllers;

import com.secs.client.ClientContext;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public abstract class BaseController {

    protected void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = ClientContext.getInstance().getPrimaryStage();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            showErrorDialog("Navigation Error", "Failed to load: " + fxmlPath, e.getMessage());
        }
    }

    protected void showToast(String message, String type) {
        // Implement toast notification
        System.out.println("[" + type + "] " + message);
    }

    protected void showErrorDialog(String title, String header, String content) {
        // Implement error dialog
        System.out.println("ERROR: " + title + " - " + header + ": " + content);
    }
}