package com.devflow.model;

import java.sql.Timestamp;

public class User {
    private int id;
    private String fullName, email, passwordHash, role;
    private Timestamp createdAt;

    public int getId() { return id; }                     public void setId(int v) { id = v; }
    public String getFullName() { return fullName; }      public void setFullName(String v) { fullName = v; }
    public String getEmail() { return email; }            public void setEmail(String v) { email = v; }
    public String getPasswordHash() { return passwordHash; } public void setPasswordHash(String v) { passwordHash = v; }
    public String getRole() { return role; }              public void setRole(String v) { role = v; }
    public Timestamp getCreatedAt() { return createdAt; } public void setCreatedAt(Timestamp v) { createdAt = v; }

    public boolean isGlobalAdmin() { return "ADMIN".equals(role); }

    /** Two-letter initials for avatar circles. */
    public String getInitials() {
        if (fullName == null || fullName.isBlank()) return "?";
        String[] p = fullName.trim().split("\\s+");
        String s = p.length == 1 ? p[0].substring(0, 1) : p[0].substring(0, 1) + p[p.length - 1].substring(0, 1);
        return s.toUpperCase();
    }
}
