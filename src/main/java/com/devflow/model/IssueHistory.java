package com.devflow.model;

import java.sql.Timestamp;

public class IssueHistory {
    private int id, issueId, userId;
    private String userName, actionType, fieldName, oldValue, newValue;
    private Timestamp createdAt;

    public int getId() { return id; }                       public void setId(int v) { id = v; }
    public int getIssueId() { return issueId; }             public void setIssueId(int v) { issueId = v; }
    public int getUserId() { return userId; }               public void setUserId(int v) { userId = v; }
    public String getUserName() { return userName; }        public void setUserName(String v) { userName = v; }
    public String getActionType() { return actionType; }    public void setActionType(String v) { actionType = v; }
    public String getFieldName() { return fieldName; }      public void setFieldName(String v) { fieldName = v; }
    public String getOldValue() { return oldValue; }        public void setOldValue(String v) { oldValue = v; }
    public String getNewValue() { return newValue; }        public void setNewValue(String v) { newValue = v; }
    public Timestamp getCreatedAt() { return createdAt; }   public void setCreatedAt(Timestamp v) { createdAt = v; }

    /** Human-readable action text for the timeline. */
    public String getActionText() {
        switch (actionType) {
            case "CREATED": return "created this issue";
            case "STATUS_CHANGED": return "changed status";
            case "PRIORITY_CHANGED": return "changed priority";
            case "ASSIGNEE_CHANGED": return "changed assignee";
            case "TITLE_CHANGED": return "changed the title";
            case "DESCRIPTION_CHANGED": return "changed the description";
            case "DUE_DATE_CHANGED": return "changed the due date";
            case "COMMENT_ADDED": return "added a comment";
            case "COMMENT_EDITED": return "edited a comment";
            case "COMMENT_DELETED": return "deleted a comment";
            case "LINK_ADDED": return "linked a blocked issue";
            default: return actionType.toLowerCase().replace('_', ' ');
        }
    }
}
