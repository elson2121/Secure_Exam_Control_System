package com.secs.exam;

import com.secs.shared.User;
import java.time.LocalDateTime;
import java.util.*;

public class ExamSystem {
    private static ExamSystem instance;
    private Map<String, Exam> exams = new HashMap<>();
    private Map<String, List<ExamResult>> results = new HashMap<>();
    private Map<String, ExamSession> activeSessions = new HashMap<>();
    
    private ExamSystem() {
        initializeSampleData();
    }
    
    public static ExamSystem getInstance() {
        if (instance == null) {
            instance = new ExamSystem();
        }
        return instance;
    }
    
    public Exam createExam(String title, String description, User creator, 
                          int durationMinutes, int totalMarks) {
        String examId = "EXAM_" + UUID.randomUUID().toString().substring(0, 8);
        Exam exam = new Exam(examId, title, description, creator.getUsername(), 
                           durationMinutes, totalMarks);
        exams.put(examId, exam);
        System.out.println("Exam created: " + title + " by " + creator.getUsername());
        return exam;
    }
    
    public boolean updateExam(String examId, Exam updatedExam, User editor) {
        if (!exams.containsKey(examId)) return false;
        Exam exam = exams.get(examId);
        if (!exam.getCreatedBy().equals(editor.getUsername()) && !editor.isAdmin()) {
            return false;
        }
        exams.put(examId, updatedExam);
        return true;
    }
    
    public boolean deleteExam(String examId, User deleter) {
        if (!exams.containsKey(examId)) return false;
        Exam exam = exams.get(examId);
        if (!exam.getCreatedBy().equals(deleter.getUsername()) && !deleter.isAdmin()) {
            return false;
        }
        exams.remove(examId);
        return true;
    }
    
    public List<Exam> getExamsForUser(User user) {
        List<Exam> userExams = new ArrayList<>();
        for (Exam exam : exams.values()) {
            if (exam.isActive()) {
                if (user.isAdmin() || user.isTeacher()) {
                    userExams.add(exam);
                } else if (user.isStudent() && exam.isStudentAssigned(user.getUsername())) {
                    userExams.add(exam);
                }
            }
        }
        return userExams;
    }
    
    public ExamSession startExam(String examId, User student) {
        if (!exams.containsKey(examId)) return null;
        Exam exam = exams.get(examId);
        if (!exam.isStudentAssigned(student.getUsername())) return null;
        String sessionId = "SESSION_" + UUID.randomUUID().toString().substring(0, 8);
        ExamSession session = new ExamSession(sessionId, examId, student.getUsername());
        activeSessions.put(sessionId, session);
        return session;
    }
    
    public boolean submitAnswer(String sessionId, String questionId, String answer) {
        if (!activeSessions.containsKey(sessionId)) return false;
        ExamSession session = activeSessions.get(sessionId);
        session.addAnswer(questionId, answer);
        return true;
    }
    
    public ExamResult submitExam(String sessionId) {
        if (!activeSessions.containsKey(sessionId)) return null;
        ExamSession session = activeSessions.get(sessionId);
        Exam exam = exams.get(session.getExamId());
        int score = exam.calculateScore(session.getAnswers());
        ExamResult result = new ExamResult(
            UUID.randomUUID().toString().substring(0, 8),
            session.getExamId(),
            session.getStudentId(),
            score,
            exam.getTotalMarks(),
            LocalDateTime.now()
        );
        if (!results.containsKey(session.getStudentId())) {
            results.put(session.getStudentId(), new ArrayList<>());
        }
        results.get(session.getStudentId()).add(result);
        activeSessions.remove(sessionId);
        return result;
    }
    
    public List<ExamResult> getStudentResults(String studentId) {
        return results.getOrDefault(studentId, new ArrayList<>());
    }
    
    public List<ExamResult> getAllResults(User requester) {
        if (!requester.isAdmin() && !requester.isTeacher()) {
            return new ArrayList<>();
        }
        List<ExamResult> allResults = new ArrayList<>();
        for (List<ExamResult> studentResults : results.values()) {
            allResults.addAll(studentResults);
        }
        return allResults;
    }
    
    private void initializeSampleData() {
        Exam mathExam = new Exam("MATH001", "Mathematics Final", "Basic math concepts", "teacher1", 60, 100);
        mathExam.addQuestion(new Question("Q1", "What is 2+2?", Arrays.asList("3", "4", "5", "6"), 1, 10));
        mathExam.addQuestion(new Question("Q2", "What is √9?", Arrays.asList("2", "3", "4", "9"), 1, 10));
        mathExam.assignStudent("student1");
        mathExam.assignStudent("student2");
        
        Exam scienceExam = new Exam("SCI001", "Science Quiz", "General science", "teacher2", 30, 50);
        scienceExam.addQuestion(new Question("Q1", "Water is H2O?", Arrays.asList("True", "False"), 0, 10));
        scienceExam.assignStudent("student1");
        
        exams.put(mathExam.getExamId(), mathExam);
        exams.put(scienceExam.getExamId(), scienceExam);
    }
    
