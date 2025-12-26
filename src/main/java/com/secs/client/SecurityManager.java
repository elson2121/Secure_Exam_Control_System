package com.secs.client;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

public class SecurityManager {

    public static void enableExamMode(Stage stage) {
        System.out.println("Enabling exam mode security...");

        // Block window closing
        stage.setOnCloseRequest(event -> {
            event.consume(); // Prevent closing
            showWarning("Security Alert", "Cannot close the application during an exam!");
        });

        // Block keyboard shortcuts
        stage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown() || event.isAltDown()) {
                switch (event.getCode()) {
                    case F4: // Alt+F4
                    case W:  // Ctrl+W
                    case Q:  // Ctrl+Q (Mac)
                    case TAB: // Alt+Tab
                        event.consume();
                        showWarning("Security Alert", "This shortcut is disabled during exam!");
                        break;
                    case C:  // Ctrl+C (copy)
                    case V:  // Ctrl+V (paste)
                        if (event.isControlDown()) {
                            event.consume();
                            showWarning("Security Alert", "Copy/Paste is disabled during exam!");
                        }
                        break;
                }
            }

            // Block function keys F1-F12
            if (event.getCode().isFunctionKey()) {
                event.consume();
            }

            // Block Print Screen
            if (event.getCode() == KeyCode.PRINTSCREEN) {
                event.consume();
                showWarning("Security Alert", "Screenshots are disabled!");
            }
        });

        // Clear clipboard
        clearClipboard();

        System.out.println("Exam mode security enabled");
    }

    public static void disableExamMode(Stage stage) {
        stage.setOnCloseRequest(null);
        System.out.println("Exam mode security disabled");
    }

    private static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void clearClipboard() {
        try {
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new java.awt.datatransfer.StringSelection(""), null);
            System.out.println("Clipboard cleared");
        } catch (Exception e) {
            System.err.println("Failed to clear clipboard: " + e.getMessage());
        }
    }
}