package com.devflow.model;

public enum IssueType {
    STORY("Story"), TASK("Task"), BUG("Bug"), SUBTASK("Subtask");

    private final String label;
    IssueType(String label) { this.label = label; }
    public String getLabel() { return label; }
    public String getName() { return name(); }

    public static IssueType from(String s) {
        try { return valueOf(s); } catch (Exception e) { return null; }
    }
}
