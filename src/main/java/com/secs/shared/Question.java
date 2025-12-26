package com.secs.shared;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class Question implements Serializable {
    private String id;
    private String text;
    private String type; // "multiple_choice", "true_false", "short_answer"
    private List<String> options;
    private String correctAnswer;
    private int points;

    public Question() {}

    public Question(String id, String text, String type, String correctAnswer, int points) {
        this.id = id;
        this.text = text;
        this.type = type;
        this.correctAnswer = correctAnswer;
        this.points = points;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    public void setOptions(String... options) { this.options = Arrays.asList(options); }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    @Override
    public String toString() {
        return text.substring(0, Math.min(50, text.length())) + "...";
    }
}