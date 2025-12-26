package com.secs.controller;

import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class ResultController {

    public VBox createResultUI() {
        Label titleLabel = new Label("EXAM RESULTS");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label studentLabel = new Label("Student: Kuei Poch Kuei (CS/0032/14)");
        Label examLabel = new Label("Exam: Computer Networks (CS301)");

        int score = 78;
        String grade = score >= 70 ? "B" : score >= 60 ? "C" : score >= 50 ? "D" : "F";
        Color gradeColor = grade.equals("B") ? Color.GREEN : grade.equals("C") ? Color.BLUE :
                grade.equals("D") ? Color.ORANGE : Color.RED;

        Label scoreLabel = new Label("Score: " + score + "/100");
        scoreLabel.setStyle("-fx-font-size: 16px;");

        ProgressBar scoreBar = new ProgressBar(score / 100.0);
        scoreBar.setPrefWidth(300);

        Label gradeLabel = new Label("Grade: " + grade);
        gradeLabel.setTextFill(gradeColor);
        gradeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label statusLabel = new Label("Status: " + (score >= 50 ? "PASSED ✓" : "FAILED ✗"));
        statusLabel.setStyle("-fx-font-size: 14px; " + (score >= 50 ? "-fx-text-fill: green;" : "-fx-text-fill: red;"));

        Label remarksLabel = new Label("Remarks: Good performance. Shows understanding of core concepts.");
        remarksLabel.setWrapText(true);

        Button printButton = new Button("Print Results");
        printButton.setOnAction(e -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Print");
            alert.setContentText("Results would be printed here.");
            alert.showAndWait();
        });

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        });

        VBox resultBox = new VBox(15,
                titleLabel,
                studentLabel,
                examLabel,
                scoreLabel,
                scoreBar,
                gradeLabel,
                statusLabel,
                remarksLabel,
                printButton,
                closeButton
        );

        resultBox.setStyle("-fx-padding: 30px; -fx-alignment: center; -fx-spacing: 10px;");

        return resultBox;
    }
}