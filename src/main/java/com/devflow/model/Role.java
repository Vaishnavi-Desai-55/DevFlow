package com.devflow.model;

public enum Role {
    ADMIN("Admin"), DEVELOPER("Developer"), VIEWER("Viewer");

    private final String label;
    Role(String label) { this.label = label; }
    public String getLabel() { return label; }
    public String getName() { return name(); }

    public static Role from(String s) {
        try { return valueOf(s); } catch (Exception e) { return null; }
    }

    /** Admins and developers may change data; viewers are read-only. */
    public static boolean canWrite(String role) {
        return "ADMIN".equals(role) || "DEVELOPER".equals(role);
    }
}
