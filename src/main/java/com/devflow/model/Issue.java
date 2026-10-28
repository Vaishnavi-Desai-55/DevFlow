package com.devflow.model;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Issue {
    private int id, projectId, issueNumber, reporterId;
    private Integer assigneeId, parentIssueId;
    private String projectKey, issueKey, title, description, issueType, priority, status;
    private String reporterName, assigneeName, parentKey;
    private Date dueDate;
    private Timestamp createdAt, updatedAt;

    public int getId() { return id; }                       public void setId(int v) { id = v; }
    public int getProjectId() { return projectId; }         public void setProjectId(int v) { projectId = v; }
    public int getIssueNumber() { return issueNumber; }     public void setIssueNumber(int v) { issueNumber = v; }
    public String getProjectKey() { return projectKey; }    public void setProjectKey(String v) { projectKey = v; }
    public String getIssueKey() { return issueKey; }        public void setIssueKey(String v) { issueKey = v; }
    public String getTitle() { return title; }              public void setTitle(String v) { title = v; }
    public String getDescription() { return description; }  public void setDescription(String v) { description = v; }
    public String getIssueType() { return issueType; }      public void setIssueType(String v) { issueType = v; }
    public String getPriority() { return priority; }        public void setPriority(String v) { priority = v; }
    public String getStatus() { return status; }            public void setStatus(String v) { status = v; }
    public int getReporterId() { return reporterId; }       public void setReporterId(int v) { reporterId = v; }
    public String getReporterName() { return reporterName; } public void setReporterName(String v) { reporterName = v; }
    public Integer getAssigneeId() { return assigneeId; }   public void setAssigneeId(Integer v) { assigneeId = v; }
    public String getAssigneeName() { return assigneeName; } public void setAssigneeName(String v) { assigneeName = v; }
    public Integer getParentIssueId() { return parentIssueId; } public void setParentIssueId(Integer v) { parentIssueId = v; }
    public String getParentKey() { return parentKey; }      public void setParentKey(String v) { parentKey = v; }
    public Date getDueDate() { return dueDate; }            public void setDueDate(Date v) { dueDate = v; }
    public Timestamp getCreatedAt() { return createdAt; }   public void setCreatedAt(Timestamp v) { createdAt = v; }
    public Timestamp getUpdatedAt() { return updatedAt; }   public void setUpdatedAt(Timestamp v) { updatedAt = v; }

    public boolean isDone() { return "DONE".equals(status); }

    /** Overdue = has a due date in the past and is not finished. */
    public boolean isOverdue() {
        return dueDate != null && !isDone() && dueDate.toLocalDate().isBefore(LocalDate.now());
    }

    /** Days until the due date (negative when overdue); null when there is no due date. */
    public Long getDaysUntilDue() {
        return dueDate == null ? null : ChronoUnit.DAYS.between(LocalDate.now(), dueDate.toLocalDate());
    }

    /** How many days ago the issue was created. */
    public long getDaysWaiting() {
        return createdAt == null ? 0 : Math.max(0, ChronoUnit.DAYS.between(createdAt.toLocalDateTime().toLocalDate(), LocalDate.now()));
    }
}
