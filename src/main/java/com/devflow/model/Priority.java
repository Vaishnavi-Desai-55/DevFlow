package com.devflow.model;

public enum Priority {
    LOWEST("Lowest"), LOW("Low"), MEDIUM("Medium"), HIGH("High"), CRITICAL("Critical");

    private final String label;
    Priority(String label) { this.label = label; }
    public String getLabel() { return label; }
    public String getName() { return name(); }

    public static Priority from(String s) {
        try { return valueOf(s); } catch (Exception e) { return null; }
    }
}
