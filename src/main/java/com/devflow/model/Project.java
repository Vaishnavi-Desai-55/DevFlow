package com.devflow.model;

import java.sql.Timestamp;

public class Project {
    private int id, nextIssueNumber, createdBy, issueCount, doneCount, memberCount;
    private String name, key, description, status, createdByName, myRole;
    private Timestamp createdAt, updatedAt;

    public int getId() { return id; }                         public void setId(int v) { id = v; }
    public String getName() { return name; }                  public void setName(String v) { name = v; }
    public String getKey() { return key; }                    public void setKey(String v) { key = v; }
    public String getDescription() { return description; }    public void setDescription(String v) { description = v; }
    public String getStatus() { return status; }              public void setStatus(String v) { status = v; }
    public int getNextIssueNumber() { return nextIssueNumber; } public void setNextIssueNumber(int v) { nextIssueNumber = v; }
    public int getCreatedBy() { return createdBy; }           public void setCreatedBy(int v) { createdBy = v; }
    public String getCreatedByName() { return createdByName; } public void setCreatedByName(String v) { createdByName = v; }
    public Timestamp getCreatedAt() { return createdAt; }     public void setCreatedAt(Timestamp v) { createdAt = v; }
    public Timestamp getUpdatedAt() { return updatedAt; }     public void setUpdatedAt(Timestamp v) { updatedAt = v; }
    public int getIssueCount() { return issueCount; }         public void setIssueCount(int v) { issueCount = v; }
    public int getDoneCount() { return doneCount; }           public void setDoneCount(int v) { doneCount = v; }
    public int getMemberCount() { return memberCount; }       public void setMemberCount(int v) { memberCount = v; }
    public String getMyRole() { return myRole; }              public void setMyRole(String v) { myRole = v; }

    public int getProgress() { return issueCount == 0 ? 0 : Math.round(doneCount * 100f / issueCount); }
    public boolean isArchived() { return "ARCHIVED".equals(status); }
}
