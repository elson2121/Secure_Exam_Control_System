package com.secs.shared;

import java.io.Serializable;
import java.util.List;

public class Exam implements Serializable {
    private String id;
    private String title;
    private String description;
    private int durationMinutes;
    private List<Question> questions;
    private boolean isActive;

    public Exam() {}

    public Exam(String id, String title, String description, int durationMinutes) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.isActive = true;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return title + " (" + durationMinutes + " minutes)";
    }
}