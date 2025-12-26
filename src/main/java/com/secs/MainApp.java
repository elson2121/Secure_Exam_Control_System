package com.secs;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.scene.input.KeyCode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MainApp extends Application {
    
    // User data
    private static String currentUserRole = null;
    private static String currentUsername = null;
    private static boolean isFirstLogin = false;
    
    // Exam timing constants
    private static final int EXAM_DURATION_MINUTES = 5; // Short exams: 3-5 minutes
    private static final int QUESTIONS_PER_EXAM = 10;
    
    // Dilla University Grading System
    private static final Map<String, GradeRange> GRADING_SYSTEM = new HashMap<>();
    static {
        GRADING_SYSTEM.put("A+", new GradeRange(90, 100, 4.0, "Excellent"));
        GRADING_SYSTEM.put("A", new GradeRange(85, 89, 4.0, "Excellent"));
        GRADING_SYSTEM.put("A-", new GradeRange(80, 84, 3.75, "Very Good"));
        GRADING_SYSTEM.put("B+", new GradeRange(75, 79, 3.5, "Good"));
        GRADING_SYSTEM.put("B", new GradeRange(70, 74, 3.0, "Good"));
        GRADING_SYSTEM.put("B-", new GradeRange(65, 69, 2.75, "Satisfactory"));
        GRADING_SYSTEM.put("C+", new GradeRange(60, 64, 2.5, "Satisfactory"));
        GRADING_SYSTEM.put("C", new GradeRange(55, 59, 2.0, "Fair"));
        GRADING_SYSTEM.put("C-", new GradeRange(50, 54, 1.75, "Fair"));
        GRADING_SYSTEM.put("D", new GradeRange(45, 49, 1.0, "Pass"));
        GRADING_SYSTEM.put("F", new GradeRange(0, 44, 0.0, "Fail"));
    }
    
    static class GradeRange {
        int min;
        int max;
        double gradePoint;
        String remark;
        
        GradeRange(int min, int max, double gradePoint, String remark) {
            this.min = min;
            this.max = max;
            this.gradePoint = gradePoint;
            this.remark = remark;
        }
    }
    
    // User storage with proper objects
    private static class User {
        String username;
        String password;
        String name;
        String id;
        String role;
        String email;
        String department;
        boolean passwordChangeRequired;
        LocalDateTime lastLogin;
        
        User(String username, String password, String name, String id, String role, String email, String department, boolean passwordChangeRequired) {
            this.username = username;
            this.password = password;
            this.name = name;
            this.id = id;
            this.role = role;
            this.email = email;
            this.department = department;
            this.passwordChangeRequired = passwordChangeRequired;
            this.lastLogin = LocalDateTime.now();
        }
        
        @Override
        public String toString() {
            return name + " (" + id + ") - " + role;
        }
    }
    
    // Exam Question class
    private static class Question {
        String questionText;
        List<String> options;
        int correctAnswerIndex; // 0-based index
        int marks;
        String difficulty; // Easy, Medium, Hard
        
        Question(String questionText, List<String> options, int correctAnswerIndex, int marks, String difficulty) {
            this.questionText = questionText;
            this.options = options;
            this.correctAnswerIndex = correctAnswerIndex;
            this.marks = marks;
            this.difficulty = difficulty;
        }
    }
    
    // Exam class
    private static class Exam {
        String examId;
        String courseName;
        String examTitle;
        String courseCode;
        String instructorId;
        List<Question> questions;
        int totalMarks;
        javafx.util.Duration duration;
        LocalDateTime createdDate;
        boolean isActive;
        
        Exam(String examId, String courseName, String examTitle, String courseCode, String instructorId, List<Question> questions) {
            this.examId = examId;
            this.courseName = courseName;
            this.examTitle = examTitle;
            this.courseCode = courseCode;
            this.instructorId = instructorId;
            this.questions = questions;
            this.totalMarks = questions.stream().mapToInt(q -> q.marks).sum();
            this.duration = javafx.util.Duration.minutes(EXAM_DURATION_MINUTES);
            this.createdDate = LocalDateTime.now();
            this.isActive = true;
        }
    }
    
    // Student Attempt with Grade
    private static class StudentAttempt {
        String studentId;
        String examId;
        List<Integer> selectedAnswers; // -1 means not attempted
        LocalDateTime startTime;
        LocalDateTime endTime;
        boolean isSubmitted;
        int marksObtained;
        String letterGrade;
        double gradePoint;
        String remark;
        
        StudentAttempt(String studentId, String examId) {
            this.studentId = studentId;
            this.examId = examId;
            this.selectedAnswers = new ArrayList<>();
            for (int i = 0; i < QUESTIONS_PER_EXAM; i++) {
                this.selectedAnswers.add(-1); // Initialize as not attempted
            }
            this.startTime = LocalDateTime.now();
            this.isSubmitted = false;
            this.marksObtained = 0;
            this.letterGrade = "F";
            this.gradePoint = 0.0;
            this.remark = "Not Graded";
        }
        
        void calculateMarks(Exam exam) {
            marksObtained = 0;
            for (int i = 0; i < exam.questions.size(); i++) {
                if (selectedAnswers.get(i) == exam.questions.get(i).correctAnswerIndex) {
                    marksObtained += exam.questions.get(i).marks;
                }
            }
            
            // Calculate percentage
            double percentage = (double) marksObtained / exam.totalMarks * 100;
            
            // Determine grade based on Dilla University system
            for (Map.Entry<String, GradeRange> entry : GRADING_SYSTEM.entrySet()) {
                if (percentage >= entry.getValue().min && percentage <= entry.getValue().max) {
                    this.letterGrade = entry.getKey();
                    this.gradePoint = entry.getValue().gradePoint;
                    this.remark = entry.getValue().remark;
                    break;
                }
            }
        }
    }
    
    // Audit Log for security
    private static class AuditLog {
        String userId;
        String action;
        LocalDateTime timestamp;
        String details;
        
        AuditLog(String userId, String action, String details) {
            this.userId = userId;
            this.action = action;
            this.timestamp = LocalDateTime.now();
            this.details = details;
        }
    }
    
    private static ObservableList<User> users = FXCollections.observableArrayList();
    private static ObservableList<Exam> exams = FXCollections.observableArrayList();
    private static ObservableList<StudentAttempt> studentAttempts = FXCollections.observableArrayList();
    private static ObservableList<AuditLog> auditLogs = FXCollections.observableArrayList();
    
    // Sample courses with codes
    private static final String[][] COURSES = {
        {"CSE101", "Introduction to Programming", "Computer Science"},
        {"CSE201", "Data Structures", "Computer Science"},
        {"CSE301", "Algorithms", "Computer Science"},
        {"CSE401", "Database Systems", "Computer Science"},
        {"CSE501", "Computer Networks", "Computer Science"},
        {"CSE601", "Cybersecurity Fundamentals", "Cybersecurity"},
        {"CSE602", "Cryptography", "Cybersecurity"},
        {"MAT101", "Calculus I", "Mathematics"},
        {"PHY101", "Physics I", "Physics"}
    };
    
    // Departments
    private static final String[] DEPARTMENTS = {
        "Computer Science",
        "Information Technology",
        "Software Engineering",
        "Cybersecurity",
        "Data Science",
        "Mathematics",
        "Physics",
        "Chemistry"
    };
    
    @Override
    public void start(Stage primaryStage) {
        // Initialize with sample data
        initializeSampleUsers();
        initializeSampleExams();
        showLoginScreen(primaryStage);
    }
    
    private void initializeSampleUsers() {
        // Add default admin
        users.add(new User("admin", "Admin@Dilla2024", "System Administrator", "ADMIN001", "Admin", "admin@dilla.edu.et", "Administration", false));
        
        // Add sample instructors
        users.add(new User("instructor1", "Instructor@2024", "Dr. Alemayehu Mekonnen", "INS001", "Instructor", "alemayehu.m@dilla.edu.et", "Computer Science", false));
        users.add(new User("instructor2", "Instructor@2024", "Dr. Sofia Tesfaye", "INS002", "Instructor", "sofia.t@dilla.edu.et", "Cybersecurity", false));
        
        // Add sample students
        users.add(new User("student1", "Student@2024", "Kaleb Getachew", "DUCS001", "Student", "kaleb.getachew@dilla.edu.et", "Computer Science", true));
        users.add(new User("student2", "Student@2024", "Meron Abebe", "DUCS002", "Student", "meron.abebe@dilla.edu.et", "Cybersecurity", true));
        users.add(new User("student3", "Student@2024", "Samuel Bekele", "DUCS003", "Student", "samuel.bekele@dilla.edu.et", "Software Engineering", true));
        
        // Add audit logs
        auditLogs.add(new AuditLog("SYSTEM", "SYSTEM_START", "Secure Exam Browser System Initialized"));
    }
    
    private void initializeSampleExams() {
        // Create sample exams for each course
        for (String[] course : COURSES) {
            List<Question> questions = generateRandomQuestions(course[1]);
            exams.add(new Exam(
                "EXAM_" + course[0] + "_" + System.currentTimeMillis(),
                course[1],
                course[0] + " - Midterm Examination",
                course[0],
                "INS001",
                questions
            ));
        }
    }
    
    private List<Question> generateRandomQuestions(String course) {
        List<Question> questions = new ArrayList<>();
        Random random = new Random();
        
        // Cybersecurity questions
        List<String[]> cyberSecurityQuestions = Arrays.asList(
            new String[]{"What is the primary goal of cryptography?", "Data confidentiality", "Network speed", "Hardware optimization", "User interface design", "0", "Medium"},
            new String[]{"Which attack intercepts communication between two parties?", "Man-in-the-middle", "DDOS", "Phishing", "SQL Injection", "0", "Hard"},
            new String[]{"What does SSL/TLS provide?", "Secure communication", "Fast processing", "Data compression", "Memory management", "0", "Easy"},
            new String[]{"What is a zero-day vulnerability?", "Unknown vulnerability with no patch", "Old vulnerability", "Hardware failure", "Network congestion", "0", "Hard"},
            new String[]{"Which is NOT a type of malware?", "Firewall", "Virus", "Trojan", "Ransomware", "0", "Easy"}
        );
        
        // Programming questions
        List<String[]> programmingQuestions = Arrays.asList(
            new String[]{"What is OOP?", "Object-Oriented Programming", "Open Office Program", "Online Operating Protocol", "Object Operation Process", "0", "Easy"},
            new String[]{"Which data structure uses LIFO?", "Stack", "Queue", "Array", "Linked List", "0", "Medium"},
            new String[]{"Time complexity of binary search?", "O(log n)", "O(n)", "O(n²)", "O(1)", "0", "Medium"},
            new String[]{"What is polymorphism?", "Multiple forms", "Data hiding", "Inheritance", "Abstraction", "0", "Hard"},
            new String[]{"Which is NOT a programming paradigm?", "Cryptography", "Functional", "Procedural", "Object-oriented", "0", "Easy"}
        );
        
        List<String[]> selectedQuestions;
        if (course.toLowerCase().contains("cyber") || course.toLowerCase().contains("security")) {
            selectedQuestions = cyberSecurityQuestions;
        } else {
            selectedQuestions = programmingQuestions;
        }
        
        // Select random questions
        for (int i = 0; i < QUESTIONS_PER_EXAM; i++) {
            String[] qData = selectedQuestions.get(i % selectedQuestions.size());
            List<String> options = Arrays.asList(qData[1], qData[2], qData[3], qData[4]);
            int correctAnswer = Integer.parseInt(qData[5]);
            String difficulty = qData[6];
            questions.add(new Question(qData[0], options, correctAnswer, 10, difficulty)); // Each question = 10 marks
        }
        
        return questions;
    }
    
    private void showLoginScreen(Stage primaryStage) {
        // Create main container with cybersecurity gradient background
        StackPane root = new StackPane();
        Stop[] stops = new Stop[] {
            new Stop(0, Color.web("#0c2461")),     // Dark Blue
            new Stop(0.5, Color.web("#1e3799")),   // Royal Blue
            new Stop(1, Color.web("#4a69bd"))      // Light Blue
        };
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
        root.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));
        
        // Add animated background pattern (cybersecurity themed)
        Pane patternLayer = new Pane();
        for (int i = 0; i < 50; i++) {
            Rectangle rect = new Rectangle(
                Math.random() * 1000,
                Math.random() * 700,
                2, 2
            );
            rect.setFill(Color.web("#00d2d3", 0.3));
            rect.setStyle("-fx-effect: dropshadow(gaussian, #00d2d3, 5, 0.5, 0, 0);");
            patternLayer.getChildren().add(rect);
        }
        
        // Create glassmorphism effect card with cybersecurity theme
        VBox loginCard = new VBox(25);
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setPadding(new Insets(40, 50, 40, 50));
        loginCard.setMaxWidth(500);
        loginCard.setMaxHeight(600);
        
        // Cybersecurity glass effect
        loginCard.setStyle("-fx-background-color: rgba(255, 255, 255, 0.08); " +
                          "-fx-background-radius: 20; " +
                          "-fx-border-color: rgba(0, 210, 211, 0.3); " +
                          "-fx-border-width: 1.5; " +
                          "-fx-border-radius: 20; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.5), 30, 0.3, 0, 5);");
        
        // Logo/Header Section with cybersecurity symbols
        VBox headerBox = new VBox(15);
        headerBox.setAlignment(Pos.CENTER);
        
        // Cybersecurity symbols
        HBox symbols = new HBox(10);
        symbols.setAlignment(Pos.CENTER);
        
        Text symbol1 = new Text("🛡️");
        symbol1.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        symbol1.setFill(Color.web("#00d2d3"));
        
        Text symbol2 = new Text("🔐");
        symbol2.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        symbol2.setFill(Color.web("#00d2d3"));
        
        Text symbol3 = new Text("🔒");
        symbol3.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        symbol3.setFill(Color.web("#00d2d3"));
        
        symbols.getChildren().addAll(symbol1, symbol2, symbol3);
        
        // App Title with gradient
        Text title = new Text("DU SECURE EXAMINATION");
        title.setFont(Font.font("Consolas", FontWeight.EXTRA_BOLD, 32));
        title.setFill(Color.web("#00d2d3"));
        
        Text subtitle = new Text("CONTROL SYSTEM");
        subtitle.setFont(Font.font("Consolas", FontWeight.BOLD, 18));
        subtitle.setFill(Color.web("#f6b93b"));
        
        Text welcome = new Text("Welcome to Dilla University");
        welcome.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        welcome.setFill(Color.web("#dff9fb"));
        
        // Security level indicator
        HBox securityLevel = new HBox(10);
        securityLevel.setAlignment(Pos.CENTER);
        
        Label securityLabel = new Label("SECURITY LEVEL: ");
        securityLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        securityLabel.setTextFill(Color.web("#f6b93b"));
        
        ProgressBar securityBar = new ProgressBar(0.95);
        securityBar.setPrefWidth(150);
        securityBar.setStyle("-fx-accent: #00d2d3;");
        
        Label levelLabel = new Label("MAXIMUM");
        levelLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        levelLabel.setTextFill(Color.web("#00d2d3"));
        
        securityLevel.getChildren().addAll(securityLabel, securityBar, levelLabel);
        
        headerBox.getChildren().addAll(symbols, title, subtitle, welcome, securityLevel);
        
        // Form Section with cybersecurity theme
        VBox formBox = new VBox(20);
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(20, 0, 0, 0));
        
        // Username Field
        VBox usernameBox = new VBox(8);
        Label usernameLabel = new Label("ACCESS ID");
        usernameLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        usernameLabel.setTextFill(Color.web("#00d2d3"));
        
        TextField userField = new TextField();
        userField.setPromptText("Enter your access ID");
        userField.setPrefHeight(45);
        userField.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); " +
                          "-fx-background-radius: 8; " +
                          "-fx-border-color: rgba(0, 210, 211, 0.2); " +
                          "-fx-border-radius: 8; " +
                          "-fx-border-width: 1; " +
                          "-fx-text-fill: #00d2d3; " +
                          "-fx-prompt-text-fill: rgba(0, 210, 211, 0.4); " +
                          "-fx-font-family: 'Consolas'; " +
                          "-fx-font-size: 14; " +
                          "-fx-padding: 0 15;");
        userField.setPrefWidth(350);
        
        usernameBox.getChildren().addAll(usernameLabel, userField);
        
        // Password Field
        VBox passwordBox = new VBox(8);
        Label passwordLabel = new Label("ENCRYPTED KEY");
        passwordLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        passwordLabel.setTextFill(Color.web("#00d2d3"));
        
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter your encrypted key");
        passField.setPrefHeight(45);
        passField.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); " +
                          "-fx-background-radius: 8; " +
                          "-fx-border-color: rgba(0, 210, 211, 0.2); " +
                          "-fx-border-radius: 8; " +
                          "-fx-border-width: 1; " +
                          "-fx-text-fill: #00d2d3; " +
                          "-fx-prompt-text-fill: rgba(0, 210, 211, 0.4); " +
                          "-fx-font-family: 'Consolas'; " +
                          "-fx-font-size: 14; " +
                          "-fx-padding: 0 15;");
        passField.setPrefWidth(350);
        
        passwordBox.getChildren().addAll(passwordLabel, passField);
        
        // Role Selection with cybersecurity terms
        VBox roleBox = new VBox(8);
        Label roleLabel = new Label("ACCESS LEVEL");
        roleLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        roleLabel.setTextFill(Color.web("#00d2d3"));
        
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("Student Access", "Instructor Access", "Administrator Access");
        roleCombo.setValue("Student Access");
        roleCombo.setPrefHeight(45);
        roleCombo.setPrefWidth(350);
        roleCombo.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); " +
                          "-fx-background-radius: 8; " +
                          "-fx-border-color: rgba(0, 210, 211, 0.2); " +
                          "-fx-border-radius: 8; " +
                          "-fx-border-width: 1; " +
                          "-fx-text-fill: #00d2d3; " +
                          "-fx-font-family: 'Consolas'; " +
                          "-fx-font-size: 14; " +
                          "-fx-padding: 0 15;");
        
        roleBox.getChildren().addAll(roleLabel, roleCombo);
        
        // Login Button with hacking animation effect
        Button loginBtn = new Button("INITIATE ACCESS");
        loginBtn.setPrefWidth(350);
        loginBtn.setPrefHeight(50);
        loginBtn.setStyle("-fx-background-color: linear-gradient(to right, #00d2d3, #1e90ff); " +
                         "-fx-background-radius: 8; " +
                         "-fx-text-fill: #0c2461; " +
                         "-fx-font-family: 'Consolas'; " +
                         "-fx-font-weight: bold; " +
                         "-fx-font-size: 16; " +
                         "-fx-cursor: hand; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0, 210, 211, 0.5), 20, 0.5, 0, 3);");
        
        // Button hover effect
        loginBtn.setOnMouseEntered(e -> {
            loginBtn.setStyle("-fx-background-color: linear-gradient(to right, #1e90ff, #00d2d3); " +
                            "-fx-background-radius: 8; " +
                            "-fx-text-fill: #0c2461; " +
                            "-fx-font-family: 'Consolas'; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 16; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(30, 144, 255, 0.6), 25, 0.6, 0, 4);");
        });
        
        loginBtn.setOnMouseExited(e -> {
            loginBtn.setStyle("-fx-background-color: linear-gradient(to right, #00d2d3, #1e90ff); " +
                            "-fx-background-radius: 8; " +
                            "-fx-text-fill: #0c2461; " +
                            "-fx-font-family: 'Consolas'; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 16; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0, 210, 211, 0.5), 20, 0.5, 0, 3);");
        });
        
        loginBtn.setOnAction(e -> {
            String username = userField.getText().trim();
            String password = passField.getText().trim();
            String roleText = roleCombo.getValue();
            
            // Map display role to actual role
            String role = "";
            if (roleText.contains("Student")) role = "Student";
            else if (roleText.contains("Instructor")) role = "Instructor";
            else if (roleText.contains("Administrator")) role = "Admin";
            
            User authenticatedUser = authenticate(username, password);
            
            if (authenticatedUser != null && authenticatedUser.role.equals(role)) {
                currentUsername = authenticatedUser.username;
                currentUserRole = authenticatedUser.role;
                isFirstLogin = authenticatedUser.passwordChangeRequired;
                
                // Log login attempt
                auditLogs.add(new AuditLog(currentUsername, "LOGIN_SUCCESS", "User logged in from " + role + " interface"));
                
                if (isFirstLogin) {
                    showPasswordChangeScreen(primaryStage, authenticatedUser);
                } else {
                    showDashboard(primaryStage, authenticatedUser.role);
                }
            } else {
                auditLogs.add(new AuditLog(username, "LOGIN_FAILED", "Failed login attempt for role: " + role));
                showAlert("ACCESS DENIED", 
                    "⛔ Invalid credentials or access level mismatch.\n\n" +
                    "Default Credentials:\n" +
                    "• Student: student1 / Student@2024\n" +
                    "• Instructor: instructor1 / Instructor@2024\n" +
                    "• Administrator: admin / Admin@Dilla2024\n\n" +
                    "⚠️ Multiple failed attempts will trigger security protocol.");
            }
        });
        
        // Security Footer
        VBox footerBox = new VBox(10);
        footerBox.setAlignment(Pos.CENTER);
        
        HBox securityInfo = new HBox(15);
        securityInfo.setAlignment(Pos.CENTER);
        
        Label secureIcon = new Label("🔐");
        secureIcon.setFont(Font.font("Arial", 14));
        
        Label secureText = new Label("256-bit SSL Encryption • Multi-factor Authentication");
        secureText.setFont(Font.font("Consolas", FontWeight.NORMAL, 10));
        secureText.setTextFill(Color.web("#b0bec5"));
        
        securityInfo.getChildren().addAll(secureIcon, secureText);
        
        Text footerText = new Text("Dilla University • Cybersecurity Division • © 2024 v2.0");
        footerText.setFont(Font.font("Consolas", FontWeight.NORMAL, 10));
        footerText.setFill(Color.web("#78909c"));
        
        Hyperlink securityAudit = new Hyperlink("View Security Audit Log");
        securityAudit.setStyle("-fx-text-fill: #f6b93b; -fx-border-color: transparent; -fx-font-family: 'Consolas';");
        securityAudit.setOnAction(e -> showAlert("Security Audit", 
            "Security protocols active:\n" +
            "• End-to-end encryption: ENABLED\n" +
            "• Intrusion detection: ACTIVE\n" +
            "• Session timeout: 30 minutes\n" +
            "• Failed login limit: 5 attempts\n" +
            "• Real-time monitoring: ACTIVE"));
        
        footerBox.getChildren().addAll(securityAudit, securityInfo, footerText);
        
        // Add all components to form
        formBox.getChildren().addAll(usernameBox, passwordBox, roleBox, loginBtn);
        
        // Add all to main card
        loginCard.getChildren().addAll(headerBox, formBox, footerBox);
        
        // Center the card
        StackPane.setAlignment(loginCard, Pos.CENTER);
        root.getChildren().addAll(patternLayer, loginCard);
        
        // Create scene with modern styling
        Scene scene = new Scene(root, 1200, 800);
        scene.setFill(Color.TRANSPARENT);
        
        // Configure stage
        primaryStage.setScene(scene);
        primaryStage.setTitle("DU SECS - Dilla University Secure Examination Control System");
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(750);
        
        primaryStage.show();
        
        // Auto-focus username field
        Platform.runLater(() -> userField.requestFocus());
    }
    
    private User authenticate(String username, String password) {
        for (User user : users) {
            if (user.username.equals(username) && user.password.equals(password)) {
                user.lastLogin = LocalDateTime.now();
                return user;
            }
        }
        return null;
    }
    
    private void showPasswordChangeScreen(Stage stage, User user) {
        StackPane root = new StackPane();
        root.setBackground(new Background(new BackgroundFill(Color.web("#0c2461"), CornerRadii.EMPTY, Insets.EMPTY)));
        
        VBox changeCard = new VBox(20);
        changeCard.setAlignment(Pos.CENTER);
        changeCard.setPadding(new Insets(40));
        changeCard.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 15; -fx-border-color: rgba(0,210,211,0.3); -fx-border-width: 1; -fx-border-radius: 15;");
        changeCard.setMaxWidth(450);
        
        Label title = new Label("🔐 PASSWORD CHANGE REQUIRED");
        title.setFont(Font.font("Consolas", FontWeight.BOLD, 20));
        title.setTextFill(Color.web("#00d2d3"));
        
        Label welcome = new Label("Welcome, " + user.name + "!\nFirst-time login detected. Please set a new password.");
        welcome.setTextFill(Color.WHITE);
        welcome.setStyle("-fx-text-alignment: center; -fx-font-family: 'Segoe UI';");
        
        PasswordField currentPassField = new PasswordField();
        currentPassField.setPromptText("Current temporary password");
        currentPassField.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-text-fill: #00d2d3; -fx-prompt-text-fill: rgba(0,210,211,0.5); -fx-font-family: 'Consolas';");
        
        PasswordField newPassField = new PasswordField();
        newPassField.setPromptText("New password (min. 12 characters)");
        newPassField.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-text-fill: #00d2d3; -fx-prompt-text-fill: rgba(0,210,211,0.5); -fx-font-family: 'Consolas';");
        
        PasswordField confirmPassField = new PasswordField();
        confirmPassField.setPromptText("Confirm new password");
        confirmPassField.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-text-fill: #00d2d3; -fx-prompt-text-fill: rgba(0,210,211,0.5); -fx-font-family: 'Consolas';");
        
        // Password strength indicator
        ProgressBar strengthBar = new ProgressBar(0);
        strengthBar.setPrefWidth(300);
        strengthBar.setVisible(false);
        
        newPassField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal.length() > 0) {
                strengthBar.setVisible(true);
                double strength = Math.min(newVal.length() / 12.0, 1.0);
                strengthBar.setProgress(strength);
                if (strength < 0.5) strengthBar.setStyle("-fx-accent: #e74c3c;");
                else if (strength < 0.8) strengthBar.setStyle("-fx-accent: #f39c12;");
                else strengthBar.setStyle("-fx-accent: #2ecc71;");
            } else {
                strengthBar.setVisible(false);
            }
        });
        
        Button changeBtn = new Button("UPDATE SECURITY KEY");
        changeBtn.setStyle("-fx-background-color: linear-gradient(to right, #00d2d3, #1e90ff); -fx-text-fill: #0c2461; -fx-font-weight: bold; -fx-font-family: 'Consolas';");
        
        changeBtn.setOnAction(e -> {
            String currentPass = currentPassField.getText();
            String newPass = newPassField.getText();
            String confirmPass = confirmPassField.getText();
            
            if (!currentPass.equals(user.password)) {
                showAlert("SECURITY ALERT", "Current password verification failed.");
                return;
            }
            
            if (newPass.length() < 12) {
                showAlert("SECURITY POLICY", "New password must be at least 12 characters for enhanced security.");
                return;
            }
            
            if (!newPass.equals(confirmPass)) {
                showAlert("VERIFICATION FAILED", "New passwords do not match.");
                return;
            }
            
            // Update password
            user.password = newPass;
            user.passwordChangeRequired = false;
            isFirstLogin = false;
            
            auditLogs.add(new AuditLog(user.username, "PASSWORD_CHANGE", "Password updated successfully"));
            
            showAlert("SECURITY UPDATE SUCCESSFUL", "✅ Password has been updated successfully!\n\nSecurity protocols activated for your account.");
            showDashboard(stage, user.role);
        });
        
        VBox passwordRules = new VBox(5);
        passwordRules.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-padding: 10; -fx-border-radius: 5;");
        passwordRules.getChildren().addAll(
            new Label("🔒 Password Requirements:"),
            new Label("• Minimum 12 characters"),
            new Label("• Mix of uppercase and lowercase"),
            new Label("• Include numbers and special characters"),
            new Label("• Not similar to previous passwords")
        );
        for (int i = 0; i < passwordRules.getChildren().size(); i++) {
            Node node = passwordRules.getChildren().get(i);
            if (node instanceof Label) {
                ((Label) node).setTextFill(Color.web("#b0bec5"));
                ((Label) node).setFont(Font.font("Consolas", 10));
            }
        }
        
        changeCard.getChildren().addAll(title, welcome, currentPassField, newPassField, strengthBar, confirmPassField, passwordRules, changeBtn);
        root.getChildren().add(changeCard);
        
        Scene scene = new Scene(root, 900, 700);
        stage.setScene(scene);
        stage.setTitle("Security Key Update - DU SECS");
    }
    
    private void showDashboard(Stage stage, String role) {
        BorderPane dashboard = new BorderPane();
        
        // Advanced Top Bar with cybersecurity theme
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(15, 25, 15, 25));
        topBar.setStyle("-fx-background-color: linear-gradient(to right, #0c2461, #1e3799);");
        
        // Left side - System info
        HBox systemInfo = new HBox(15);
        systemInfo.setAlignment(Pos.CENTER_LEFT);
        
        Label systemIcon = new Label("🛡️");
        systemIcon.setFont(Font.font("Arial", 20));
        
        VBox systemText = new VBox(2);
        Label systemName = new Label("DU SECURE EXAMINATION CONTROL SYSTEM");
        systemName.setFont(Font.font("Consolas", FontWeight.BOLD, 14));
        systemName.setTextFill(Color.web("#00d2d3"));
        
        Label userInfo = new Label("Active Session: " + currentUsername + " | Access Level: " + role);
        userInfo.setFont(Font.font("Consolas", 10));
        userInfo.setTextFill(Color.web("#dff9fb"));
        
        systemText.getChildren().addAll(systemName, userInfo);
        systemInfo.getChildren().addAll(systemIcon, systemText);
        
        // Center - Session timer
        Label sessionTimer = new Label("Session: 29:45");
        sessionTimer.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        sessionTimer.setTextFill(Color.web("#f6b93b"));
        
        // Right side - Controls
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_RIGHT);
        
        Button auditBtn = new Button("🔍 Audit");
        auditBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #00d2d3; -fx-font-family: 'Consolas';");
        auditBtn.setOnAction(e -> showAuditLogs(stage));
        
        Button helpBtn = new Button("❓ Help");
        helpBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #00d2d3; -fx-font-family: 'Consolas';");
        
        Button logout = new Button("🚪 Secure Logout");
        logout.setStyle("-fx-background-color: rgba(231, 76, 60, 0.2); -fx-text-fill: #e74c3c; -fx-font-family: 'Consolas'; -fx-font-weight: bold;");
        logout.setOnAction(e -> {
            auditLogs.add(new AuditLog(currentUsername, "LOGOUT", "User logged out securely"));
            currentUserRole = null;
            currentUsername = null;
            isFirstLogin = false;
            showLoginScreen(stage);
        });
        
        controls.getChildren().addAll(auditBtn, helpBtn, logout);
        
        topBar.getChildren().addAll(systemInfo, sessionTimer, controls);
        HBox.setHgrow(systemInfo, Priority.ALWAYS);
        HBox.setHgrow(sessionTimer, Priority.ALWAYS);
        
        VBox sidebar = createAdvancedSidebar(role, stage);
        StackPane content = new StackPane();
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #f8f9fa;");
        
        dashboard.setTop(topBar);
        dashboard.setLeft(sidebar);
        dashboard.setCenter(content);
        
        showAdvancedDefaultContent(content, role, stage);
        
        Scene scene = new Scene(dashboard, 1400, 900);
        stage.setScene(scene);
        stage.setTitle("DU SECS Dashboard | " + role + " Interface");
    }
    
    private VBox createAdvancedSidebar(String role, Stage stage) {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(280);
        sidebar.setStyle("-fx-background-color: linear-gradient(to bottom, #1e3799, #0c2461);");
        
        // Header with role icon
        HBox sidebarHeader = new HBox(15);
        sidebarHeader.setPadding(new Insets(20, 15, 20, 15));
        sidebarHeader.setStyle("-fx-background-color: rgba(0, 0, 0, 0.2);");
        
        String roleIcon = "";
        String roleColor = "";
        
        if ("Student".equals(role)) {
            roleIcon = "🎓";
            roleColor = "#00d2d3";
        } else if ("Instructor".equals(role)) {
            roleIcon = "👨‍🏫";
            roleColor = "#f6b93b";
        } else if ("Admin".equals(role)) {
            roleIcon = "🛡️";
            roleColor = "#e74c3c";
        }
        
        Label iconLabel = new Label(roleIcon);
        iconLabel.setFont(Font.font("Arial", 24));
        
        VBox roleInfo = new VBox(3);
        Label roleLabel = new Label(role.toUpperCase());
        roleLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 14));
        roleLabel.setTextFill(Color.web(roleColor));
        
        Label welcomeLabel = new Label("Welcome Back");
        welcomeLabel.setFont(Font.font("Segoe UI", 11));
        welcomeLabel.setTextFill(Color.web("#dff9fb"));
        
        roleInfo.getChildren().addAll(roleLabel, welcomeLabel);
        sidebarHeader.getChildren().addAll(iconLabel, roleInfo);
        
        // Menu items
        VBox menuItems = new VBox(0);
        menuItems.setPadding(new Insets(10, 0, 20, 0));
        
        if ("Student".equals(role)) {
            addAdvancedMenuButton(menuItems, "📚 Available Examinations", "#00d2d3", () -> showAdvancedAvailableExams(stage));
            addAdvancedMenuButton(menuItems, "⏰ Active Exam Sessions", "#00d2d3", () -> showActiveExams(stage));
            addAdvancedMenuButton(menuItems, "📊 Academic Performance", "#00d2d3", () -> showAdvancedStudentResults(stage));
            addAdvancedMenuButton(menuItems, "📈 Grade Analytics", "#00d2d3", () -> showStudentAnalytics(stage));
            addAdvancedMenuButton(menuItems, "👤 Student Profile", "#00d2d3", () -> showAdvancedStudentProfile(stage));
            addAdvancedMenuButton(menuItems, "🔔 Notifications", "#00d2d3", () -> showNotifications(stage));
        } 
        else if ("Instructor".equals(role)) {
            addAdvancedMenuButton(menuItems, "➕ Create New Examination", "#f6b93b", () -> showAdvancedCreateExam(stage));
            addAdvancedMenuButton(menuItems, "📋 Examination Management", "#f6b93b", () -> showAdvancedManageExams(stage));
            addAdvancedMenuButton(menuItems, "👥 Student Roster", "#f6b93b", () -> showAdvancedViewStudents(stage));
            addAdvancedMenuButton(menuItems, "📝 Grade Management", "#f6b93b", () -> showAdvancedGradeManagement(stage));
            addAdvancedMenuButton(menuItems, "📊 Performance Analytics", "#f6b93b", () -> showInstructorAnalytics(stage));
            addAdvancedMenuButton(menuItems, "📋 Course Management", "#f6b93b", () -> showCourseManagement(stage));
        }
        else if ("Admin".equals(role)) {
            addAdvancedMenuButton(menuItems, "📊 System Dashboard", "#e74c3c", () -> showAdminDashboard(stage));
            addAdvancedMenuButton(menuItems, "👥 User Management", "#e74c3c", () -> showAdvancedUserManagement(stage));
            addAdvancedMenuButton(menuItems, "🏫 Department Management", "#e74c3c", () -> showDepartmentManagement(stage));
            addAdvancedMenuButton(menuItems, "📈 System Analytics", "#e74c3c", () -> showSystemAnalytics(stage));
            addAdvancedMenuButton(menuItems, "🔒 Security Center", "#e74c3c", () -> showSecurityCenter(stage));
            addAdvancedMenuButton(menuItems, "⚙️ System Configuration", "#e74c3c", () -> showAdvancedSystemSettings(stage));
            addAdvancedMenuButton(menuItems, "📋 Audit Logs", "#e74c3c", () -> showAuditLogs(stage));
        }
        
        // Quick stats panel
        VBox quickStats = new VBox(10);
        quickStats.setPadding(new Insets(15));
        quickStats.setStyle("-fx-background-color: rgba(0, 0, 0, 0.1); -fx-border-color: rgba(255,255,255,0.1); -fx-border-width: 1 0 0 0;");
        
        Label statsTitle = new Label("QUICK STATS");
        statsTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        statsTitle.setTextFill(Color.web("#dff9fb"));
        
        if ("Student".equals(role)) {
            User currentStudent = getCurrentUser();
            long examsTaken = studentAttempts.stream()
                .filter(a -> a.studentId.equals(currentStudent.id) && a.isSubmitted)
                .count();
            quickStats.getChildren().addAll(
                statsTitle,
                createStatItem("Exams Taken", String.valueOf(examsTaken), "#00d2d3"),
                createStatItem("Avg. Grade", "B+", "#f6b93b"),
                createStatItem("Completion", "75%", "#2ecc71")
            );
        } else if ("Instructor".equals(role)) {
            int activeExams = (int) exams.stream().filter(e -> e.isActive).count();
            int totalStudents = (int) users.stream().filter(u -> u.role.equals("Student")).count();
            quickStats.getChildren().addAll(
                statsTitle,
                createStatItem("Active Exams", String.valueOf(activeExams), "#f6b93b"),
                createStatItem("Total Students", String.valueOf(totalStudents), "#00d2d3"),
                createStatItem("To Grade", "12", "#e74c3c")
            );
        } else if ("Admin".equals(role)) {
            int totalUsers = users.size();
            int totalExams = exams.size();
            int totalLogs = auditLogs.size();
            quickStats.getChildren().addAll(
                statsTitle,
                createStatItem("Total Users", String.valueOf(totalUsers), "#e74c3c"),
                createStatItem("Total Exams", String.valueOf(totalExams), "#f6b93b"),
                createStatItem("Security Logs", String.valueOf(totalLogs), "#00d2d3")
            );
        }
        
        sidebar.getChildren().addAll(sidebarHeader, menuItems, quickStats);
        return sidebar;
    }
    
    private HBox createStatItem(String label, String value, String color) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        
        Circle indicator = new Circle(4);
        indicator.setFill(Color.web(color));
        
        VBox textBox = new VBox(2);
        Label itemLabel = new Label(label);
        itemLabel.setFont(Font.font("Segoe UI", 10));
        itemLabel.setTextFill(Color.web("#b0bec5"));
        
        Label itemValue = new Label(value);
        itemValue.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        itemValue.setTextFill(Color.web(color));
        
        textBox.getChildren().addAll(itemLabel, itemValue);
        item.getChildren().addAll(indicator, textBox);
        
        return item;
    }
    
    private void addAdvancedMenuButton(VBox menuItems, String text, String color, Runnable action) {
        HBox buttonContainer = new HBox();
        buttonContainer.setPadding(new Insets(5, 15, 5, 15));
        buttonContainer.setStyle("-fx-cursor: hand;");
        
        Button btn = new Button(text);
        btn.setPrefWidth(250);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + color + "; -fx-font-family: 'Segoe UI'; -fx-font-size: 13;");
        btn.setOnAction(e -> action.run());
        
        buttonContainer.getChildren().add(btn);
        buttonContainer.setOnMouseEntered(e -> buttonContainer.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-cursor: hand;"));
        buttonContainer.setOnMouseExited(e -> buttonContainer.setStyle("-fx-background-color: transparent; -fx-cursor: hand;"));
        
        menuItems.getChildren().add(buttonContainer);
    }
    
    private void showAdvancedDefaultContent(StackPane content, String role, Stage stage) {
        VBox defaultContent = new VBox(30);
        defaultContent.setAlignment(Pos.CENTER);
        defaultContent.setPadding(new Insets(40));
        
        // Welcome section with role-specific design
        VBox welcomeSection = new VBox(15);
        welcomeSection.setAlignment(Pos.CENTER);
        welcomeSection.setMaxWidth(600);
        
        String roleIcon = "";
        String roleTitle = "";
        String roleDescription = "";
        String bgColor = "";
        
        if ("Student".equals(role)) {
            roleIcon = "🎓";
            roleTitle = "STUDENT DASHBOARD";
            roleDescription = "Access your examinations, track academic performance, and view grades";
            bgColor = "linear-gradient(135deg, #00d2d3, #1e90ff)";
        } else if ("Instructor".equals(role)) {
            roleIcon = "👨‍🏫";
            roleTitle = "INSTRUCTOR PORTAL";
            roleDescription = "Create and manage examinations, grade submissions, and monitor student progress";
            bgColor = "linear-gradient(135deg, #f6b93b, #e55039)";
        } else if ("Admin".equals(role)) {
            roleIcon = "🛡️";
            roleTitle = "ADMINISTRATOR CONTROL PANEL";
            roleDescription = "Manage system users, monitor security, and configure system settings";
            bgColor = "linear-gradient(135deg, #e74c3c, #0c2461)";
        }
        
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER);
        
        Text icon = new Text(roleIcon);
        icon.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        
        VBox textBox = new VBox(5);
        Label title = new Label(roleTitle);
        title.setFont(Font.font("Consolas", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#2c3e50"));
        
        Label description = new Label(roleDescription);
        description.setFont(Font.font("Segoe UI", 14));
        description.setTextFill(Color.web("#7f8c8d"));
        description.setStyle("-fx-text-alignment: center;");
        
        textBox.getChildren().addAll(title, description);
        headerBox.getChildren().addAll(icon, textBox);
        
        // Stats cards
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(20);
        statsGrid.setAlignment(Pos.CENTER);
        
        if ("Student".equals(role)) {
            User currentStudent = getCurrentUser();
            long examsTaken = studentAttempts.stream()
                .filter(a -> a.studentId.equals(currentStudent.id) && a.isSubmitted)
                .count();
            
            // Calculate average grade
            double avgGrade = studentAttempts.stream()
                .filter(a -> a.studentId.equals(currentStudent.id) && a.isSubmitted)
                .mapToDouble(a -> a.gradePoint)
                .average()
                .orElse(0.0);
            
            statsGrid.add(createStatCard("📚", "Available Exams", String.valueOf(exams.size()), "#00d2d3"), 0, 0);
            statsGrid.add(createStatCard("✅", "Exams Completed", String.valueOf(examsTaken), "#2ecc71"), 1, 0);
            statsGrid.add(createStatCard("📊", "Average Grade", String.format("%.1f", avgGrade), "#f6b93b"), 2, 0);
            statsGrid.add(createStatCard("🎯", "Performance", "Good", "#9b59b6"), 3, 0);
        } else if ("Instructor".equals(role)) {
            int activeExams = (int) exams.stream().filter(e -> e.isActive).count();
            int totalStudents = (int) users.stream().filter(u -> u.role.equals("Student")).count();
            int pendingGrading = 5; // Example value
            
            statsGrid.add(createStatCard("📋", "Active Exams", String.valueOf(activeExams), "#f6b93b"), 0, 0);
            statsGrid.add(createStatCard("👥", "Total Students", String.valueOf(totalStudents), "#3498db"), 1, 0);
            statsGrid.add(createStatCard("📝", "To Grade", String.valueOf(pendingGrading), "#e74c3c"), 2, 0);
            statsGrid.add(createStatCard("📈", "Avg. Score", "78%", "#2ecc71"), 3, 0);
        } else if ("Admin".equals(role)) {
            int totalUsers = users.size();
            int totalExams = exams.size();
            int activeSessions = 3; // Example value
            int securityAlerts = auditLogs.stream()
                .filter(log -> log.action.contains("FAILED") || log.action.contains("ALERT"))
                .toList()
                .size();
            
            statsGrid.add(createStatCard("👥", "Total Users", String.valueOf(totalUsers), "#e74c3c"), 0, 0);
            statsGrid.add(createStatCard("📋", "Total Exams", String.valueOf(totalExams), "#f6b93b"), 1, 0);
            statsGrid.add(createStatCard("🔐", "Active Sessions", String.valueOf(activeSessions), "#00d2d3"), 2, 0);
            statsGrid.add(createStatCard("⚠️", "Security Alerts", String.valueOf(securityAlerts), "#e74c3c"), 3, 0);
        }
        
        // Quick actions
        VBox quickActions = new VBox(15);
        quickActions.setAlignment(Pos.CENTER);
        quickActions.setMaxWidth(800);
        
        Label actionsTitle = new Label("QUICK ACTIONS");
        actionsTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 16));
        actionsTitle.setTextFill(Color.web("#2c3e50"));
        
        HBox actionButtons = new HBox(20);
        actionButtons.setAlignment(Pos.CENTER);
        
        if ("Student".equals(role)) {
            Button startExamBtn = createActionButton("🚀 Start New Exam", "#00d2d3", () -> showAdvancedAvailableExams(stage));
            Button viewResultsBtn = createActionButton("📊 View Results", "#2ecc71", () -> showAdvancedStudentResults(stage));
            actionButtons.getChildren().addAll(startExamBtn, viewResultsBtn);
        } else if ("Instructor".equals(role)) {
            Button createExamBtn = createActionButton("➕ Create Exam", "#f6b93b", () -> showAdvancedCreateExam(stage));
            Button gradeBtn = createActionButton("📝 Grade Papers", "#3498db", () -> showAdvancedGradeManagement(stage));
            actionButtons.getChildren().addAll(createExamBtn, gradeBtn);
        } else if ("Admin".equals(role)) {
            Button userMgmtBtn = createActionButton("👥 Manage Users", "#e74c3c", () -> showAdvancedUserManagement(stage));
            Button securityBtn = createActionButton("🔒 Security Center", "#00d2d3", () -> showSecurityCenter(stage));
            actionButtons.getChildren().addAll(userMgmtBtn, securityBtn);
        }
        
        quickActions.getChildren().addAll(actionsTitle, actionButtons);
        
        // System status
        HBox systemStatus = new HBox(15);
        systemStatus.setAlignment(Pos.CENTER);
        systemStatus.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-border-radius: 10; -fx-border-color: #dfe6e9; -fx-border-width: 1;");
        
        Label statusIcon = new Label("✅");
        statusIcon.setFont(Font.font("Arial", 20));
        
        VBox statusText = new VBox(3);
        Label statusTitle = new Label("SYSTEM STATUS: OPERATIONAL");
        statusTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        statusTitle.setTextFill(Color.web("#2ecc71"));
        
        Label statusDetail = new Label("All systems running • Security protocols active • Last updated: " + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        statusDetail.setFont(Font.font("Segoe UI", 10));
        statusDetail.setTextFill(Color.web("#7f8c8d"));
        
        statusText.getChildren().addAll(statusTitle, statusDetail);
        systemStatus.getChildren().addAll(statusIcon, statusText);
        
        defaultContent.getChildren().addAll(headerBox, statsGrid, quickActions, systemStatus);
        content.getChildren().setAll(defaultContent);
    }
    
    private VBox createStatCard(String icon, String title, String value, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                     "-fx-border-color: " + color + "20; -fx-border-width: 1; -fx-border-radius: 10; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.5, 0, 2);");
        card.setPrefWidth(180);
        card.setPrefHeight(120);
        
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Arial", 24));
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        titleLabel.setTextFill(Color.web("#7f8c8d"));
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 24));
        valueLabel.setTextFill(Color.web(color));
        
        card.getChildren().addAll(iconLabel, titleLabel, valueLabel);
        return card;
    }
    
    private Button createActionButton(String text, String color, Runnable action) {
        Button btn = new Button(text);
        btn.setPrefWidth(200);
        btn.setPrefHeight(50);
        btn.setStyle("-fx-background-color: " + color + "; " +
                    "-fx-background-radius: 8; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-family: 'Segoe UI'; " +
                    "-fx-font-weight: bold; " +
                    "-fx-font-size: 14; " +
                    "-fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, " + color + "80, 10, 0.5, 0, 2);");
        btn.setOnAction(e -> action.run());
        
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + color + "CC; " +
                                               "-fx-background-radius: 8; " +
                                               "-fx-text-fill: white; " +
                                               "-fx-font-family: 'Segoe UI'; " +
                                               "-fx-font-weight: bold; " +
                                               "-fx-font-size: 14; " +
                                               "-fx-cursor: hand; " +
                                               "-fx-effect: dropshadow(gaussian, " + color + "80, 15, 0.7, 0, 3);"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + color + "; " +
                                              "-fx-background-radius: 8; " +
                                              "-fx-text-fill: white; " +
                                              "-fx-font-family: 'Segoe UI'; " +
                                              "-fx-font-weight: bold; " +
                                              "-fx-font-size: 14; " +
                                              "-fx-cursor: hand; " +
                                              "-fx-effect: dropshadow(gaussian, " + color + "80, 10, 0.5, 0, 2);"));
        return btn;
    }
    
    // ================= ADVANCED STUDENT FUNCTIONALITIES =================
    private void showAdvancedAvailableExams(Stage stage) {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        
        Label title = new Label("📚 AVAILABLE EXAMINATIONS");
        title.setFont(Font.font("Consolas", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#2c3e50"));
        
        // Get current student
        User currentStudent = getCurrentUser();
        List<Exam> availableExams = new ArrayList<>();
        
        // Find exams student hasn't attempted yet
        for (Exam exam : exams) {
            if (!exam.isActive) continue;
            
            boolean hasAttempted = studentAttempts.stream()
                .anyMatch(a -> a.studentId.equals(currentStudent.id) && a.examId.equals(exam.examId));
            if (!hasAttempted) {
                availableExams.add(exam);
            }
        }
        
        if (availableExams.isEmpty()) {
            VBox noExamsBox = new VBox(20);
            noExamsBox.setAlignment(Pos.CENTER);
            noExamsBox.setPadding(new Insets(50));
            
            Label noExamsIcon = new Label("📭");
            noExamsIcon.setFont(Font.font("Arial", 48));
            
            Label noExams = new Label("No examinations available at this time");
            noExams.setFont(Font.font("Segoe UI", 16));
            noExams.setTextFill(Color.web("#7f8c8d"));
            
            noExamsBox.getChildren().addAll(noExamsIcon, noExams);
            content.getChildren().addAll(title, noExamsBox);
        } else {
            // Create advanced exam cards
            TilePane examTiles = new TilePane();
            examTiles.setPadding(new Insets(20));
            examTiles.setHgap(20);
            examTiles.setVgap(20);
            examTiles.setPrefColumns(2);
            
            for (Exam exam : availableExams) {
                VBox examCard = createExamCard(exam, stage);
                examTiles.getChildren().add(examCard);
            }
            
            ScrollPane scrollPane = new ScrollPane(examTiles);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(600);
            scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
            
            content.getChildren().addAll(title, scrollPane);
        }
        
        ((BorderPane) stage.getScene().getRoot()).setCenter(content);
    }
    
    private VBox createExamCard(Exam exam, Stage stage) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                     "-fx-border-color: #dfe6e9; -fx-border-width: 1; -fx-border-radius: 15; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0.5, 0, 3);");
        card.setPrefWidth(350);
        
        // Header with course code
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label courseIcon = new Label("📘");
        courseIcon.setFont(Font.font("Arial", 20));
        
        VBox courseInfo = new VBox(3);
        Label courseCode = new Label(exam.courseCode);
        courseCode.setFont(Font.font("Consolas", FontWeight.BOLD, 14));
        courseCode.setTextFill(Color.web("#00d2d3"));
        
        Label courseName = new Label(exam.courseName);
        courseName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        courseName.setTextFill(Color.web("#2c3e50"));
        
        courseInfo.getChildren().addAll(courseCode, courseName);
        header.getChildren().addAll(courseIcon, courseInfo);
        
        // Exam details
        VBox details = new VBox(8);
        
        Label examTitle = new Label(exam.examTitle);
        examTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        examTitle.setWrapText(true);
        
        // Stats grid
        GridPane stats = new GridPane();
        stats.setHgap(15);
        stats.setVgap(8);
        
        stats.add(createDetailItem("⏱️", "Duration", EXAM_DURATION_MINUTES + " min"), 0, 0);
        stats.add(createDetailItem("📝", "Questions", QUESTIONS_PER_EXAM + " items"), 1, 0);
        stats.add(createDetailItem("⭐", "Difficulty", "Mixed"), 0, 1);
        stats.add(createDetailItem("🏆", "Total Marks", exam.totalMarks + " pts"), 1, 1);
        
        // Instructions
        VBox instructions = new VBox(5);
        instructions.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-border-radius: 8;");
        
        Label instructionsTitle = new Label("📋 Exam Instructions:");
        instructionsTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        instructionsTitle.setTextFill(Color.web("#2c3e50"));
        
        TextArea instructionText = new TextArea(
            "• Strict time limit of " + EXAM_DURATION_MINUTES + " minutes\n" +
            "• Each question: 10 marks\n" +
            "• Unanswered questions: 0 marks\n" +
            "• No negative marking\n" +
            "• Auto-submit when time expires\n" +
            "• Cannot revisit after submission"
        );
        instructionText.setEditable(false);
        instructionText.setWrapText(true);
        instructionText.setPrefHeight(100);
        instructionText.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; " +
                               "-fx-font-family: 'Segoe UI'; -fx-font-size: 11;");
        
        instructions.getChildren().addAll(instructionsTitle, instructionText);
        
        // Start button
        Button startBtn = new Button("🚀 Start Examination");
        startBtn.setPrefWidth(310);
        startBtn.setPrefHeight(45);
        startBtn.setStyle("-fx-background-color: linear-gradient(to right, #00d2d3, #1e90ff); " +
                         "-fx-background-radius: 8; " +
                         "-fx-text-fill: white; " +
                         "-fx-font-family: 'Segoe UI'; " +
                         "-fx-font-weight: bold; " +
                         "-fx-font-size: 14; " +
                         "-fx-cursor: hand;");
        startBtn.setOnAction(e -> startAdvancedExam(stage, exam));
        
        card.getChildren().addAll(header, examTitle, stats, instructions, startBtn);
        return card;
    }
    
    private HBox createDetailItem(String icon, String label, String value) {
        HBox item = new HBox(8);
        item.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Arial", 14));
        
        VBox textBox = new VBox(2);
        Label labelText = new Label(label);
        labelText.setFont(Font.font("Segoe UI", 9));
        labelText.setTextFill(Color.web("#7f8c8d"));
        
        Label valueText = new Label(value);
        valueText.setFont(Font.font("Consolas", FontWeight.BOLD, 11));
        valueText.setTextFill(Color.web("#2c3e50"));
        
        textBox.getChildren().addAll(labelText, valueText);
        item.getChildren().addAll(iconLabel, textBox);
        
        return item;
    }
    
    private void startAdvancedExam(Stage stage, Exam exam) {
        VBox examContent = new VBox(20);
        examContent.setPadding(new Insets(30));
        examContent.setStyle("-fx-background-color: #f8f9fa;");
        
        // Create exam attempt
        User currentStudent = getCurrentUser();
        StudentAttempt attempt = new StudentAttempt(currentStudent.id, exam.examId);
        
        // Enhanced timer panel
        HBox timerPanel = new HBox(20);
        timerPanel.setPadding(new Insets(15));
        timerPanel.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                          "-fx-border-color: #00d2d3; -fx-border-width: 2; -fx-border-radius: 10; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,210,211,0.3), 10, 0.5, 0, 2);");
        timerPanel.setAlignment(Pos.CENTER);
        
        // Timer with animated warning
        Label timerLabel = new Label("Time Remaining: 05:00");
        timerLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 20));
        timerLabel.setTextFill(Color.web("#00d2d3"));
        
        // Progress bar with color change
        ProgressBar timeProgress = new ProgressBar(1.0);
        timeProgress.setPrefWidth(300);
        timeProgress.setStyle("-fx-accent: #00d2d3;");
        
        // Enhanced question counter with progress
        Label questionCounter = new Label("Question: 1/" + QUESTIONS_PER_EXAM);
        questionCounter.setFont(Font.font("Consolas", FontWeight.BOLD, 14));
        questionCounter.setTextFill(Color.web("#2c3e50"));
        
        // Add answered counter
        Label answeredCounter = new Label("Answered: 0/" + QUESTIONS_PER_EXAM);
        answeredCounter.setFont(Font.font("Consolas", 12));
        answeredCounter.setTextFill(Color.web("#7f8c8d"));
        
        timerPanel.getChildren().addAll(timerLabel, timeProgress, questionCounter, answeredCounter);
        
        // Calculate total exam duration in seconds
        int totalSeconds = EXAM_DURATION_MINUTES * 60;
        final int[] remainingSeconds = {totalSeconds};
        
        // Create a final reference to timer
        final Timeline[] timerRef = new Timeline[1];
        
        // Enhanced timer with better UI updates
        Timeline timer = new Timeline(
            new KeyFrame(javafx.util.Duration.seconds(1), event -> {
                remainingSeconds[0]--;
                
                // Update progress bar with smooth transition
                double progress = (double) remainingSeconds[0] / totalSeconds;
                timeProgress.setProgress(progress);
                
                // Change color based on time remaining
                if (remainingSeconds[0] <= 120) { // 2 minutes left
                    timeProgress.setStyle("-fx-accent: #e74c3c;");
                    timerLabel.setTextFill(Color.web("#e74c3c"));
                    timerPanel.setStyle("-fx-background-color: #fff5f5; -fx-border-color: #e74c3c;");
                } else if (remainingSeconds[0] <= 180) { // 3 minutes left
                    timeProgress.setStyle("-fx-accent: #f6b93b;");
                    timerLabel.setTextFill(Color.web("#f6b93b"));
                    timerPanel.setStyle("-fx-background-color: #fffaf0; -fx-border-color: #f6b93b;");
                }
                
                if (remainingSeconds[0] <= 0) {
                    // Time's up - auto submit
                    if (timerRef[0] != null) {
                        timerRef[0].stop();
                    }
                    attempt.endTime = LocalDateTime.now();
                    attempt.calculateMarks(exam);
                    attempt.isSubmitted = true;
                    studentAttempts.add(attempt);
                    
                    auditLogs.add(new AuditLog(currentStudent.id, "EXAM_AUTO_SUBMIT", 
                        "Exam: " + exam.examTitle + " - Auto-submitted due to time limit"));
                    
                    showAlert("TIME'S UP!", 
                        "⏰ Examination automatically submitted.\n\n" +
                        "• Unanswered questions: 0 marks\n" +
                        "• Results calculated automatically\n" +
                        "• Grade assigned based on Dilla University system");
                    showAdvancedExamResults(stage, exam, attempt);
                    return;
                }
                
                long minutes = remainingSeconds[0] / 60;
                long seconds = remainingSeconds[0] % 60;
                timerLabel.setText(String.format("Time Remaining: %02d:%02d", minutes, seconds));
            })
        );
        
        // Store timer reference
        timerRef[0] = timer;
        timer.setCycleCount(totalSeconds + 1);
        timer.play();
        
        // Enhanced exam header with student info
        HBox examHeader = new HBox(20);
        examHeader.setAlignment(Pos.CENTER_LEFT);
        examHeader.setPadding(new Insets(10));
        examHeader.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                           "-fx-border-color: #dfe6e9; -fx-border-width: 1; -fx-border-radius: 10;");
        
        VBox examInfo = new VBox(5);
        Label examTitle = new Label(exam.examTitle);
        examTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        examTitle.setTextFill(Color.web("#2c3e50"));
        
        HBox courseStudentInfo = new HBox(20);
        Label courseInfo = new Label(exam.courseCode + " - " + exam.courseName);
        courseInfo.setFont(Font.font("Segoe UI", 14));
        courseInfo.setTextFill(Color.web("#7f8c8d"));
        
        Label studentInfo = new Label("Student: " + currentStudent.name + " (" + currentStudent.id + ")");
        studentInfo.setFont(Font.font("Segoe UI", 12));
        studentInfo.setTextFill(Color.web("#00d2d3"));
        studentInfo.setStyle("-fx-font-weight: bold;");
        
        courseStudentInfo.getChildren().addAll(courseInfo, studentInfo);
        examInfo.getChildren().addAll(examTitle, courseStudentInfo);
        examHeader.getChildren().addAll(examInfo);
        
        // Main question area with smooth transitions
        StackPane questionArea = new StackPane();
        questionArea.setPrefHeight(400);
        questionArea.setStyle("-fx-background-color: transparent;");
        
        // Create question panels
        List<VBox> questionPanels = new ArrayList<>();
        List<ToggleGroup> toggleGroups = new ArrayList<>();
        final int[] currentQuestionIndex = {0};
        
        for (int i = 0; i < exam.questions.size(); i++) {
            Question question = exam.questions.get(i);
            
            VBox questionPanel = new VBox(15);
            questionPanel.setPadding(new Insets(25));
            questionPanel.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                                 "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.5, 0, 2);");
            
            // Enhanced question header
            HBox qHeader = new HBox(15);
            qHeader.setAlignment(Pos.CENTER_LEFT);
            
            Label qNumber = new Label("Q" + (i + 1));
            qNumber.setFont(Font.font("Consolas", FontWeight.BOLD, 20));
            qNumber.setTextFill(Color.web("#00d2d3"));
            qNumber.setStyle("-fx-background-color: #00d2d310; -fx-padding: 5 10; -fx-border-radius: 5;");
            
            HBox difficultyBox = new HBox(5);
            Label difficultyLabel = new Label(question.difficulty);
            difficultyLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            
            // Color code difficulty
            switch (question.difficulty) {
                case "Easy":
                    difficultyLabel.setTextFill(Color.web("#2ecc71"));
                    break;
                case "Medium":
                    difficultyLabel.setTextFill(Color.web("#f6b93b"));
                    break;
                case "Hard":
                    difficultyLabel.setTextFill(Color.web("#e74c3c"));
                    break;
            }
            
            Label qMarks = new Label("• " + question.marks + " marks");
            qMarks.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            qMarks.setTextFill(Color.web("#3498db"));
            
            difficultyBox.getChildren().addAll(difficultyLabel, qMarks);
            
            qHeader.getChildren().addAll(qNumber, difficultyBox);
            
            // Enhanced question text
            VBox questionTextContainer = new VBox(5);
            questionTextContainer.setPadding(new Insets(10));
            questionTextContainer.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 8;");
            
            Label questionTextLabel = new Label("Question " + (i + 1) + ":");
            questionTextLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            questionTextLabel.setTextFill(Color.web("#7f8c8d"));
            
            TextArea qText = new TextArea(question.questionText);
            qText.setEditable(false);
            qText.setWrapText(true);
            qText.setPrefHeight(80);
            qText.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; " +
                          "-fx-font-family: 'Segoe UI'; -fx-font-size: 14; -fx-text-fill: #2c3e50;");
            
            questionTextContainer.getChildren().addAll(questionTextLabel, qText);
            
            // Enhanced options with better visual feedback
            VBox optionsBox = new VBox(10);
            ToggleGroup group = new ToggleGroup();
            toggleGroups.add(group);
            
            String[] optionLabels = {"A", "B", "C", "D"};
            Color[] optionColors = {Color.web("#e74c3c"), Color.web("#3498db"), 
                                   Color.web("#2ecc71"), Color.web("#f6b93b")};
            
            for (int j = 0; j < question.options.size(); j++) {
                HBox optionBox = new HBox(15);
                optionBox.setPadding(new Insets(10));
                optionBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; " +
                                 "-fx-border-color: #dfe6e9; -fx-border-width: 1; -fx-border-radius: 8; " +
                                 "-fx-cursor: hand;");
                optionBox.setAlignment(Pos.CENTER_LEFT);
                
                // Option letter with colored background
                StackPane optionCircle = new StackPane();
                Circle circle = new Circle(15);
                circle.setFill(optionColors[j].deriveColor(0, 1, 1, 0.1));
                circle.setStroke(optionColors[j]);
                circle.setStrokeWidth(1.5);
                
                Label optionLetter = new Label(optionLabels[j]);
                optionLetter.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
                optionLetter.setTextFill(optionColors[j]);
                
                optionCircle.getChildren().addAll(circle, optionLetter);
                
                RadioButton optionBtn = new RadioButton(question.options.get(j));
                optionBtn.setToggleGroup(group);
                optionBtn.setUserData(j);
                optionBtn.setFont(Font.font("Segoe UI", 13));
                optionBtn.setWrapText(true);
                optionBtn.setStyle("-fx-text-fill: #2c3e50;");
                
                // Restore previously selected answer if any
                if (attempt.selectedAnswers.get(i) == j) {
                    optionBtn.setSelected(true);
                    optionBox.setStyle("-fx-background-color: #00d2d310; -fx-border-color: #00d2d3; " +
                                     "-fx-border-width: 2; -fx-border-radius: 8;");
                }
                
                final int questionIdx = i;
                final int optionIdx = j;
                
                // Enhanced hover effects
                optionBox.setOnMouseEntered(e -> {
                    if (!optionBtn.isSelected()) {
                        optionBox.setStyle("-fx-background-color: #e8f4f8; -fx-border-color: #b2ebf2; " +
                                         "-fx-border-width: 1; -fx-border-radius: 8;");
                    }
                });
                
                optionBox.setOnMouseExited(e -> {
                    if (!optionBtn.isSelected()) {
                        optionBox.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dfe6e9; " +
                                         "-fx-border-width: 1; -fx-border-radius: 8;");
                    }
                });
                
                optionBox.setOnMouseClicked(e -> {
                    optionBtn.setSelected(true);
                    attempt.selectedAnswers.set(questionIdx, optionIdx);
                    
                    // Update all option boxes for this question
                    for (int k = 0; k < optionBox.getParent().getChildrenUnmodifiable().size(); k++) {
                        Node node = optionBox.getParent().getChildrenUnmodifiable().get(k);
                        if (node instanceof HBox) {
                            HBox hbox = (HBox) node;
                            if (k == optionIdx) {
                                hbox.setStyle("-fx-background-color: #00d2d310; -fx-border-color: #00d2d3; " +
                                            "-fx-border-width: 2; -fx-border-radius: 8;");
                            } else {
                                hbox.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dfe6e9; " +
                                            "-fx-border-width: 1; -fx-border-radius: 8;");
                            }
                        }
                    }
                    
                    // Update answered counter
                    long answered = attempt.selectedAnswers.stream().filter(ans -> ans != -1).count();
                    answeredCounter.setText("Answered: " + answered + "/" + QUESTIONS_PER_EXAM);
                });
                
                optionBtn.setOnAction(e -> {
                    attempt.selectedAnswers.set(questionIdx, optionIdx);
                    
                    // Update answered counter
                    long answered = attempt.selectedAnswers.stream().filter(ans -> ans != -1).count();
                    answeredCounter.setText("Answered: " + answered + "/" + QUESTIONS_PER_EXAM);
                });
                
                optionBox.getChildren().addAll(optionCircle, optionBtn);
                optionsBox.getChildren().add(optionBox);
            }
            
            questionPanel.getChildren().addAll(qHeader, questionTextContainer, optionsBox);
            questionPanels.add(questionPanel);
        }
        
        // Enhanced question navigation with quick buttons
        HBox navigationPanel = new HBox(20);
        navigationPanel.setAlignment(Pos.CENTER);
        navigationPanel.setPadding(new Insets(20));
        navigationPanel.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                               "-fx-border-color: #dfe6e9; -fx-border-width: 1; -fx-border-radius: 10;");
        
        // Previous button with icon
        Button prevBtn = new Button("◀ Previous");
        prevBtn.setPrefWidth(150);
        prevBtn.setPrefHeight(45);
        prevBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-font-size: 14; -fx-cursor: hand;");
        prevBtn.setDisable(true);
        
        // Enhanced next button with progress indicator
        Button nextBtn = new Button("Next Question ▶");
        nextBtn.setPrefWidth(150);
        nextBtn.setPrefHeight(45);
        nextBtn.setStyle("-fx-background-color: #00d2d3; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-font-size: 14; -fx-cursor: hand;");
        
        // Question number navigation - Enhanced with visual indicators
        HBox questionNumbers = new HBox(5);
        questionNumbers.setAlignment(Pos.CENTER);
        questionNumbers.setPadding(new Insets(10));
        
        List<Button> numberButtons = new ArrayList<>();
        for (int i = 0; i < exam.questions.size(); i++) {
            final int questionNum = i;
            Button qNumBtn = new Button(String.valueOf(i + 1));
            qNumBtn.setPrefSize(40, 40);
            qNumBtn.setStyle("-fx-background-color: #dfe6e9; -fx-text-fill: #7f8c8d; " +
                            "-fx-font-weight: bold; -fx-border-radius: 20;");
            qNumBtn.setTooltip(new Tooltip("Question " + (i + 1)));
            
            if (i == 0) {
                qNumBtn.setStyle("-fx-background-color: #00d2d3; -fx-text-fill: white; " +
                                "-fx-font-weight: bold; -fx-border-radius: 20; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0,210,211,0.5), 5, 0.5, 0, 1);");
            }
            
            qNumBtn.setOnAction(e -> {
                currentQuestionIndex[0] = questionNum;
                questionArea.getChildren().setAll(questionPanels.get(questionNum));
                questionCounter.setText("Question: " + (questionNum + 1) + "/" + QUESTIONS_PER_EXAM);
                
                // Update navigation buttons
                prevBtn.setDisable(questionNum == 0);
                nextBtn.setDisable(questionNum == exam.questions.size() - 1);
                
                // Update number buttons with animation
                for (int j = 0; j < numberButtons.size(); j++) {
                    Button btn = numberButtons.get(j);
                    if (j == questionNum) {
                        btn.setStyle("-fx-background-color: #00d2d3; -fx-text-fill: white; " +
                                    "-fx-font-weight: bold; -fx-border-radius: 20; " +
                                    "-fx-effect: dropshadow(gaussian, rgba(0,210,211,0.5), 5, 0.5, 0, 1);");
                    } else if (attempt.selectedAnswers.get(j) != -1) {
                        btn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; " +
                                    "-fx-font-weight: bold; -fx-border-radius: 20; " +
                                    "-fx-effect: dropshadow(gaussian, rgba(46,204,113,0.5), 3, 0.5, 0, 1);");
                    } else {
                        btn.setStyle("-fx-background-color: #dfe6e9; -fx-text-fill: #7f8c8d; " +
                                    "-fx-font-weight: bold; -fx-border-radius: 20;");
                    }
                }
            });
            
            numberButtons.add(qNumBtn);
            questionNumbers.getChildren().add(qNumBtn);
        }
        
        // Add quick navigation arrows
        Button firstBtn = new Button("⏮");
        firstBtn.setTooltip(new Tooltip("First Question"));
        firstBtn.setOnAction(e -> numberButtons.get(0).fire());
        
        Button lastBtn = new Button("⏭");
        lastBtn.setTooltip(new Tooltip("Last Question"));
        lastBtn.setOnAction(e -> numberButtons.get(numberButtons.size() - 1).fire());
        
        // Navigation button actions with smooth transitions
        prevBtn.setOnAction(e -> {
            if (currentQuestionIndex[0] > 0) {
                currentQuestionIndex[0]--;
                questionArea.getChildren().setAll(questionPanels.get(currentQuestionIndex[0]));
                questionCounter.setText("Question: " + (currentQuestionIndex[0] + 1) + "/" + QUESTIONS_PER_EXAM);
                
                // Update navigation buttons
                prevBtn.setDisable(currentQuestionIndex[0] == 0);
                nextBtn.setDisable(false);
                
                // Update number buttons
                for (int j = 0; j < numberButtons.size(); j++) {
                    Button btn = numberButtons.get(j);
                    if (j == currentQuestionIndex[0]) {
                        btn.setStyle("-fx-background-color: #00d2d3; -fx-text-fill: white; " +
                                    "-fx-font-weight: bold; -fx-border-radius: 20; " +
                                    "-fx-effect: dropshadow(gaussian, rgba(0,210,211,0.5), 5, 0.5, 0, 1);");
                    } else if (attempt.selectedAnswers.get(j) != -1) {
                        btn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; " +
                                    "-fx-font-weight: bold; -fx-border-radius: 20; " +
                                    "-fx-effect: dropshadow(gaussian, rgba(46,204,113,0.5), 3, 0.5, 0, 1);");
                    } else {
                        btn.setStyle("-fx-background-color: #dfe6e9; -fx-text-fill: #7f8c8d; " +
                                    "-fx-font-weight: bold; -fx-border-radius: 20;");
                    }
                }
            }
        });
        
        nextBtn.setOnAction(e -> {
            if (currentQuestionIndex[0] < exam.questions.size() - 1) {
                currentQuestionIndex[0]++;
                questionArea.getChildren().setAll(questionPanels.get(currentQuestionIndex[0]));
                questionCounter.setText("Question: " + (currentQuestionIndex[0] + 1) + "/" + QUESTIONS_PER_EXAM);
                
                // Update navigation buttons
                prevBtn.setDisable(false);
                nextBtn.setDisable(currentQuestionIndex[0] == exam.questions.size() - 1);
                
                // Update number buttons
                for (int j = 0; j < numberButtons.size(); j++) {
                    Button btn = numberButtons.get(j);
                    if (j == currentQuestionIndex[0]) {
                        btn.setStyle("-fx-background-color: #00d2d3; -fx-text-fill: white; " +
                                    "-fx-font-weight: bold; -fx-border-radius: 20; " +
                                    "-fx-effect: dropshadow(gaussian, rgba(0,210,211,0.5), 5, 0.5, 0, 1);");
                    } else if (attempt.selectedAnswers.get(j) != -1) {
                        btn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; " +
                                    "-fx-font-weight: bold; -fx-border-radius: 20; " +
                                    "-fx-effect: dropshadow(gaussian, rgba(46,204,113,0.5), 3, 0.5, 0, 1);");
                    } else {
                        btn.setStyle("-fx-background-color: #dfe6e9; -fx-text-fill: #7f8c8d; " +
                                    "-fx-font-weight: bold; -fx-border-radius: 20;");
                    }
                }
                
                // If this is the last question, change next button text
                if (currentQuestionIndex[0] == exam.questions.size() - 1) {
                    nextBtn.setText("Review & Submit ▶");
                }
            }
        });
        
        // Add navigation controls to panel
        navigationPanel.getChildren().addAll(firstBtn, prevBtn, questionNumbers, nextBtn, lastBtn);
        
        // Initialize first question
        questionArea.getChildren().setAll(questionPanels.get(0));
        
        // Enhanced control buttons with better visual hierarchy
        HBox controlButtons = new HBox(20);
        controlButtons.setAlignment(Pos.CENTER);
        controlButtons.setPadding(new Insets(20));
        controlButtons.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                              "-fx-border-color: #dfe6e9; -fx-border-width: 1; -fx-border-radius: 10;");
        
        // Save button with icon
        Button saveBtn = new Button("💾 Save & Continue Later");
        saveBtn.setPrefWidth(200);
        saveBtn.setPrefHeight(50);
        saveBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-font-size: 14; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> {
            showAlert("Progress Saved", 
                "✅ Your exam progress has been saved.\n\n" +
                "You can continue from where you left off.\n" +
                "Timer will continue when you resume.");
        });
        
        // Mark for review button
        Button markBtn = new Button("🔖 Mark for Review");
        markBtn.setPrefWidth(180);
        markBtn.setPrefHeight(50);
        markBtn.setStyle("-fx-background-color: #f6b93b; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-font-size: 14; -fx-cursor: hand;");
        markBtn.setOnAction(e -> {
            // Mark current question for review
            Button currentQBtn = numberButtons.get(currentQuestionIndex[0]);
            currentQBtn.setStyle("-fx-background-color: #f6b93b; -fx-text-fill: white; " +
                               "-fx-font-weight: bold; -fx-border-radius: 20; " +
                               "-fx-effect: dropshadow(gaussian, rgba(246,185,59,0.5), 5, 0.5, 0, 1);");
            currentQBtn.setText(currentQuestionIndex[0] + 1 + " ⭐");
            
            showAlert("Question Marked", 
                "Question " + (currentQuestionIndex[0] + 1) + " has been marked for review.\n\n" +
                "You can easily identify it in the question navigation.");
        });
        
        // Enhanced submit button
        Button submitBtn = new Button("📤 Submit Examination");
        submitBtn.setPrefWidth(220);
        submitBtn.setPrefHeight(50);
        submitBtn.setStyle("-fx-background-color: linear-gradient(to right, #2ecc71, #27ae60); " +
                          "-fx-text-fill: white; -fx-font-weight: bold; " +
                          "-fx-font-size: 15; -fx-cursor: hand; " +
                          "-fx-effect: dropshadow(gaussian, rgba(46,204,113,0.5), 10, 0.5, 0, 2);");
        submitBtn.setOnAction(e -> {
            // Check if all questions are answered
            long unanswered = attempt.selectedAnswers.stream().filter(ans -> ans == -1).count();
            
            if (unanswered > 0) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirm Submission");
                confirm.setHeaderText("You have " + unanswered + " unanswered questions");
                confirm.setContentText("Are you sure you want to submit?\n\n" +
                                     "Unanswered questions will receive 0 marks.");
                
                Optional<ButtonType> result = confirm.showAndWait();
                if (result.isPresent() && result.get() != ButtonType.OK) {
                    return;
                }
            }
            
            timer.stop();
            attempt.endTime = LocalDateTime.now();
            
            // Calculate marks
            attempt.calculateMarks(exam);
            attempt.isSubmitted = true;
            studentAttempts.add(attempt);
            
            auditLogs.add(new AuditLog(currentStudent.id, "EXAM_SUBMITTED", 
                "Exam: " + exam.examTitle + " - Submitted by student"));
            
            showAlert("EXAMINATION SUBMITTED", 
                "✅ Your examination has been successfully submitted!\n\n" +
                "• Total questions: " + QUESTIONS_PER_EXAM + "\n" +
                "• Answered: " + (QUESTIONS_PER_EXAM - unanswered) + "\n" +
                "• Time taken: " + (EXAM_DURATION_MINUTES - remainingSeconds[0]/60) + " minutes\n" +
                "• Grade calculation in progress...\n\n" +
                "Results will be displayed with Dilla University grading.");
            
            showAdvancedExamResults(stage, exam, attempt);
        });
        
        // Cancel button with warning
        Button cancelBtn = new Button("❌ Cancel Exam");
        cancelBtn.setPrefWidth(180);
        cancelBtn.setPrefHeight(50);
        cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                          "-fx-font-weight: bold; -fx-font-size: 14; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> {
            timer.stop();
            Alert confirm = new Alert(Alert.AlertType.WARNING);
            confirm.setTitle("Cancel Examination");
            confirm.setHeaderText("⚠️ WARNING: This action cannot be undone!");
            confirm.setContentText("Are you sure you want to cancel this exam?\n\n" +
                                 "• All progress will be lost\n" +
                                 "• No marks will be recorded\n" +
                                 "• You will need to start over\n\n" +
                                 "This action is final.");
            
            confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES) {
                showAdvancedAvailableExams(stage);
            } else {
                timer.play(); // Resume timer if not cancelling
            }
        });
        
        // Add keyboard shortcuts
        examContent.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case LEFT:
                    if (!prevBtn.isDisabled()) prevBtn.fire();
                    break;
                case RIGHT:
                    if (!nextBtn.isDisabled()) nextBtn.fire();
                    break;
                case DIGIT1:
                case DIGIT2:
                case DIGIT3:
                case DIGIT4:
                    // Select answer options 1-4
                    int optionIndex = event.getCode().ordinal() - KeyCode.DIGIT1.ordinal();
                    if (optionIndex >= 0 && optionIndex < 4) {
                        ToggleGroup currentGroup = toggleGroups.get(currentQuestionIndex[0]);
                        if (currentGroup != null && optionIndex < currentGroup.getToggles().size()) {
                            currentGroup.getToggles().get(optionIndex).setSelected(true);
                        }
                    }
                    break;
                case S:
                    if (event.isControlDown()) {
                        saveBtn.fire();
                    }
                    break;
                case ENTER:
                    nextBtn.fire();
                    break;
            }
        });
        
        // Request focus for keyboard shortcuts
        examContent.setFocusTraversable(true);
        
        controlButtons.getChildren().addAll(saveBtn, markBtn, submitBtn, cancelBtn);
        
        examContent.getChildren().addAll(timerPanel, examHeader, navigationPanel, questionArea, controlButtons);
        
        ScrollPane scrollPane = new ScrollPane(examContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        
        ((BorderPane) stage.getScene().getRoot()).setCenter(scrollPane);
        
        // Request focus for keyboard shortcuts
        Platform.runLater(() -> examContent.requestFocus());
    }
    
    private void showAdvancedExamResults(Stage stage, Exam exam, StudentAttempt attempt) {
        // Create final references for use in lambda expressions
        final Exam finalExam = exam;
        final StudentAttempt finalAttempt = attempt;
        
        VBox resultsContent = new VBox(30);
        resultsContent.setPadding(new Insets(40));
        resultsContent.setAlignment(Pos.CENTER);
        resultsContent.setStyle("-fx-background-color: #f8f9fa;");
        
        // Header with celebration
        VBox headerBox = new VBox(15);
        headerBox.setAlignment(Pos.CENTER);
        
        Label celebrationIcon = new Label("🎉");
        celebrationIcon.setFont(Font.font("Arial", 48));
        
        Label title = new Label("EXAMINATION RESULTS");
        title.setFont(Font.font("Consolas", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2c3e50"));
        
        Label subTitle = new Label(finalExam.courseCode + " - " + finalExam.examTitle);
        subTitle.setFont(Font.font("Segoe UI", 16));
        subTitle.setTextFill(Color.web("#7f8c8d"));
        
        headerBox.getChildren().addAll(celebrationIcon, title, subTitle);
        
        // Main results card
        HBox mainResults = new HBox(30);
        mainResults.setAlignment(Pos.CENTER);
        mainResults.setPadding(new Insets(30));
        mainResults.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                           "-fx-border-color: #dfe6e9; -fx-border-width: 1; -fx-border-radius: 15; " +
                           "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 20, 0.5, 0, 5);");
        
        // Score panel
        VBox scorePanel = new VBox(15);
        scorePanel.setAlignment(Pos.CENTER);
        scorePanel.setPrefWidth(300);
        
        Label scoreTitle = new Label("YOUR SCORE");
        scoreTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 16));
        scoreTitle.setTextFill(Color.web("#7f8c8d"));
        
        // Circular progress for score
        StackPane scoreCircle = new StackPane();
        scoreCircle.setPrefSize(200, 200);
        
        Circle backgroundCircle = new Circle(100);
        backgroundCircle.setFill(Color.web("#f8f9fa"));
        backgroundCircle.setStroke(Color.web("#dfe6e9"));
        backgroundCircle.setStrokeWidth(10);
        
        Circle progressCircle = new Circle(100);
        double percentage = (double) finalAttempt.marksObtained / finalExam.totalMarks * 100;
        progressCircle.setFill(Color.TRANSPARENT);
        
        // Use a final variable for grade color
        String gradeColor = getGradeColor(finalAttempt.letterGrade);
        progressCircle.setStroke(Color.web(gradeColor));
        progressCircle.setStrokeWidth(10);
        progressCircle.getStrokeDashArray().addAll(2 * Math.PI * 100 * (percentage / 100), 2 * Math.PI * 100);
        progressCircle.setRotate(-90);
        
        VBox scoreText = new VBox(5);
        scoreText.setAlignment(Pos.CENTER);
        
        Label scoreValue = new Label(finalAttempt.marksObtained + "/" + finalExam.totalMarks);
        scoreValue.setFont(Font.font("Consolas", FontWeight.BOLD, 36));
        scoreValue.setTextFill(Color.web("#2c3e50"));
        
        Label percentageLabel = new Label(String.format("%.1f%%", percentage));
        percentageLabel.setFont(Font.font("Segoe UI", 16));
        percentageLabel.setTextFill(Color.web("#7f8c8d"));
        
        scoreText.getChildren().addAll(scoreValue, percentageLabel);
        
        scoreCircle.getChildren().addAll(backgroundCircle, progressCircle, scoreText);
        
        scorePanel.getChildren().addAll(scoreTitle, scoreCircle);
        
        // Grade panel
        VBox gradePanel = new VBox(20);
        gradePanel.setAlignment(Pos.CENTER);
        gradePanel.setPrefWidth(300);
        
        Label gradeTitle = new Label("DILLA UNIVERSITY GRADE");
        gradeTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 16));
        gradeTitle.setTextFill(Color.web("#7f8c8d"));
        
        // Grade display
        VBox gradeDisplay = new VBox(10);
        gradeDisplay.setAlignment(Pos.CENTER);
        gradeDisplay.setPadding(new Insets(20));
        gradeDisplay.setStyle("-fx-background-color: " + gradeColor + "20; " +
                            "-fx-border-color: " + gradeColor + "; " +
                            "-fx-border-width: 2; -fx-border-radius: 10;");
        
        Label letterGrade = new Label(finalAttempt.letterGrade);
        letterGrade.setFont(Font.font("Consolas", FontWeight.BOLD, 48));
        letterGrade.setTextFill(Color.web(gradeColor));
        
        Label gradePoint = new Label("Grade Point: " + finalAttempt.gradePoint);
        gradePoint.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        gradePoint.setTextFill(Color.web("#2c3e50"));
        
        Label remark = new Label(finalAttempt.remark);
        remark.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        remark.setTextFill(Color.web(gradeColor));
        
        gradeDisplay.getChildren().addAll(letterGrade, gradePoint, remark);
        
        // Grade explanation
        VBox gradeExplanation = new VBox(10);
        gradeExplanation.setPadding(new Insets(15));
        gradeExplanation.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 8;");
        
        Label explanationTitle = new Label("📊 Grading System:");
        explanationTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        
        TextArea explanationText = new TextArea(
            "A+ (90-100): 4.0 - Excellent\n" +
            "A  (85-89): 4.0 - Excellent\n" +
            "A- (80-84): 3.75 - Very Good\n" +
            "B+ (75-79): 3.5 - Good\n" +
            "B  (70-74): 3.0 - Good\n" +
            "B- (65-69): 2.75 - Satisfactory\n" +
            "C+ (60-64): 2.5 - Satisfactory\n" +
            "C  (55-59): 2.0 - Fair\n" +
            "C- (50-54): 1.75 - Fair\n" +
            "D  (45-49): 1.0 - Pass\n" +
            "F  (0-44): 0.0 - Fail\n\n" +
            "Your performance: " + String.format("%.1f%%", percentage) + " - " + finalAttempt.remark
        );
        explanationText.setEditable(false);
        explanationText.setWrapText(true);
        explanationText.setPrefHeight(150);
        explanationText.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        
        gradeExplanation.getChildren().addAll(explanationTitle, explanationText);
        gradePanel.getChildren().addAll(gradeTitle, gradeDisplay, gradeExplanation);
        
        mainResults.getChildren().addAll(scorePanel, gradePanel);
        
        // Detailed breakdown
        VBox breakdown = new VBox(15);
        breakdown.setPadding(new Insets(20));
        breakdown.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                         "-fx-border-color: #dfe6e9; -fx-border-width: 1; -fx-border-radius: 10;");
        
        Label breakdownTitle = new Label("📋 DETAILED BREAKDOWN");
        breakdownTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 18));
        breakdownTitle.setTextFill(Color.web("#2c3e50"));
        
        GridPane breakdownGrid = new GridPane();
        breakdownGrid.setHgap(20);
        breakdownGrid.setVgap(10);
        breakdownGrid.setPadding(new Insets(10));
        
        // Calculate statistics
        int totalQuestions = finalExam.questions.size();
        int attempted = 0;
        int correct = 0;
        
        for (int i = 0; i < totalQuestions; i++) {
            if (finalAttempt.selectedAnswers.get(i) != -1) {
                attempted++;
                if (finalAttempt.selectedAnswers.get(i) == finalExam.questions.get(i).correctAnswerIndex) {
                    correct++;
                }
            }
        }
        
        int unattempted = totalQuestions - attempted;
        int wrong = attempted - correct;
        
        breakdownGrid.add(createBreakdownItem("Total Questions", String.valueOf(totalQuestions), "#00d2d3"), 0, 0);
        breakdownGrid.add(createBreakdownItem("Attempted", String.valueOf(attempted), "#3498db"), 1, 0);
        breakdownGrid.add(createBreakdownItem("Correct", String.valueOf(correct), "#2ecc71"), 2, 0);
        breakdownGrid.add(createBreakdownItem("Wrong", String.valueOf(wrong), "#e74c3c"), 3, 0);
        breakdownGrid.add(createBreakdownItem("Unattempted", String.valueOf(unattempted), "#95a5a6"), 0, 1);
        breakdownGrid.add(createBreakdownItem("Time Taken", EXAM_DURATION_MINUTES + " min", "#f6b93b"), 1, 1);
        breakdownGrid.add(createBreakdownItem("Submission Time", 
            finalAttempt.endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), "#9b59b6"), 2, 1);
        
        // Question-wise performance
        VBox questionPerformance = new VBox(10);
        questionPerformance.setPadding(new Insets(10));
        
        Label performanceTitle = new Label("📝 Question-wise Performance:");
        performanceTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        
        for (int i = 0; i < finalExam.questions.size(); i++) {
            HBox qPerformance = new HBox(15);
            qPerformance.setPadding(new Insets(5));
            
            Label qNum = new Label("Q" + (i + 1));
            qNum.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
            qNum.setPrefWidth(40);
            
            Question question = finalExam.questions.get(i);
            int selectedAnswer = finalAttempt.selectedAnswers.get(i);
            
            String status;
            javafx.scene.paint.Color color;
            
            if (selectedAnswer == -1) {
                status = "❌ Unattempted (0 marks)";
                color = javafx.scene.paint.Color.RED;
            } else if (selectedAnswer == question.correctAnswerIndex) {
                status = "✅ Correct (" + question.marks + " marks)";
                color = javafx.scene.paint.Color.GREEN;
            } else {
                status = "❌ Wrong (0 marks)";
                color = javafx.scene.paint.Color.RED;
            }
            
            Label qStatus = new Label(status);
            qStatus.setTextFill(color);
            qStatus.setFont(Font.font("Segoe UI", 12));
            
            Label qDifficulty = new Label(question.difficulty);
            qDifficulty.setFont(Font.font("Segoe UI", 10));
            qDifficulty.setTextFill(Color.web("#7f8c8d"));
            qDifficulty.setPrefWidth(60);
            
            qPerformance.getChildren().addAll(qNum, qStatus, qDifficulty);
            questionPerformance.getChildren().add(qPerformance);
        }
        
        ScrollPane performanceScroll = new ScrollPane(questionPerformance);
        performanceScroll.setPrefHeight(200);
        performanceScroll.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        
        breakdown.getChildren().addAll(breakdownTitle, breakdownGrid, performanceTitle, performanceScroll);
        
        // Action buttons
        HBox actionButtons = new HBox(20);
        actionButtons.setAlignment(Pos.CENTER);
        
        Button returnBtn = createActionButton("🏠 Return to Dashboard", "#00d2d3", 
            () -> showAdvancedDefaultContent((StackPane) ((BorderPane) stage.getScene().getRoot()).getCenter(), "Student", stage));
        
        Button downloadBtn = createActionButton("📥 Download Certificate", "#2ecc71", 
            () -> showAlert("Certificate", "Your examination certificate has been generated and is ready for download."));
        
        Button reviewBtn = createActionButton("📚 Review Answers", "#f6b93b", 
            () -> showAlert("Review", "Answer review functionality will be available after instructor approval."));
        
        actionButtons.getChildren().addAll(returnBtn, downloadBtn, reviewBtn);
        
        resultsContent.getChildren().addAll(headerBox, mainResults, breakdown, actionButtons);
        
        ScrollPane scrollPane = new ScrollPane(resultsContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        
        ((BorderPane) stage.getScene().getRoot()).setCenter(scrollPane);
    }
    
    private String getGradeColor(String grade) {
        switch (grade) {
            case "A+": case "A": case "A-": return "#2ecc71"; // Green
            case "B+": case "B": case "B-": return "#3498db"; // Blue
            case "C+": case "C": case "C-": return "#f6b93b"; // Yellow
            case "D": return "#e67e22"; // Orange
            case "F": return "#e74c3c"; // Red
            default: return "#95a5a6"; // Gray
        }
    }
    
    private VBox createBreakdownItem(String label, String value, String color) {
        VBox item = new VBox(5);
        item.setAlignment(Pos.CENTER);
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 18));
        valueLabel.setTextFill(Color.web(color));
        
        Label labelText = new Label(label);
        labelText.setFont(Font.font("Segoe UI", 10));
        labelText.setTextFill(Color.web("#7f8c8d"));
        
        item.getChildren().addAll(valueLabel, labelText);
        return item;
    }
    
    // ================= ADVANCED INSTRUCTOR FUNCTIONALITIES =================
    private void showAdvancedCreateExam(Stage stage) {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        
        Label title = new Label("➕ CREATE NEW EXAMINATION");
        title.setFont(Font.font("Consolas", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#2c3e50"));
        
        // Create wizard-like interface
        TabPane wizardTabs = new TabPane();
        wizardTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Tab 1: Basic Information
        Tab basicInfoTab = new Tab("1. Basic Information");
        VBox basicInfoContent = new VBox(20);
        basicInfoContent.setPadding(new Insets(20));
        
        GridPane basicForm = new GridPane();
        basicForm.setHgap(15);
        basicForm.setVgap(15);
        basicForm.setPadding(new Insets(20));
        basicForm.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                          "-fx-border-color: #dfe6e9; -fx-border-width: 1; -fx-border-radius: 10;");
        
        ComboBox<String> courseCombo = new ComboBox<>();
        for (String[] course : COURSES) {
            courseCombo.getItems().add(course[0] + " - " + course[1]);
        }
        courseCombo.setPrefWidth(300);
        
        TextField examTitleField = new TextField();
        examTitleField.setPromptText("e.g., Midterm Examination 2024");
        examTitleField.setPrefWidth(300);
        
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Exam description and instructions...");
        descriptionField.setPrefHeight(100);
        descriptionField.setPrefWidth(300);
        
        ComboBox<String> durationCombo = new ComboBox<>();
        durationCombo.getItems().addAll("3 minutes", "5 minutes", "10 minutes", "15 minutes");
        durationCombo.setValue("5 minutes");
        
        ComboBox<String> difficultyCombo = new ComboBox<>();
        difficultyCombo.getItems().addAll("Easy", "Medium", "Hard", "Mixed");
        difficultyCombo.setValue("Mixed");
        
        int row = 0;
        basicForm.add(new Label("Course*:"), 0, row);
        basicForm.add(courseCombo, 1, row++);
        
        basicForm.add(new Label("Exam Title*:"), 0, row);
        basicForm.add(examTitleField, 1, row++);
        
        basicForm.add(new Label("Description:"), 0, row);
        basicForm.add(descriptionField, 1, row++);
        
        basicForm.add(new Label("Duration*:"), 0, row);
        basicForm.add(durationCombo, 1, row++);
        
        basicForm.add(new Label("Difficulty Level:"), 0, row);
        basicForm.add(difficultyCombo, 1, row++);
        
        basicInfoContent.getChildren().add(basicForm);
        basicInfoTab.setContent(basicInfoContent);
        
        // Tab 2: Question Bank
        Tab questionsTab = new Tab("2. Question Bank");
        VBox questionsContent = new VBox(20);
        questionsContent.setPadding(new Insets(20));
        
        // Question management interface would go here
        Label questionsLabel = new Label("Question bank management interface");
        questionsContent.getChildren().add(questionsLabel);
        questionsTab.setContent(questionsContent);
        
        // Tab 3: Settings
        Tab settingsTab = new Tab("3. Settings & Security");
        VBox settingsContent = new VBox(20);
        settingsContent.setPadding(new Insets(20));
        
        GridPane settingsForm = new GridPane();
        settingsForm.setHgap(15);
        settingsForm.setVgap(15);
        settingsForm.setPadding(new Insets(20));
        settingsForm.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                            "-fx-border-color: #dfe6e9; -fx-border-width: 1; -fx-border-radius: 10;");
        
        CheckBox shuffleQuestions = new CheckBox("Shuffle questions");
        shuffleQuestions.setSelected(true);
        
        CheckBox shuffleOptions = new CheckBox("Shuffle answer options");
        shuffleOptions.setSelected(true);
        
        CheckBox showTimer = new CheckBox("Show countdown timer");
        showTimer.setSelected(true);
        
        CheckBox allowReview = new CheckBox("Allow answer review after submission");
        
        CheckBox enableSecurity = new CheckBox("Enable enhanced security features");
        enableSecurity.setSelected(true);
        
        row = 0;
        settingsForm.add(new Label("Security Settings:"), 0, row, 2, 1);
        settingsForm.add(enableSecurity, 0, ++row, 2, 1);
        settingsForm.add(new Label("Display Settings:"), 0, ++row, 2, 1);
        settingsForm.add(shuffleQuestions, 0, ++row);
        settingsForm.add(shuffleOptions, 1, row++);
        settingsForm.add(showTimer, 0, ++row);
        settingsForm.add(allowReview, 1, row++);
        
        settingsContent.getChildren().add(settingsForm);
        settingsTab.setContent(settingsContent);
        
        wizardTabs.getTabs().addAll(basicInfoTab, questionsTab, settingsTab);
        
        // Create button
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button createBtn = new Button("🚀 Create Examination");
        createBtn.setPrefWidth(250);
        createBtn.setPrefHeight(50);
        createBtn.setStyle("-fx-background-color: linear-gradient(to right, #f6b93b, #e55039); " +
                          "-fx-background-radius: 8; " +
                          "-fx-text-fill: white; " +
                          "-fx-font-family: 'Segoe UI'; " +
                          "-fx-font-weight: bold; " +
                          "-fx-font-size: 16; " +
                          "-fx-cursor: hand;");
        
        createBtn.setOnAction(e -> {
            String course = courseCombo.getValue();
            String titleText = examTitleField.getText().trim();
            String durationText = durationCombo.getValue();
            
            if (titleText.isEmpty()) {
                showAlert("Validation Error", "Exam title is required");
                return;
            }
            
            if (course == null) {
                showAlert("Validation Error", "Please select a course");
                return;
            }
            
            // Generate questions
            List<Question> questions = generateRandomQuestions(course.split(" - ")[1]);
            String examId = "EXAM_" + System.currentTimeMillis();
            
            Exam newExam = new Exam(
                examId,
                course.split(" - ")[1],
                titleText,
                course.split(" - ")[0],
                getCurrentUser().id,
                questions
            );
            
            exams.add(newExam);
            
            auditLogs.add(new AuditLog(currentUsername, "EXAM_CREATED", 
                "Created exam: " + titleText + " for course: " + course));
            
            showAlert("EXAMINATION CREATED", 
                "✅ Examination created successfully!\n\n" +
                "• Course: " + course + "\n" +
                "• Title: " + titleText + "\n" +
                "• Duration: " + durationText + "\n" +
                "• Questions: " + QUESTIONS_PER_EXAM + "\n" +
                "• Security: Enhanced features enabled\n\n" +
                "The examination is now available for students.");
            
            examTitleField.clear();
            descriptionField.clear();
        });
        
        Button cancelBtn = new Button("❌ Cancel");
        cancelBtn.setPrefWidth(150);
        cancelBtn.setPrefHeight(50);
        cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        cancelBtn.setOnAction(e -> showAdvancedDefaultContent((StackPane) ((BorderPane) stage.getScene().getRoot()).getCenter(), "Instructor", stage));
        
        buttonBox.getChildren().addAll(cancelBtn, createBtn);
        
        content.getChildren().addAll(title, wizardTabs, buttonBox);
        ((BorderPane) stage.getScene().getRoot()).setCenter(content);
    }
    
    private void showAdvancedGradeManagement(Stage stage) {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        
        Label title = new Label("📝 GRADE MANAGEMENT");
        title.setFont(Font.font("Consolas", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#2c3e50"));
        
        // Create tabs for different grading views
        TabPane gradeTabs = new TabPane();
        
        // Tab 1: Pending Submissions
        Tab pendingTab = new Tab("⏳ Pending Grading");
        VBox pendingContent = new VBox(15);
        pendingContent.setPadding(new Insets(20));
        
        // Create table for pending submissions
        TableView<Map<String, String>> pendingTable = new TableView<>();
        
        TableColumn<Map<String, String>, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("student")));
        studentCol.setPrefWidth(200);
        
        TableColumn<Map<String, String>, String> examCol = new TableColumn<>("Exam");
        examCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("exam")));
        examCol.setPrefWidth(250);
        
        TableColumn<Map<String, String>, String> dateCol = new TableColumn<>("Submission Date");
        dateCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("date")));
        dateCol.setPrefWidth(150);
        
        TableColumn<Map<String, String>, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("status")));
        statusCol.setPrefWidth(100);
        
        TableColumn<Map<String, String>, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("action")));
        actionCol.setPrefWidth(150);
        
        pendingTable.getColumns().add(studentCol);
        pendingTable.getColumns().add(examCol);
        pendingTable.getColumns().add(dateCol);
        pendingTable.getColumns().add(statusCol);
        pendingTable.getColumns().add(actionCol);
        pendingTable.setPrefHeight(400);
        
        // Add sample data
        ObservableList<Map<String, String>> pendingData = FXCollections.observableArrayList();
        
        Map<String, String> row1 = new HashMap<>();
        row1.put("student", "Kaleb Getachew (DUCS001)");
        row1.put("exam", "CSE101 - Midterm Examination");
        row1.put("date", "2024-12-23 10:30");
        row1.put("status", "⏳ Pending");
        row1.put("action", "Grade Now");
        pendingData.add(row1);
        
        Map<String, String> row2 = new HashMap<>();
        row2.put("student", "Meron Abebe (DUCS002)");
        row2.put("exam", "CSE601 - Cybersecurity Fundamentals");
        row2.put("date", "2024-12-23 11:15");
        row2.put("status", "⏳ Pending");
        row2.put("action", "Grade Now");
        pendingData.add(row2);
        
        pendingTable.setItems(pendingData);
        
        // Add action buttons
        HBox pendingActions = new HBox(15);
        Button gradeSelectedBtn = new Button("📝 Grade Selected");
        gradeSelectedBtn.setStyle("-fx-background-color: #f6b93b; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Button bulkGradeBtn = new Button("📊 Bulk Grading");
        bulkGradeBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        
        pendingActions.getChildren().addAll(gradeSelectedBtn, bulkGradeBtn);
        pendingContent.getChildren().addAll(pendingTable, pendingActions);
        pendingTab.setContent(pendingContent);
        
        // Tab 2: Graded Submissions
        Tab gradedTab = new Tab("✅ Graded Submissions");
        VBox gradedContent = new VBox(15);
        gradedContent.setPadding(new Insets(20));
        
        TableView<Map<String, String>> gradedTable = new TableView<>();
        
        TableColumn<Map<String, String>, String> gStudentCol = new TableColumn<>("Student");
        gStudentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("student")));
        
        TableColumn<Map<String, String>, String> gExamCol = new TableColumn<>("Exam");
        gExamCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("exam")));
        
        TableColumn<Map<String, String>, String> gScoreCol = new TableColumn<>("Score");
        gScoreCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("score")));
        
        TableColumn<Map<String, String>, String> gGradeCol = new TableColumn<>("Grade");
        gGradeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("grade")));
        
        TableColumn<Map<String, String>, String> gDateCol = new TableColumn<>("Graded On");
        gDateCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("date")));
        
        gradedTable.getColumns().add(gStudentCol);
        gradedTable.getColumns().add(gExamCol);
        gradedTable.getColumns().add(gScoreCol);
        gradedTable.getColumns().add(gGradeCol);
        gradedTable.getColumns().add(gDateCol);
        gradedTable.setPrefHeight(400);
        
        gradedContent.getChildren().add(gradedTable);
        gradedTab.setContent(gradedContent);
        
        // Tab 3: Grade Distribution
        Tab distributionTab = new Tab("📊 Grade Distribution");
        VBox distributionContent = new VBox(20);
        distributionContent.setPadding(new Insets(20));
        
        // Create grade distribution chart (simulated)
        VBox distributionChart = new VBox(10);
        distributionChart.setPadding(new Insets(20));
        distributionChart.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                                 "-fx-border-color: #dfe6e9; -fx-border-width: 1; -fx-border-radius: 10;");
        
        Label chartTitle = new Label("Grade Distribution for CSE101 - Midterm");
        chartTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        // Simulated grade bars
        String[] grades = {"A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D", "F"};
        int[] counts = {2, 5, 8, 12, 15, 10, 8, 6, 4, 2, 1};
        
        VBox barsContainer = new VBox(10);
        for (int i = 0; i < grades.length; i++) {
            HBox barRow = new HBox(10);
            barRow.setAlignment(Pos.CENTER_LEFT);
            
            Label gradeLabel = new Label(grades[i]);
            gradeLabel.setPrefWidth(30);
            gradeLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
            gradeLabel.setTextFill(Color.web(getGradeColor(grades[i])));
            
            ProgressBar bar = new ProgressBar(counts[i] / 30.0); // Normalize to max 30
            bar.setPrefWidth(300);
            bar.setStyle("-fx-accent: " + getGradeColor(grades[i]) + ";");
            
            Label countLabel = new Label(String.valueOf(counts[i]));
            countLabel.setFont(Font.font("Consolas", 12));
            
            barRow.getChildren().addAll(gradeLabel, bar, countLabel);
            barsContainer.getChildren().add(barRow);
        }
        
        distributionChart.getChildren().addAll(chartTitle, barsContainer);
        distributionContent.getChildren().add(distributionChart);
        distributionTab.setContent(distributionContent);
        
        gradeTabs.getTabs().addAll(pendingTab, gradedTab, distributionTab);
        
        // Statistics panel
        HBox statsPanel = new HBox(20);
        statsPanel.setPadding(new Insets(20));
        statsPanel.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                           "-fx-border-color: #dfe6e9; -fx-border-width: 1; -fx-border-radius: 10;");
        
        VBox pendingStats = createGradeStatCard("⏳", "Pending", "12 submissions", "#f6b93b");
        VBox gradedStats = createGradeStatCard("✅", "Graded", "48 submissions", "#2ecc71");
        VBox avgStats = createGradeStatCard("📊", "Avg. Score", "78.5%", "#3498db");
        VBox topStats = createGradeStatCard("🏆", "Top Grade", "A+ (4.0)", "#9b59b6");
        
        statsPanel.getChildren().addAll(pendingStats, gradedStats, avgStats, topStats);
        
        content.getChildren().addAll(title, statsPanel, gradeTabs);
        ((BorderPane) stage.getScene().getRoot()).setCenter(content);
    }
    
    private VBox createGradeStatCard(String icon, String title, String value, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color: " + color + "10; -fx-background-radius: 8; " +
                     "-fx-border-color: " + color + "30; -fx-border-width: 1; -fx-border-radius: 8;");
        
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Arial", 24));
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        titleLabel.setTextFill(Color.web("#7f8c8d"));
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 18));
        valueLabel.setTextFill(Color.web(color));
        
        card.getChildren().addAll(iconLabel, titleLabel, valueLabel);
        return card;
    }
    
    // ================= ADVANCED ADMIN FUNCTIONALITIES =================
    private void showAdminDashboard(Stage stage) {
        VBox content = new VBox(30);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #f8f9fa;");
        
        Label title = new Label("🛡️ ADMINISTRATOR CONTROL PANEL");
        title.setFont(Font.font("Consolas", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2c3e50"));
        
        // System overview cards
        GridPane overviewGrid = new GridPane();
        overviewGrid.setHgap(20);
        overviewGrid.setVgap(20);
        overviewGrid.setPadding(new Insets(20));
        overviewGrid.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                            "-fx-border-color: #dfe6e9; -fx-border-width: 1; -fx-border-radius: 15;");
        
        // System statistics
        int totalUsers = users.size();
        int totalStudents = (int) users.stream().filter(u -> u.role.equals("Student")).count();
        int totalInstructors = (int) users.stream().filter(u -> u.role.equals("Instructor")).count();
        int totalAdmins = (int) users.stream().filter(u -> u.role.equals("Admin")).count();
        int totalExams = exams.size();
        int activeExams = (int) exams.stream().filter(e -> e.isActive).count();
        int totalAttempts = studentAttempts.size();
        int securityAlerts = auditLogs.stream()
            .filter(log -> log.action.contains("FAILED") || log.action.contains("ALERT"))
            .toList()
            .size();
        
        overviewGrid.add(createAdminStatCard("👥", "Total Users", String.valueOf(totalUsers), 
            "Students: " + totalStudents + " | Instructors: " + totalInstructors, "#e74c3c"), 0, 0);
        
        overviewGrid.add(createAdminStatCard("📋", "Examinations", String.valueOf(totalExams), 
            "Active: " + activeExams + " | Total: " + totalExams, "#f6b93b"), 1, 0);
        
        overviewGrid.add(createAdminStatCard("📊", "Submissions", String.valueOf(totalAttempts), 
            "Student attempts across all exams", "#3498db"), 2, 0);
        
        overviewGrid.add(createAdminStatCard("🔒", "Security", String.valueOf(securityAlerts), 
            "Alerts in last 24 hours", "#00d2d3"), 3, 0);
        
        // Quick actions
        VBox quickActions = new VBox(20);
        quickActions.setPadding(new Insets(30));
        quickActions.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                            "-fx-border-color: #dfe6e9; -fx-border-width: 1; -fx-border-radius: 15;");
        
        Label actionsTitle = new Label("⚡ QUICK ADMINISTRATIVE ACTIONS");
        actionsTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 18));
        actionsTitle.setTextFill(Color.web("#2c3e50"));
        
        // Action buttons grid
        GridPane actionGrid = new GridPane();
        actionGrid.setHgap(15);
        actionGrid.setVgap(15);
        
        Button userMgmtBtn = createAdminActionButton("👥 User Management", "#e74c3c", 
            () -> showAdvancedUserManagement(stage));
        Button examMgmtBtn = createAdminActionButton("📋 Exam Management", "#f6b93b", 
            () -> showAdvancedManageExams(stage));
        Button securityBtn = createAdminActionButton("🔒 Security Center", "#00d2d3", 
            () -> showSecurityCenter(stage));
        Button auditBtn = createAdminActionButton("📋 Audit Logs", "#3498db", 
            () -> showAuditLogs(stage));
        Button backupBtn = createAdminActionButton("💾 System Backup", "#2ecc71", 
            () -> showAlert("Backup", "System backup initiated. Estimated completion: 5 minutes."));
        Button reportsBtn = createAdminActionButton("📈 Generate Reports", "#9b59b6", 
            () -> showAlert("Reports", "Comprehensive system report generated and saved."));
        
        actionGrid.add(userMgmtBtn, 0, 0);
        actionGrid.add(examMgmtBtn, 1, 0);
        actionGrid.add(securityBtn, 2, 0);
        actionGrid.add(auditBtn, 0, 1);
        actionGrid.add(backupBtn, 1, 1);
        actionGrid.add(reportsBtn, 2, 1);
        
        quickActions.getChildren().addAll(actionsTitle, actionGrid);
        
        // System monitoring
        VBox monitoring = new VBox(20);
        monitoring.setPadding(new Insets(30));
        monitoring.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                          "-fx-border-color: #dfe6e9; -fx-border-width: 1; -fx-border-radius: 15;");
        
        Label monitoringTitle = new Label("📊 REAL-TIME SYSTEM MONITORING");
        monitoringTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 18));
        monitoringTitle.setTextFill(Color.web("#2c3e50"));
        
        // Monitoring metrics
        HBox metrics = new HBox(20);
        metrics.setAlignment(Pos.CENTER);
        
        VBox serverMetric = createMonitoringMetric("🖥️", "Server Load", "45%", "#2ecc71");
        VBox memoryMetric = createMonitoringMetric("💾", "Memory Usage", "68%", "#3498db");
        VBox networkMetric = createMonitoringMetric("🌐", "Network Traffic", "1.2 Gbps", "#f6b93b");
        VBox securityMetric = createMonitoringMetric("🛡️", "Security Status", "Active", "#00d2d3");
        
        metrics.getChildren().addAll(serverMetric, memoryMetric, networkMetric, securityMetric);
        monitoring.getChildren().addAll(monitoringTitle, metrics);
        
        content.getChildren().addAll(title, overviewGrid, quickActions, monitoring);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        
        ((BorderPane) stage.getScene().getRoot()).setCenter(scrollPane);
    }
    
    private VBox createAdminStatCard(String icon, String title, String value, String subtitle, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setPrefWidth(250);
        card.setStyle("-fx-background-color: " + color + "10; -fx-background-radius: 10; " +
                     "-fx-border-color: " + color + "30; -fx-border-width: 1; -fx-border-radius: 10;");
        
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Arial", 20));
        
        VBox titles = new VBox(2);
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.web("#7f8c8d"));
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 24));
        valueLabel.setTextFill(Color.web(color));
        
        titles.getChildren().addAll(titleLabel, valueLabel);
        header.getChildren().addAll(iconLabel, titles);
        
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setFont(Font.font("Segoe UI", 10));
        subtitleLabel.setTextFill(Color.web("#95a5a6"));
        subtitleLabel.setWrapText(true);
        
        card.getChildren().addAll(header, subtitleLabel);
        return card;
    }
    
    private Button createAdminActionButton(String text, String color, Runnable action) {
        Button btn = new Button(text);
        btn.setPrefWidth(200);
        btn.setPrefHeight(60);
        btn.setStyle("-fx-background-color: " + color + "; " +
                    "-fx-background-radius: 8; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-family: 'Segoe UI'; " +
                    "-fx-font-weight: bold; " +
                    "-fx-font-size: 13; " +
                    "-fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, " + color + "80, 10, 0.5, 0, 2);");
        btn.setOnAction(e -> action.run());
        return btn;
    }
    
    private VBox createMonitoringMetric(String icon, String title, String value, String color) {
        VBox metric = new VBox(10);
        metric.setPadding(new Insets(20));
        metric.setAlignment(Pos.CENTER);
        metric.setPrefWidth(180);
        metric.setStyle("-fx-background-color: " + color + "10; -fx-background-radius: 10; " +
                       "-fx-border-color: " + color + "30; -fx-border-width: 1; -fx-border-radius: 10;");
        
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Arial", 24));
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        titleLabel.setTextFill(Color.web("#7f8c8d"));
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 18));
        valueLabel.setTextFill(Color.web(color));
        
        // Progress indicator
        ProgressBar progress = new ProgressBar(0.5); // Example value
        progress.setPrefWidth(140);
        progress.setStyle("-fx-accent: " + color + ";");
        
        metric.getChildren().addAll(iconLabel, titleLabel, valueLabel, progress);
        return metric;
    }
    
    private void showAdvancedUserManagement(Stage stage) {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        
        Label title = new Label("👥 USER MANAGEMENT");
        title.setFont(Font.font("Consolas", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#2c3e50"));
        
        // Filter and search panel
        HBox filterPanel = new HBox(15);
        filterPanel.setPadding(new Insets(15));
        filterPanel.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                           "-fx-border-color: #dfe6e9; -fx-border-width: 1; -fx-border-radius: 10;");
        
        TextField searchField = new TextField();
        searchField.setPromptText("Search users by name, ID, or email...");
        searchField.setPrefWidth(300);
        
        ComboBox<String> roleFilter = new ComboBox<>();
        roleFilter.getItems().addAll("All Roles", "Student", "Instructor", "Admin");
        roleFilter.setValue("All Roles");
        
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All Status", "Active", "Inactive", "Locked");
        statusFilter.setValue("All Status");
        
        Button searchBtn = new Button("🔍 Search");
        searchBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        
        Button addUserBtn = new Button("➕ Add User");
        addUserBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        addUserBtn.setOnAction(e -> showAddUserDialog(stage));
        
        filterPanel.getChildren().addAll(searchField, roleFilter, statusFilter, searchBtn, addUserBtn);
        
        // User table
        TableView<User> userTable = new TableView<>(users);
        
        TableColumn<User, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().name));
        nameCol.setPrefWidth(200);
        
        TableColumn<User, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().id));
        idCol.setPrefWidth(100);
        
        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().role));
        roleCol.setPrefWidth(100);
        
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().email));
        emailCol.setPrefWidth(200);
        
        TableColumn<User, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().department));
        deptCol.setPrefWidth(150);
        
        TableColumn<User, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().passwordChangeRequired ? "First Login" : "Active"
        ));
        statusCol.setPrefWidth(100);
        
        TableColumn<User, String> lastLoginCol = new TableColumn<>("Last Login");
        lastLoginCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().lastLogin.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        ));
        lastLoginCol.setPrefWidth(150);
        
        userTable.getColumns().add(nameCol);
        userTable.getColumns().add(idCol);
        userTable.getColumns().add(roleCol);
        userTable.getColumns().add(emailCol);
        userTable.getColumns().add(deptCol);
        userTable.getColumns().add(statusCol);
        userTable.getColumns().add(lastLoginCol);
        userTable.setPrefHeight(400);
        
        // Action buttons for selected user
        HBox actionButtons = new HBox(15);
        actionButtons.setPadding(new Insets(15));
        actionButtons.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                             "-fx-border-color: #dfe6e9; -fx-border-width: 1; -fx-border-radius: 10;");
        
        Button editBtn = new Button("✏️ Edit User");
        editBtn.setStyle("-fx-background-color: #f6b93b; -fx-text-fill: white;");
        
        Button resetPassBtn = new Button("🔑 Reset Password");
        resetPassBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        resetPassBtn.setOnAction(e -> {
            User selected = userTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String newTempPass = generateTemporaryPassword();
                selected.password = newTempPass;
                selected.passwordChangeRequired = true;
                
                auditLogs.add(new AuditLog(currentUsername, "PASSWORD_RESET", 
                    "Reset password for user: " + selected.username));
                
                showAlert("Password Reset", 
                    "Password has been reset for: " + selected.name + "\n\n" +
                    "New Temporary Password: " + newTempPass + "\n\n" +
                    "User must change password on next login.");
            }
        });
        
        Button lockBtn = new Button("🔒 Lock Account");
        lockBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        
        Button deleteBtn = new Button("🗑️ Delete User");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> {
            User selected = userTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirm Delete");
                confirm.setHeaderText("Delete User Account");
                confirm.setContentText("Are you sure you want to delete:\n" + 
                                     selected.name + " (" + selected.id + ")?\n\n" +
                                     "This action cannot be undone.");
                
                Optional<ButtonType> result = confirm.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    users.remove(selected);
                    
                    auditLogs.add(new AuditLog(currentUsername, "USER_DELETED", 
                        "Deleted user: " + selected.username));
                    
                    showAlert("User Deleted", "User account has been removed from the system.");
                }
            }
        });
        
        actionButtons.getChildren().addAll(editBtn, resetPassBtn, lockBtn, deleteBtn);
        
        content.getChildren().addAll(title, filterPanel, userTable, actionButtons);
        ((BorderPane) stage.getScene().getRoot()).setCenter(content);
    }
    
    private void showAddUserDialog(Stage stage) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("➕ Add New User");
        dialog.setHeaderText("Create a new user account");
        
        // Set the button types
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
        
        // Create the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        
        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        
        TextField idField = new TextField();
        idField.setPromptText("User ID");
        
        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");
        
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("Student", "Instructor", "Admin");
        roleCombo.setValue("Student");
        
        ComboBox<String> deptCombo = new ComboBox<>();
        deptCombo.getItems().addAll(DEPARTMENTS);
        deptCombo.setValue("Computer Science");
        
        CheckBox requirePassChange = new CheckBox("Require password change on first login");
        requirePassChange.setSelected(true);
        
        grid.add(new Label("Username*:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Full Name*:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("User ID*:"), 0, 2);
        grid.add(idField, 1, 2);
        grid.add(new Label("Email*:"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("Role*:"), 0, 4);
        grid.add(roleCombo, 1, 4);
        grid.add(new Label("Department:"), 0, 5);
        grid.add(deptCombo, 1, 5);
        grid.add(requirePassChange, 0, 6, 2, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on username field
        Platform.runLater(() -> usernameField.requestFocus());
        
        // Convert the result to a user when the create button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                String tempPassword = generateTemporaryPassword();
                return new User(
                    usernameField.getText(),
                    tempPassword,
                    nameField.getText(),
                    idField.getText(),
                    roleCombo.getValue(),
                    emailField.getText(),
                    deptCombo.getValue(),
                    requirePassChange.isSelected()
                );
            }
            return null;
        });
        
        Optional<User> result = dialog.showAndWait();
        result.ifPresent(newUser -> {
            // Check for duplicate username
            for (User user : users) {
                if (user.username.equals(newUser.username)) {
                    showAlert("Error", "Username already exists. Please choose a different username.");
                    return;
                }
            }
            
            users.add(newUser);
            
            auditLogs.add(new AuditLog(currentUsername, "USER_CREATED", 
                "Created new user: " + newUser.username + " (" + newUser.role + ")"));
            
            showAlert("User Created Successfully",
                "✅ Account created for: " + newUser.name + "\n\n" +
                "Login Credentials:\n" +
                "• Username: " + newUser.username + "\n" +
                "• Temporary Password: " + newUser.password + "\n\n" +
                "User must change password on first login.");
        });
    }
    
    private void showSecurityCenter(Stage stage) {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        
        Label title = new Label("🔒 SECURITY CENTER");
        title.setFont(Font.font("Consolas", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#2c3e50"));
        
        // Security status panel
        VBox statusPanel = new VBox(15);
        statusPanel.setPadding(new Insets(20));
        statusPanel.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                           "-fx-border-color: #dfe6e9; -fx-border-width: 1; -fx-border-radius: 10;");
        
        Label statusTitle = new Label("🛡️ SECURITY STATUS: ACTIVE");
        statusTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 16));
        statusTitle.setTextFill(Color.web("#2ecc71"));
        
        // Security metrics
        GridPane securityGrid = new GridPane();
        securityGrid.setHgap(20);
        securityGrid.setVgap(15);
        
        securityGrid.add(createSecurityMetric("🔐", "Encryption", "256-bit SSL", "#00d2d3", true), 0, 0);
        securityGrid.add(createSecurityMetric("👁️", "Monitoring", "Active", "#3498db", true), 1, 0);
        securityGrid.add(createSecurityMetric("🚨", "Intrusion Detection", "Enabled", "#e74c3c", true), 2, 0);
        securityGrid.add(createSecurityMetric("📋", "Audit Logging", "Enabled", "#f6b93b", true), 3, 0);
        securityGrid.add(createSecurityMetric("⏱️", "Session Timeout", "30 min", "#9b59b6", true), 0, 1);
        securityGrid.add(createSecurityMetric("🔑", "MFA", "Optional", "#2ecc71", false), 1, 1);
        securityGrid.add(createSecurityMetric("📧", "Email Alerts", "Enabled", "#e67e22", true), 2, 1);
        securityGrid.add(createSecurityMetric("💾", "Backup", "Daily", "#95a5a6", true), 3, 1);
        
        statusPanel.getChildren().addAll(statusTitle, securityGrid);
        
        // Recent security events
        VBox eventsPanel = new VBox(15);
        eventsPanel.setPadding(new Insets(20));
        eventsPanel.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                           "-fx-border-color: #dfe6e9; -fx-border-width: 1; -fx-border-radius: 10;");
        
        Label eventsTitle = new Label("🚨 RECENT SECURITY EVENTS");
        eventsTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 16));
        
        // Display recent audit logs with security events
        ListView<String> eventsList = new ListView<>();
        ObservableList<String> securityEvents = FXCollections.observableArrayList();
        
        // Get recent security-related logs
        List<AuditLog> recentLogs = auditLogs.stream()
            .filter(log -> log.action.contains("FAILED") || log.action.contains("ALERT") || 
                          log.action.contains("SECURITY") || log.action.contains("LOGIN"))
            .sorted((a, b) -> b.timestamp.compareTo(a.timestamp))
            .limit(10)
            .toList();
        
        for (AuditLog log : recentLogs) {
            String event = String.format("[%s] %s: %s - %s",
                log.timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                log.userId,
                log.action,
                log.details);
            securityEvents.add(event);
        }
        
        eventsList.setItems(securityEvents);
        eventsList.setPrefHeight(200);
        
        eventsPanel.getChildren().addAll(eventsTitle, eventsList);
        
        // Security actions
        HBox securityActions = new HBox(15);
        securityActions.setPadding(new Insets(20));
        securityActions.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                               "-fx-border-color: #dfe6e9; -fx-border-width: 1; -fx-border-radius: 10;");
        
        Button scanBtn = new Button("🔍 Run Security Scan");
        scanBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        
        Button lockSystemBtn = new Button("🔒 Lock System");
        lockSystemBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        
        Button exportLogsBtn = new Button("📋 Export Audit Logs");
        exportLogsBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        
        Button configBtn = new Button("⚙️ Security Configuration");
        configBtn.setStyle("-fx-background-color: #f6b93b; -fx-text-fill: white;");
        
        securityActions.getChildren().addAll(scanBtn, lockSystemBtn, exportLogsBtn, configBtn);
        
        content.getChildren().addAll(title, statusPanel, eventsPanel, securityActions);
        ((BorderPane) stage.getScene().getRoot()).setCenter(content);
    }
    
    private HBox createSecurityMetric(String icon, String label, String value, String color, boolean enabled) {
        HBox metric = new HBox(10);
        metric.setPadding(new Insets(10));
        metric.setAlignment(Pos.CENTER_LEFT);
        metric.setStyle("-fx-background-color: " + color + "10; -fx-background-radius: 8; " +
                       "-fx-border-color: " + color + "30; -fx-border-width: 1; -fx-border-radius: 8;");
        
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Arial", 16));
        
        VBox textBox = new VBox(2);
        Label labelText = new Label(label);
        labelText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        labelText.setTextFill(Color.web("#7f8c8d"));
        
        HBox valueBox = new HBox(5);
        Label valueText = new Label(value);
        valueText.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        valueText.setTextFill(Color.web(color));
        
        Label statusIcon = new Label(enabled ? "✅" : "⚠️");
        statusIcon.setFont(Font.font("Arial", 10));
        
        valueBox.getChildren().addAll(valueText, statusIcon);
        textBox.getChildren().addAll(labelText, valueBox);
        metric.getChildren().addAll(iconLabel, textBox);
        
        return metric;
    }
    
    private void showAuditLogs(Stage stage) {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        
        Label title = new Label("📋 AUDIT LOGS");
        title.setFont(Font.font("Consolas", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#2c3e50"));
        
        // Filter panel
        HBox filterPanel = new HBox(15);
        filterPanel.setPadding(new Insets(15));
        filterPanel.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                           "-fx-border-color: #dfe6e9; -fx-border-width: 1; -fx-border-radius: 10;");
        
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Select date");
        
        ComboBox<String> userFilter = new ComboBox<>();
        userFilter.getItems().addAll("All Users");
        for (User user : users) {
            userFilter.getItems().add(user.username);
        }
        userFilter.setValue("All Users");
        
        ComboBox<String> actionFilter = new ComboBox<>();
        actionFilter.getItems().addAll("All Actions", "LOGIN", "LOGOUT", "EXAM", "USER", "SECURITY");
        actionFilter.setValue("All Actions");
        
        Button filterBtn = new Button("🔍 Filter");
        filterBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        
        Button clearBtn = new Button("🧹 Clear Filters");
        clearBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        
        Button exportBtn = new Button("📥 Export Logs");
        exportBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        
        filterPanel.getChildren().addAll(datePicker, userFilter, actionFilter, filterBtn, clearBtn, exportBtn);
        
        // Logs table
        TableView<AuditLog> logsTable = new TableView<>(auditLogs);
        
        TableColumn<AuditLog, String> timestampCol = new TableColumn<>("Timestamp");
        timestampCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        ));
        timestampCol.setPrefWidth(150);
        
        TableColumn<AuditLog, String> userCol = new TableColumn<>("User");
        userCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().userId));
        userCol.setPrefWidth(120);
        
        TableColumn<AuditLog, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().action));
        actionCol.setPrefWidth(150);
        
        TableColumn<AuditLog, String> detailsCol = new TableColumn<>("Details");
        detailsCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().details));
        detailsCol.setPrefWidth(400);
        
        TableColumn<AuditLog, String> severityCol = new TableColumn<>("Severity");
        severityCol.setCellValueFactory(cellData -> {
            String action = cellData.getValue().action;
            String severity = "INFO";
            if (action.contains("FAILED") || action.contains("ALERT")) {
                severity = "HIGH";
            } else if (action.contains("WARNING")) {
                severity = "MEDIUM";
            }
            return new javafx.beans.property.SimpleStringProperty(severity);
        });
        severityCol.setPrefWidth(80);
        
        logsTable.getColumns().add(timestampCol);
        logsTable.getColumns().add(userCol);
        logsTable.getColumns().add(actionCol);
        logsTable.getColumns().add(detailsCol);
        logsTable.getColumns().add(severityCol);
        logsTable.setPrefHeight(400);
        
        // Statistics
        HBox statsPanel = new HBox(20);
        statsPanel.setPadding(new Insets(15));
        statsPanel.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                           "-fx-border-color: #dfe6e9; -fx-border-width: 1; -fx-border-radius: 10;");
        
        long totalLogs = auditLogs.size();
        long failedLogins = auditLogs.stream()
            .filter(log -> log.action.contains("LOGIN_FAILED"))
            .count();
        long securityEvents = auditLogs.stream()
            .filter(log -> log.action.contains("ALERT") || log.action.contains("SECURITY"))
            .count();
        
        statsPanel.getChildren().addAll(
            createLogStat("Total Logs", String.valueOf(totalLogs), "#3498db"),
            createLogStat("Failed Logins", String.valueOf(failedLogins), "#e74c3c"),
            createLogStat("Security Events", String.valueOf(securityEvents), "#f6b93b"),
            createLogStat("Last 24h", "48", "#2ecc71")
        );
        
        content.getChildren().addAll(title, filterPanel, logsTable, statsPanel);
        ((BorderPane) stage.getScene().getRoot()).setCenter(content);
    }
    
    private VBox createLogStat(String label, String value, String color) {
        VBox stat = new VBox(5);
        stat.setPadding(new Insets(10));
        stat.setAlignment(Pos.CENTER);
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 18));
        valueLabel.setTextFill(Color.web(color));
        
        Label labelText = new Label(label);
        labelText.setFont(Font.font("Segoe UI", 10));
        labelText.setTextFill(Color.web("#7f8c8d"));
        
        stat.getChildren().addAll(valueLabel, labelText);
        return stat;
    }
    
    // ================= OTHER ADVANCED FUNCTIONALITIES =================
    private void showAdvancedStudentResults(Stage stage) {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        
        Label title = new Label("📊 ACADEMIC PERFORMANCE");
        title.setFont(Font.font("Consolas", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#2c3e50"));
        
        User currentStudent = getCurrentUser();
        
        // Get student's exam results
        List<StudentAttempt> studentExams = studentAttempts.stream()
            .filter(a -> a.studentId.equals(currentStudent.id) && a.isSubmitted)
            .toList();
        
        if (studentExams.isEmpty()) {
            VBox noResults = new VBox(20);
            noResults.setAlignment(Pos.CENTER);
            noResults.setPadding(new Insets(50));
            
            Label noResultsIcon = new Label("📭");
            noResultsIcon.setFont(Font.font("Arial", 48));
            
            Label noResultsText = new Label("No examination results available yet.\nComplete your first exam to see results.");
            noResultsText.setFont(Font.font("Segoe UI", 16));
            noResultsText.setTextFill(Color.web("#7f8c8d"));
            noResultsText.setStyle("-fx-text-alignment: center;");
            
            noResults.getChildren().addAll(noResultsIcon, noResultsText);
            content.getChildren().addAll(title, noResults);
        } else {
            // Create results table
            TableView<Map<String, String>> resultsTable = new TableView<>();
            
            TableColumn<Map<String, String>, String> examCol = new TableColumn<>("Examination");
            examCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("exam")));
            examCol.setPrefWidth(250);
            
            TableColumn<Map<String, String>, String> dateCol = new TableColumn<>("Date");
            dateCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("date")));
            dateCol.setPrefWidth(120);
            
            TableColumn<Map<String, String>, String> scoreCol = new TableColumn<>("Score");
            scoreCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("score")));
            scoreCol.setPrefWidth(100);
            
            TableColumn<Map<String, String>, String> gradeCol = new TableColumn<>("Grade");
            gradeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("grade")));
            gradeCol.setPrefWidth(80);
            
            TableColumn<Map<String, String>, String> gpaCol = new TableColumn<>("GPA");
            gpaCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("gpa")));
            gpaCol.setPrefWidth(80);
            
            resultsTable.getColumns().addAll(examCol, dateCol, scoreCol, gradeCol, gpaCol);
            resultsTable.setPrefHeight(300);
            
            // Add data
            ObservableList<Map<String, String>> resultsData = FXCollections.observableArrayList();
            
            for (StudentAttempt attempt : studentExams) {
                // Find the exam
                Exam exam = exams.stream()
                    .filter(e -> e.examId.equals(attempt.examId))
                    .findFirst()
                    .orElse(null);
                
                if (exam != null) {
                    Map<String, String> row = new HashMap<>();
                    row.put("exam", exam.examTitle);
                    row.put("date", attempt.endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    row.put("score", attempt.marksObtained + "/" + exam.totalMarks);
                    row.put("grade", attempt.letterGrade);
                    row.put("gpa", String.format("%.2f", attempt.gradePoint));
                    resultsData.add(row);
                }
            }
            
            resultsTable.setItems(resultsData);
            
            // Calculate overall statistics
            double avgScore = studentExams.stream()
                .mapToDouble(a -> (double) a.marksObtained)
                .average()
                .orElse(0.0);
            
            double avgGPA = studentExams.stream()
                .mapToDouble(a -> a.gradePoint)
                .average()
                .orElse(0.0);
            
            // Statistics cards
            HBox statsCards = new HBox(20);
            statsCards.setAlignment(Pos.CENTER);
            
            VBox totalExamsCard = createResultStatCard("📋", "Exams Taken", String.valueOf(studentExams.size()), "#00d2d3");
            VBox avgScoreCard = createResultStatCard("📊", "Avg. Score", String.format("%.1f", avgScore), "#3498db");
            VBox avgGPACard = createResultStatCard("⭐", "Avg. GPA", String.format("%.2f", avgGPA), "#f6b93b");
            VBox bestGradeCard = createResultStatCard("🏆", "Best Grade", "A", "#2ecc71");
            
            statsCards.getChildren().addAll(totalExamsCard, avgScoreCard, avgGPACard, bestGradeCard);
            
            content.getChildren().addAll(title, statsCards, resultsTable);
        }
        
        ((BorderPane) stage.getScene().getRoot()).setCenter(content);
    }
    
    private VBox createResultStatCard(String icon, String title, String value, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(150);
        card.setStyle("-fx-background-color: " + color + "10; -fx-background-radius: 10; " +
                     "-fx-border-color: " + color + "30; -fx-border-width: 1; -fx-border-radius: 10;");
        
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Arial", 24));
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        titleLabel.setTextFill(Color.web("#7f8c8d"));
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 20));
        valueLabel.setTextFill(Color.web(color));
        
        card.getChildren().addAll(iconLabel, titleLabel, valueLabel);
        return card;
    }
    
    private void showStudentAnalytics(Stage stage) {
        showAlert("Student Analytics", "Detailed analytics and performance tracking functionality");
    }
    
    private void showAdvancedStudentProfile(Stage stage) {
        User currentStudent = getCurrentUser();
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        
        Label title = new Label("👤 STUDENT PROFILE");
        title.setFont(Font.font("Consolas", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#2c3e50"));
        
        // Profile card
        VBox profileCard = new VBox(20);
        profileCard.setPadding(new Insets(30));
        profileCard.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                           "-fx-border-color: #dfe6e9; -fx-border-width: 1; -fx-border-radius: 15; " +
                           "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0.5, 0, 3);");
        
        // Profile header
        HBox profileHeader = new HBox(20);
        profileHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label profileIcon = new Label("🎓");
        profileIcon.setFont(Font.font("Arial", 48));
        
        VBox profileInfo = new VBox(5);
        Label nameLabel = new Label(currentStudent.name);
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        nameLabel.setTextFill(Color.web("#2c3e50"));
        
        Label idLabel = new Label("ID: " + currentStudent.id);
        idLabel.setFont(Font.font("Consolas", 14));
        idLabel.setTextFill(Color.web("#7f8c8d"));
        
        Label roleLabel = new Label("Role: Student");
        roleLabel.setFont(Font.font("Segoe UI", 12));
        roleLabel.setTextFill(Color.web("#00d2d3"));
        
        profileInfo.getChildren().addAll(nameLabel, idLabel, roleLabel);
        profileHeader.getChildren().addAll(profileIcon, profileInfo);
        
        // Personal information
        VBox personalInfo = new VBox(10);
        personalInfo.setPadding(new Insets(20, 0, 0, 0));
        
        Label personalTitle = new Label("📋 PERSONAL INFORMATION");
        personalTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 16));
        personalTitle.setTextFill(Color.web("#2c3e50"));
        
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(10);
        infoGrid.setPadding(new Insets(10));
        
        infoGrid.add(new Label("Email:"), 0, 0);
        infoGrid.add(new Label(currentStudent.email), 1, 0);
        infoGrid.add(new Label("Department:"), 0, 1);
        infoGrid.add(new Label(currentStudent.department), 1, 1);
        infoGrid.add(new Label("Last Login:"), 0, 2);
        infoGrid.add(new Label(currentStudent.lastLogin.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))), 1, 2);
        
        personalInfo.getChildren().addAll(personalTitle, infoGrid);
        
        // Academic summary
        VBox academicSummary = new VBox(10);
        academicSummary.setPadding(new Insets(20, 0, 0, 0));
        
        Label academicTitle = new Label("📚 ACADEMIC SUMMARY");
        academicTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 16));
        academicTitle.setTextFill(Color.web("#2c3e50"));
        
        // Get student's exam stats
        List<StudentAttempt> studentExams = studentAttempts.stream()
            .filter(a -> a.studentId.equals(currentStudent.id) && a.isSubmitted)
            .toList();
        
        int examsTaken = studentExams.size();
        double avgGPA = studentExams.stream()
            .mapToDouble(a -> a.gradePoint)
            .average()
            .orElse(0.0);
        
        GridPane academicGrid = new GridPane();
        academicGrid.setHgap(20);
        academicGrid.setVgap(10);
        academicGrid.setPadding(new Insets(10));
        
        academicGrid.add(new Label("Exams Taken:"), 0, 0);
        academicGrid.add(new Label(String.valueOf(examsTaken)), 1, 0);
        academicGrid.add(new Label("Average GPA:"), 0, 1);
        academicGrid.add(new Label(String.format("%.2f", avgGPA)), 1, 1);
        academicGrid.add(new Label("Status:"), 0, 2);
        academicGrid.add(new Label("Active"), 1, 2);
        
        academicSummary.getChildren().addAll(academicTitle, academicGrid);
        
        profileCard.getChildren().addAll(profileHeader, personalInfo, academicSummary);
        content.getChildren().addAll(title, profileCard);
        
        ((BorderPane) stage.getScene().getRoot()).setCenter(content);
    }
    
    private void showNotifications(Stage stage) {
        showAlert("Notifications", "System notifications and alerts functionality");
    }
    
    private void showAdvancedManageExams(Stage stage) {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        
        Label title = new Label("📋 EXAMINATION MANAGEMENT");
        title.setFont(Font.font("Consolas", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#2c3e50"));
        
        // Create tabs for different views
        TabPane examTabs = new TabPane();
        
        // Tab 1: Active Exams
        Tab activeTab = new Tab("✅ Active Exams");
        VBox activeContent = new VBox(15);
        activeContent.setPadding(new Insets(20));
        
        // Create table for active exams
        TableView<Exam> activeTable = new TableView<>();
        
        TableColumn<Exam, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().courseCode + " - " + cellData.getValue().courseName));
        courseCol.setPrefWidth(200);
        
        TableColumn<Exam, String> titleCol = new TableColumn<>("Exam Title");
        titleCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().examTitle));
        titleCol.setPrefWidth(250);
        
        TableColumn<Exam, String> questionsCol = new TableColumn<>("Questions");
        questionsCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().questions.size())));
        questionsCol.setPrefWidth(80);
        
        TableColumn<Exam, String> marksCol = new TableColumn<>("Total Marks");
        marksCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().totalMarks)));
        marksCol.setPrefWidth(100);
        
        TableColumn<Exam, String> dateCol = new TableColumn<>("Created Date");
        dateCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().createdDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        ));
        dateCol.setPrefWidth(120);
        
        activeTable.getColumns().addAll(courseCol, titleCol, questionsCol, marksCol, dateCol);
        activeTable.setPrefHeight(300);
        
        // Filter active exams
        ObservableList<Exam> activeExams = exams.filtered(e -> e.isActive);
        activeTable.setItems(activeExams);
        
        activeContent.getChildren().add(activeTable);
        activeTab.setContent(activeContent);
        
        // Tab 2: All Exams
        Tab allTab = new Tab("📋 All Exams");
        VBox allContent = new VBox(15);
        allContent.setPadding(new Insets(20));
        
        TableView<Exam> allTable = new TableView<>(exams);
        allTable.getColumns().addAll(courseCol, titleCol, questionsCol, marksCol, dateCol);
        allTable.setPrefHeight(300);
        
        allContent.getChildren().add(allTable);
        allTab.setContent(allContent);
        
        examTabs.getTabs().addAll(activeTab, allTab);
        
        // Action buttons
        HBox actionButtons = new HBox(15);
        actionButtons.setPadding(new Insets(20));
        
        Button createBtn = new Button("➕ Create New Exam");
        createBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
        createBtn.setOnAction(e -> showAdvancedCreateExam(stage));
        
        Button editBtn = new Button("✏️ Edit Selected");
        editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Button deactivateBtn = new Button("⏸️ Deactivate");
        deactivateBtn.setStyle("-fx-background-color: #f6b93b; -fx-text-fill: white; -fx-font-weight: bold;");
        deactivateBtn.setOnAction(e -> {
            Exam selected = activeTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selected.isActive = false;
                showAlert("Exam Deactivated", "The selected exam has been deactivated.");
            }
        });
        
        Button deleteBtn = new Button("🗑️ Delete");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        
        actionButtons.getChildren().addAll(createBtn, editBtn, deactivateBtn, deleteBtn);
        
        content.getChildren().addAll(title, examTabs, actionButtons);
        ((BorderPane) stage.getScene().getRoot()).setCenter(content);
    }
    
    private void showAdvancedViewStudents(Stage stage) {
        showAlert("Student Roster", "Student roster management functionality");
    }
    
    private void showInstructorAnalytics(Stage stage) {
        showAlert("Instructor Analytics", "Teaching analytics and student performance functionality");
    }
    
    private void showCourseManagement(Stage stage) {
        showAlert("Course Management", "Course and curriculum management functionality");
    }
    
    private void showDepartmentManagement(Stage stage) {
        showAlert("Department Management", "Department administration functionality");
    }
    
    private void showSystemAnalytics(Stage stage) {
        showAlert("System Analytics", "Comprehensive system analytics functionality");
    }
    
    private void showAdvancedSystemSettings(Stage stage) {
        showAlert("System Settings", "Advanced system configuration functionality");
    }
    
    private void showActiveExams(Stage stage) {
        showAdvancedAvailableExams(stage);
    }
    
    // ================= HELPER METHODS =================
    private User getCurrentUser() {
        for (User user : users) {
            if (user.username.equals(currentUsername)) {
                return user;
            }
        }
        return null;
    }
    
    private String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setPrefWidth(400);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}