    // Inner classes
    public static class Exam {
        private String examId, title, description, createdBy;
        private LocalDateTime createdDate;
        private int durationMinutes, totalMarks;
        private boolean active;
        private List<Question> questions = new ArrayList<>();
        private Set<String> assignedStudents = new HashSet<>();
        
        public Exam(String examId, String title, String description, String createdBy, 
                   int durationMinutes, int totalMarks) {
            this.examId = examId;
            this.title = title;
            this.description = description;
            this.createdBy = createdBy;
            this.createdDate = LocalDateTime.now();
            this.durationMinutes = durationMinutes;
            this.totalMarks = totalMarks;
            this.active = true;
        }
        
        public String getExamId() { return examId; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getCreatedBy() { return createdBy; }
        public int getDurationMinutes() { return durationMinutes; }
        public int getTotalMarks() { return totalMarks; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        
        public void addQuestion(Question question) {
            questions.add(question);
        }
        
        public List<Question> getQuestions() {
            return new ArrayList<>(questions);
        }
        
        public void assignStudent(String studentId) {
            assignedStudents.add(studentId);
        }
        
        public boolean isStudentAssigned(String studentId) {
            return assignedStudents.contains(studentId);
        }
        
        public int calculateScore(Map<String, String> answers) {
            int score = 0;
            for (Question q : questions) {
                String answer = answers.get(q.getQuestionId());
                if (q.isCorrectAnswer(answer)) {
                    score += q.getPoints();
                }
            }
            return score;
        }
    }
    
    public static class Question {
        private String questionId, text;
        private List<String> options;
        private int correctOptionIndex, points;
        
        public Question(String questionId, String text, List<String> options, 
                       int correctOptionIndex, int points) {
            this.questionId = questionId;
            this.text = text;
            this.options = new ArrayList<>(options);
            this.correctOptionIndex = correctOptionIndex;
            this.points = points;
        }
        
        public String getQuestionId() { return questionId; }
        public String getText() { return text; }
        public List<String> getOptions() { return new ArrayList<>(options); }
        public int getPoints() { return points; }
        
        public boolean isCorrectAnswer(String answer) {
            if (answer == null) return false;
            try {
                int answerIndex = Integer.parseInt(answer);
                return answerIndex == correctOptionIndex;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        public String getCorrectAnswer() {
            if (correctOptionIndex >= 0 && correctOptionIndex < options.size()) {
                return options.get(correctOptionIndex);
            }
            return "";
        }
    }
    
    public static class ExamSession {
        private String sessionId, examId, studentId;
        private LocalDateTime startTime;
        private Map<String, String> answers = new HashMap<>();
        
        public ExamSession(String sessionId, String examId, String studentId) {
            this.sessionId = sessionId;
            this.examId = examId;
            this.studentId = studentId;
            this.startTime = LocalDateTime.now();
        }
        
        public String getSessionId() { return sessionId; }
        public String getExamId() { return examId; }
        public String getStudentId() { return studentId; }
        public LocalDateTime getStartTime() { return startTime; }
        public Map<String, String> getAnswers() { return new HashMap<>(answers); }
        
        public void addAnswer(String questionId, String answer) {
            answers.put(questionId, answer);
        }
    }
    
    public static class ExamResult {
        private String resultId, examId, studentId, grade;
        private int score, totalMarks;
        private LocalDateTime submissionTime;
        
        public ExamResult(String resultId, String examId, String studentId, 
                         int score, int totalMarks, LocalDateTime submissionTime) {
            this.resultId = resultId;
            this.examId = examId;
            this.studentId = studentId;
            this.score = score;
            this.totalMarks = totalMarks;
            this.submissionTime = submissionTime;
            this.grade = calculateGrade(score, totalMarks);
        }
        
        private String calculateGrade(int score, int total) {
            double percentage = (score * 100.0) / total;
            if (percentage >= 90) return "A";
            if (percentage >= 80) return "B";
            if (percentage >= 70) return "C";
            if (percentage >= 60) return "D";
            return "F";
        }
        
        public String getResultId() { return resultId; }
        public String getExamId() { return examId; }
        public String getStudentId() { return studentId; }
        public int getScore() { return score; }
        public int getTotalMarks() { return totalMarks; }
        public LocalDateTime getSubmissionTime() { return submissionTime; }
        public String getGrade() { return grade; }
        public double getPercentage() { return (score * 100.0) / totalMarks; }
    }
}