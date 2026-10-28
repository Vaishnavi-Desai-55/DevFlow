package com.devflow.model;

import java.util.List;

/** Everything the issue-details page needs, gathered in one object. */
public class IssueDetail {
    public Issue issue;
    public String myRole;
    public boolean canWrite, canEdit, canDelete;
    public List<Comment> comments;
    public List<IssueHistory> history;
    public List<Issue> subtasks, blocks, blockedBy;
    public List<Status> nextStatuses;
    public List<Member> assignees;
    public Recommendation recommendation;

    public Issue getIssue() { return issue; }
    public String getMyRole() { return myRole; }
    public boolean isCanWrite() { return canWrite; }
    public boolean isCanEdit() { return canEdit; }
    public boolean isCanDelete() { return canDelete; }
    public List<Comment> getComments() { return comments; }
    public List<IssueHistory> getHistory() { return history; }
    public List<Issue> getSubtasks() { return subtasks; }
    public List<Issue> getBlocks() { return blocks; }
    public List<Issue> getBlockedBy() { return blockedBy; }
    public List<Status> getNextStatuses() { return nextStatuses; }
    public List<Member> getAssignees() { return assignees; }
    public Recommendation getRecommendation() { return recommendation; }
}
