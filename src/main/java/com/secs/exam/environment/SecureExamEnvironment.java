package com.secs.exam.environment;

public class SecureExamEnvironment {

    private static SecureExamEnvironment instance;

    private SecureExamEnvironment() {
        System.out.println("SecureExamEnvironment created");
    }

    public static SecureExamEnvironment getInstance() {
        if (instance == null) {
            instance = new SecureExamEnvironment();
        }
        return instance;
    }

    public void enableExamMode() {
        System.out.println("Exam mode enabled");
    }

    public void disableExamMode() {
        System.out.println("Exam mode disabled");
    }

    public boolean isExamModeActive() {
        return true;
    }
}