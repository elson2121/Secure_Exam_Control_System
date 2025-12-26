package com.secs.controller;

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

public class ExamController {

    public VBox createExamUI() {
        Label titleLabel = new Label("EXAMINATION");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label timerLabel = new Label("Time: 60:00");
        timerLabel.setStyle("-fx-font-size: 16px;");

        ProgressBar timeBar = new ProgressBar(1.0);
        timeBar.setPrefWidth(400);

        Label questionLabel = new Label("Question 1: Explain the principle of least privilege.");
        questionLabel.setWrapText(true);

        TextArea answerArea = new TextArea();
        answerArea.setPromptText("Type your answer here...");
        answerArea.setPrefHeight(200);

        Button saveButton = new Button("SAVE ANSWER");
        Button nextButton = new Button("NEXT QUESTION");
        Button submitButton = new Button("SUBMIT EXAM");
        submitButton.setStyle("-fx-background-color: #0066cc; -fx-text-fill: white;");

        saveButton.setOnAction(e -> {
            if (!answerArea.getText().trim().isEmpty()) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Saved");
                alert.setContentText("Answer saved successfully.");
                alert.showAndWait();
            }
        });

        submitButton.setOnAction(e -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.CONFIRMATION);
            alert.setTitle("Submit Exam");
            alert.setContentText("Are you sure you want to submit?");
            alert.showAndWait();
        });

        VBox examBox = new VBox(15,
                titleLabel,
                timerLabel,
                timeBar,
                questionLabel,
                new Label("Your Answer:"),
                answerArea,
                saveButton,
                nextButton,
                submitButton
        );

        examBox.setStyle("-fx-padding: 30px; -fx-spacing: 15px;");

        return examBox;
    }
}