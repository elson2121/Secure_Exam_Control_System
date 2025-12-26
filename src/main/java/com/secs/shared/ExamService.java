package com.secs.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ExamService extends Remote {
    // Authentication
    User login(String username, String password) throws RemoteException;

    // Exam operations
    List<Exam> getAvailableExams() throws RemoteException;
    Exam getExamById(String examId) throws RemoteException;

    // Result operations
    Result submitExam(String studentId, String examId, List<String> answers) throws RemoteException;
    List<Result> getStudentResults(String studentId) throws RemoteException;

    // Health check
    boolean isAlive() throws RemoteException;
}