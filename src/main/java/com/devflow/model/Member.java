package com.devflow.model;

import java.sql.Timestamp;

/** A user's membership of one project. */
public class Member {
    private int userId;
    private String fullName, email, projectRole;
    private Timestamp joinedAt;

    public int getUserId() { return userId; }             public void setUserId(int v) { userId = v; }
    public String getFullName() { return fullName; }      public void setFullName(String v) { fullName = v; }
    public String getEmail() { return email; }            public void setEmail(String v) { email = v; }
    public String getProjectRole() { return projectRole; } public void setProjectRole(String v) { projectRole = v; }
    public Timestamp getJoinedAt() { return joinedAt; }   public void setJoinedAt(Timestamp v) { joinedAt = v; }

    public String getInitials() {
        String[] p = fullName.trim().split("\\s+");
        return (p.length == 1 ? p[0].substring(0, 1) : p[0].substring(0, 1) + p[p.length - 1].substring(0, 1)).toUpperCase();
    }
}
