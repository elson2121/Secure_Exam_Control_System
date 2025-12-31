package com.secs.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

public class StudentController {

    public VBox createStudentUI() {
        Label titleLabel = new Label("STUDENT MANAGEMENT");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<String> studentList = new ListView<>();
        studentList.setPrefHeight(200);

        ObservableList<String> students = FXCollections.observableArrayList(
                "CS/001/14 - John Smith (Active)",
                "CS/002/14 - Sarah Johnson (Active)",
                "CS/003/14 - Michael Brown (Active)",
                "CS/004/14 - Emily Davis (Inactive)",
                "CS/005/14 - Robert Wilson (Suspended)"
        );
        studentList.setItems(students);

        Label nameLabel = new Label("Name: Not selected");
        Label idLabel = new Label("ID: Not selected");
        Label statusLabel = new Label("Status: Not selected");

        TextArea detailsArea = new TextArea();
        detailsArea.setPrefHeight(150);
        detailsArea.setEditable(false);

        ToggleGroup statusGroup = new ToggleGroup();
        RadioButton activeRadio = new RadioButton("Active");
        RadioButton inactiveRadio = new RadioButton("Inactive");
        RadioButton suspendedRadio = new RadioButton("Suspended");

        activeRadio.setToggleGroup(statusGroup);
        inactiveRadio.setToggleGroup(statusGroup);
        suspendedRadio.setToggleGroup(statusGroup);
        activeRadio.setSelected(true);

        Button updateButton = new Button("Update Status");
        updateButton.setOnAction(e -> {
            String selected = studentList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Updated");
                alert.setContentText("Student status updated successfully");
                alert.showAndWait();
            }
        });

        studentList.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                String[] parts = selected.split(" - ");
                if (parts.length >= 2) {
                    nameLabel.setText("Name: " + parts[1].replace("(Active)", "").replace("(Inactive)", "").replace("(Suspended)", "").trim());
                    idLabel.setText("ID: " + parts[0]);

                    String status = "Active";
                    if (selected.contains("(Inactive)")) status = "Inactive";
                    else if (selected.contains("(Suspended)")) status = "Suspended";
                    statusLabel.setText("Status: " + status);

                    detailsArea.setText("Student details for " + parts[0] + "\n\nRegistered: 2023\nDepartment: Computer Science\nYear: 3rd");
                }
            }
        });

        VBox statusBox = new VBox(10,
                new Label("Change Status:"),
                activeRadio, inactiveRadio, suspendedRadio,
                updateButton
        );

        VBox mainBox = new VBox(15,
                titleLabel,
                new Label("Student List:"),
                studentList,
                nameLabel,
                idLabel,
                statusLabel,
                new Label("Details:"),
                detailsArea,
                statusBox
        );

        mainBox.setStyle("-fx-padding: 20px; -fx-spacing: 10px;");

        if (!students.isEmpty()) {
            studentList.getSelectionModel().select(0);
        }

        return mainBox;
    }
}