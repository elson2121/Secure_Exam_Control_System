package com.secs.server;

import com.secs.shared.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class ExamServiceImpl extends UnicastRemoteObject implements ExamService {

    private AuthManager authManager;
    private ExamManager examManager;

    public ExamServiceImpl() throws RemoteException {
        super();
        this.authManager = new AuthManager();
        this.examManager = new ExamManager();
        System.out.println("ExamServiceImpl initialized");
    }

    @Override
    public User login(String username, String password) throws RemoteException {
        System.out.println("Login attempt: " + username);
        User user = authManager.authenticate(username, password);
        if (user != null) {
            System.out.println("Login successful for: " + user.getName());
        }
        return user;
    }

    @Override
    public List<Exam> getAvailableExams() throws RemoteException {
        List<Exam> exams = examManager.getAllExams();
        System.out.println("Returning " + exams.size() + " available exams");
        return exams;
    }

    @Override
    public Exam getExamById(String examId) throws RemoteException {
        Exam exam = examManager.getExamById(examId);
        if (exam == null) {
            throw new RemoteException("Exam not found: " + examId);
        }
        System.out.println("Returning exam: " + exam.getTitle());
        return exam;
    }

    @Override
    public Result submitExam(String studentId, String examId, List<String> answers) throws RemoteException {
        System.out.println("Submitting exam - Student: " + studentId + ", Exam: " + examId);
        System.out.println("Answers received: " + answers.size());

        Exam exam = examManager.getExamById(examId);
        if (exam == null) {
            throw new RemoteException("Exam not found: " + examId);
        }

        if (answers.size() != exam.getQuestions().size()) {
            System.out.println("Warning: Answer count mismatch. Expected: " +
                    exam.getQuestions().size() + ", Got: " + answers.size());
        }

        int score = 0;
        int totalPoints = 0;

        for (int i = 0; i < exam.getQuestions().size(); i++) {
            Question question = exam.getQuestions().get(i);
            totalPoints += question.getPoints();

            if (i < answers.size() && answers.get(i) != null) {
                String answer = answers.get(i).trim();
                String correctAnswer = question.getCorrectAnswer().trim();

                // For short answer questions, accept partial matches
                if ("short_answer".equals(question.getType())) {
                    // Simple partial match (could be improved)
                    if (answer.toLowerCase().contains(correctAnswer.toLowerCase().substring(0, Math.min(10, correctAnswer.length())))) {
                        score += question.getPoints();
                        System.out.println("Question " + (i+1) + ": Correct (partial match)");
                    } else {
                        System.out.println("Question " + (i+1) + ": Incorrect");
                    }
                } else {
                    // For MCQ and True/False, exact match
                    if (answer.equalsIgnoreCase(correctAnswer)) {
                        score += question.getPoints();
                        System.out.println("Question " + (i+1) + ": Correct");
                    } else {
                        System.out.println("Question " + (i+1) + ": Incorrect");
                    }
                }
            }
        }

        Result result = new Result(studentId, examId, score, totalPoints);
        result.setId("R" + System.currentTimeMillis());

        examManager.saveResult(result);

        System.out.println("Exam submitted. Score: " + score + "/" + totalPoints +
                " (" + String.format("%.1f", result.getPercentage()) + "%)");

        return result;
    }

    @Override
    public List<Result> getStudentResults(String studentId) throws RemoteException {
        List<Result> results = examManager.getStudentResults(studentId);
        System.out.println("Returning " + results.size() + " results for student: " + studentId);
        return results;
    }

    @Override
    public boolean isAlive() throws RemoteException {
        return true;
    }

    public String getServerStats() {
        return String.format("Users: %d, Exams: %d, Results: %d",
                authManager.getTotalUsers(),
                examManager.getTotalExams(),
                examManager.getTotalResults());
    }
}