package com.secs.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class ExamListController {

    public VBox createExamListUI() {
        Label titleLabel = new Label("AVAILABLE EXAMS");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<String> examList = new ListView<>();
        examList.setPrefHeight(250);

        ObservableList<String> exams = FXCollections.observableArrayList(
                "CS101 - Introduction to Programming (60 mins)",
                "CS201 - Data Structures (90 mins)",
                "CS301 - Computer Networks (75 mins)",
                "SEC101 - Cybersecurity Basics (60 mins)",
                "SEC201 - Network Security (90 mins)"
        );
        examList.setItems(exams);

        Button startButton = new Button("START EXAM");
        startButton.setDisable(true);

        examList.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            startButton.setDisable(selected == null);
        });

        startButton.setOnAction(e -> {
            String selected = examList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Exam Starting");
                alert.setContentText("Starting: " + selected + "\n\nGood luck!");
                alert.showAndWait();
            }
        });

        VBox examBox = new VBox(15,
                titleLabel,
                new Label("Select an exam:"),
                examList,
                startButton
        );

        examBox.setStyle("-fx-padding: 25px; -fx-alignment: center;");

        return examBox;
    }
}