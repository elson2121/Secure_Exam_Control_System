package com.secs.shared;

import java.io.Serializable;
import java.util.Date;

public class Result implements Serializable {
    private String id;
    private String studentId;
    private String examId;
    private int score;
    private int totalPoints;
    private Date completedAt;
    private boolean passed;

    public Result() {}

    public Result(String studentId, String examId, int score, int totalPoints) {
        this.studentId = studentId;
        this.examId = examId;
        this.score = score;
        this.totalPoints = totalPoints;
        this.completedAt = new Date();
        this.passed = score >= (totalPoints * 0.6); // 60% to pass
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getExamId() { return examId; }
    public void setExamId(String examId) { this.examId = examId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }

    public Date getCompletedAt() { return completedAt; }
    public void setCompletedAt(Date completedAt) { this.completedAt = completedAt; }

    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }

    public double getPercentage() {
        return totalPoints > 0 ? (score * 100.0) / totalPoints : 0;
    }

    @Override
    public String toString() {
        return "Score: " + score + "/" + totalPoints + " (" + String.format("%.1f", getPercentage()) + "%)";
    }
}