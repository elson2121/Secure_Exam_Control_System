package com.secs.client.controllers;

import com.secs.client.ClientContext;
import com.secs.exam.ExamSystem;
import com.secs.security.ExamSecurityManager;
import com.secs.shared.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.ArrayList;
import java.util.List;

public class TeacherController {
    
    @FXML private ListView<String> examList;
    @FXML private TextField txtExamTitle, txtDuration, txtTotalMarks, txtPoints;
    @FXML private TextField txtOption1, txtOption2, txtOption3, txtOption4;
    @FXML private TextArea txtExamDescription, txtQuestion;
    @FXML private ComboBox<Integer> cmbCorrectAnswer;
    @FXML private Label lblStatus;
    
    private ObservableList<String> exams = FXCollections.observableArrayList();
    private ExamSystem.Exam currentExam = null;
    private List<ExamSystem.Question> currentQuestions = new ArrayList<>();
    
    @FXML
    private void initialize() {
        System.out.println("Teacher Controller initialized");
        cmbCorrectAnswer.getItems().addAll(1, 2, 3, 4);
        cmbCorrectAnswer.setValue(1);
        loadExams();
        
        User user = ClientContext.getInstance().getCurrentUser();
        if (user != null) {
            lblStatus.setText("Welcome, " + user.getName() + " (Teacher)");
        }
    }
    
    private void loadExams() {
        exams.clear();
        List<ExamSystem.Exam> teacherExams = ExamSystem.getInstance().getExamsForUser(
            ClientContext.getInstance().getCurrentUser()
        );
        for (ExamSystem.Exam exam : teacherExams) {
            exams.add(exam.getTitle() + " (ID: " + exam.getExamId() + ")");
        }
        examList.setItems(exams);
    }
    
    @FXML
    private void handleCreateExam() {
        String title = txtExamTitle.getText().trim();
        String description = txtExamDescription.getText().trim();
        String durationStr = txtDuration.getText().trim();
        String marksStr = txtTotalMarks.getText().trim();
        
        if (title.isEmpty() || durationStr.isEmpty() || marksStr.isEmpty()) {
            showAlert("Error", "Please fill required fields");
            return;
        }
        
        try {
            int duration = Integer.parseInt(durationStr);
            int marks = Integer.parseInt(marksStr);
            User teacher = ClientContext.getInstance().getCurrentUser();
            currentExam = ExamSystem.getInstance().createExam(
                title, description, teacher, duration, marks
            );
            currentQuestions.clear();
            clearQuestionForm();
            showAlert("Success", "Exam created: " + title + "\nExam ID: " + currentExam.getExamId());
            loadExams();
        } catch (NumberFormatException e) {
            showAlert("Error", "Duration and Marks must be numbers");
        }
    }
    
    @FXML
    private void handleAddQuestion() {
        if (currentExam == null) {
            showAlert("Error", "Please create an exam first");
            return;
        }
        
        String questionText = txtQuestion.getText().trim();
        String pointsStr = txtPoints.getText().trim();
        
        if (questionText.isEmpty() || pointsStr.isEmpty()) {
            showAlert("Error", "Please fill question and points");
            return;
        }
        
        List<String> options = new ArrayList<>();
        if (!txtOption1.getText().trim().isEmpty()) options.add(txtOption1.getText().trim());
        if (!txtOption2.getText().trim().isEmpty()) options.add(txtOption2.getText().trim());
        if (!txtOption3.getText().trim().isEmpty()) options.add(txtOption3.getText().trim());
        if (!txtOption4.getText().trim().isEmpty()) options.add(txtOption4.getText().trim());
        
        if (options.size() < 2) {
            showAlert("Error", "At least 2 options required");
            return;
        }
        
        try {
            int points = Integer.parseInt(pointsStr);
            int correctIndex = cmbCorrectAnswer.getValue() - 1;
            
            ExamSystem.Question question = new ExamSystem.Question(
                "Q" + (currentQuestions.size() + 1),
                questionText,
                options,
                correctIndex,
                points
            );
            
            currentQuestions.add(question);
            currentExam.addQuestion(question);
            clearQuestionForm();
            showAlert("Success", "Question added. Total: " + currentQuestions.size());
        } catch (NumberFormatException e) {
            showAlert("Error", "Points must be a number");
        }
    }
    
    @FXML
    private void handleAssignStudents() {
        if (currentExam == null) {
            showAlert("Error", "No exam selected");
            return;
        }
        showAlert("Assign Students", 
            "Exam: " + currentExam.getTitle() + "\n" +
            "Students can be assigned in the full version.\n" +
            "For demo: student1 and student2 are auto-assigned.");
    }
    
    @FXML
    private void handleViewResults() {
        List<ExamSystem.ExamResult> results = ExamSystem.getInstance().getAllResults(
            ClientContext.getInstance().getCurrentUser()
        );
        
        StringBuilder sb = new StringBuilder();
        sb.append("Total Results: ").append(results.size()).append("\n\n");
        for (ExamSystem.ExamResult result : results) {
            sb.append("Student: ").append(result.getStudentId()).append("\n")
              .append("Exam: ").append(result.getExamId()).append("\n")
              .append("Score: ").append(result.getScore()).append("/").append(result.getTotalMarks()).append("\n")
              .append("Grade: ").append(result.getGrade()).append("\n")
              .append("Time: ").append(result.getSubmissionTime()).append("\n\n");
        }
        showAlert("Exam Results", sb.toString());
    }
    
    @FXML
    private void handleLogout() {
        showLogoutDialog();
    }
    
    private void showLogoutDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Admin Override Required");
        dialog.setHeaderText("Teacher Logout");
        dialog.setContentText("Enter admin override password to logout:");
        
        dialog.showAndWait().ifPresent(password -> {
            User currentUser = ClientContext.getInstance().getCurrentUser();
            if (ExamSecurityManager.getInstance().canLogout(currentUser, password)) {
                navigateToLogin();
            } else {
                showAlert("Error", "Invalid override password");
            }
        });
    }
    
    private void clearQuestionForm() {
        txtQuestion.clear();
        txtOption1.clear();
        txtOption2.clear();
        txtOption3.clear();
        txtOption4.clear();
        txtPoints.clear();
        cmbCorrectAnswer.setValue(1);
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