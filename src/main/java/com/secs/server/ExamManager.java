package com.secs.server;

import com.secs.shared.Exam;
import com.secs.shared.Question;
import com.secs.shared.Result;
import java.util.*;

public class ExamManager {
    private Map<String, Exam> exams = new HashMap<>();
    private Map<String, List<Result>> results = new HashMap<>();

    public ExamManager() {
        initializeExams();
    }

    private void initializeExams() {
        // Exam 1: Java Basics
        Exam javaExam = new Exam("EX001", "Java Programming Basics",
                "Test your fundamental Java knowledge", 30);

        List<Question> javaQuestions = new ArrayList<>();

        Question q1 = new Question("Q1", "What is the main purpose of Java?",
                "multiple_choice", "A programming language", 10);
        q1.setOptions("A programming language", "A coffee brand", "An island", "A car model");

        Question q2 = new Question("Q2", "Java is platform independent (Write 'True' or 'False')",
                "true_false", "True", 10);

        Question q3 = new Question("Q3", "Which keyword is used for inheritance in Java?",
                "multiple_choice", "extends", 15);
        q3.setOptions("extends", "inherits", "uses", "implement");

        Question q4 = new Question("Q4", "Explain polymorphism in Java",
                "short_answer",
                "Polymorphism allows methods to do different things based on the object", 25);

        javaQuestions.add(q1);
        javaQuestions.add(q2);
        javaQuestions.add(q3);
        javaQuestions.add(q4);
        javaExam.setQuestions(javaQuestions);
        exams.put("EX001", javaExam);

        // Exam 2: OOP Concepts
        Exam oopExam = new Exam("EX002", "Object Oriented Programming",
                "Test your OOP knowledge", 45);

        List<Question> oopQuestions = new ArrayList<>();

        Question q5 = new Question("Q1", "What are the four pillars of OOP?",
                "multiple_choice", "All of these", 20);
        q5.setOptions("Encapsulation", "Inheritance", "Polymorphism", "All of these");

        Question q6 = new Question("Q2", "Abstraction hides implementation details (True/False)",
                "true_false", "True", 15);

        Question q7 = new Question("Q3", "What is encapsulation?",
                "short_answer",
                "Encapsulation is wrapping data and methods within one unit", 25);

        oopQuestions.add(q5);
        oopQuestions.add(q6);
        oopQuestions.add(q7);
        oopExam.setQuestions(oopQuestions);
        exams.put("EX002", oopExam);

        // Exam 3: Database Fundamentals
        Exam dbExam = new Exam("EX003", "Database Management Systems",
                "Basic database concepts", 40);

        List<Question> dbQuestions = new ArrayList<>();

        Question q8 = new Question("Q1", "What does SQL stand for?",
                "multiple_choice", "Structured Query Language", 10);
        q8.setOptions("Structured Query Language", "Simple Query Language",
                "Standard Query Language", "System Query Language");

        Question q9 = new Question("Q2", "Primary key uniquely identifies each record (True/False)",
                "true_false", "True", 10);

        Question q10 = new Question("Q3", "What is a foreign key?",
                "short_answer",
                "A foreign key is a field that references the primary key of another table", 30);

        dbQuestions.add(q8);
        dbQuestions.add(q9);
        dbQuestions.add(q10);
        dbExam.setQuestions(dbQuestions);
        exams.put("EX003", dbExam);

        System.out.println("Initialized " + exams.size() + " exams");
    }

    public List<Exam> getAllExams() {
        return new ArrayList<>(exams.values());
    }

    public Exam getExamById(String examId) {
        return exams.get(examId);
    }

    public void saveResult(Result result) {
        results.computeIfAbsent(result.getStudentId(), k -> new ArrayList<>()).add(result);
        System.out.println("Saved result for student: " + result.getStudentId() +
                ", Score: " + result.getScore() + "/" + result.getTotalPoints());
    }

    public List<Result> getStudentResults(String studentId) {
        List<Result> studentResults = results.getOrDefault(studentId, new ArrayList<>());
        System.out.println("Returning " + studentResults.size() + " results for student: " + studentId);
        return studentResults;
    }

    public List<Result> getAllResults() {
        List<Result> allResults = new ArrayList<>();
        results.values().forEach(allResults::addAll);
        return allResults;
    }

    public int getTotalExams() {
        return exams.size();
    }

    public int getTotalResults() {
        return results.values().stream().mapToInt(List::size).sum();
    }
}