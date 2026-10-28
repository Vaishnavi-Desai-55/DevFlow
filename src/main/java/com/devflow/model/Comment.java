package com.devflow.model;

import java.sql.Timestamp;

public class Comment {
    private int id, issueId, userId;
    private String authorName, text;
    private Timestamp createdAt, updatedAt;

    public int getId() { return id; }                      public void setId(int v) { id = v; }
    public int getIssueId() { return issueId; }            public void setIssueId(int v) { issueId = v; }
    public int getUserId() { return userId; }              public void setUserId(int v) { userId = v; }
    public String getAuthorName() { return authorName; }   public void setAuthorName(String v) { authorName = v; }
    public String getText() { return text; }               public void setText(String v) { text = v; }
    public Timestamp getCreatedAt() { return createdAt; }  public void setCreatedAt(Timestamp v) { createdAt = v; }
    public Timestamp getUpdatedAt() { return updatedAt; }  public void setUpdatedAt(Timestamp v) { updatedAt = v; }

    public boolean isEdited() { return updatedAt != null && createdAt != null && updatedAt.getTime() - createdAt.getTime() > 1000; }
}
