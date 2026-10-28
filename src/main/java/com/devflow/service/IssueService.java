package com.devflow.service;

import com.devflow.dao.CommentDAO;
import com.devflow.dao.IssueDAO;
import com.devflow.dao.ProjectDAO;
import com.devflow.dao.UserDAO;
import com.devflow.model.*;
import com.devflow.util.ServiceException;
import com.devflow.util.Tx;
import com.devflow.util.ValidationUtil;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class IssueService {
    private final IssueDAO issues = new IssueDAO();
    private final ProjectDAO projectDAO = new ProjectDAO();
    private final CommentDAO comments = new CommentDAO();
    private final UserDAO users = new UserDAO();
    private final ProjectService projectService = new ProjectService();
    private final WorkflowService workflow = new WorkflowService();
    private final IssueHistoryService history = new IssueHistoryService();
    private final PriorityRecommendationService priorityEngine = new PriorityRecommendationService();

    // ---------- permission rules (also used by the servlets to show/hide buttons) ----------

    /** Admins edit anything; developers edit issues they reported or are assigned to. */
    public static boolean canEdit(String role, User u, Issue i) {
        if ("ADMIN".equals(role)) return true;
        return "DEVELOPER".equals(role) && (i.getReporterId() == u.getId() || Integer.valueOf(u.getId()).equals(i.getAssigneeId()));
    }

    public static boolean canDelete(String role, User u, Issue i) {
        return "ADMIN".equals(role) || ("DEVELOPER".equals(role) && i.getReporterId() == u.getId());
    }

    // ---------- create ----------

    public Issue create(User user, Issue draft) {
        draft.setTitle(ValidationUtil.trim(draft.getTitle()));
        draft.setDescription(ValidationUtil.trim(draft.getDescription()));
        validateText(draft.getTitle(), draft.getDescription());
        if (IssueType.from(draft.getIssueType()) == null) throw ServiceException.validation("Please choose an issue type.");
        if (draft.getPriority() == null) draft.setPriority("MEDIUM");
        if (Priority.from(draft.getPriority()) == null) throw ServiceException.validation("Please choose a valid priority.");

        return Tx.run(con -> {
            Project p = projectDAO.findById(con, draft.getProjectId());
            if (p == null) throw ServiceException.notFound("Project not found.");
            projectService.requireWrite(con, user, p.getId());
            if (p.isArchived()) throw ServiceException.validation("This project is archived. Re-activate it to add issues.");

            validateAssignee(con, p.getId(), draft.getAssigneeId());
            validateParent(con, p.getId(), draft);

            // Lock the project row, take the next number, save the issue, bump the counter: all in ONE transaction.
            int number = projectDAO.lockNextIssueNumber(con, p.getId());
            draft.setIssueNumber(number);
            draft.setIssueKey(p.getKey() + "-" + number);
            draft.setReporterId(user.getId());
            int id = issues.insert(con, draft);
            projectDAO.bumpIssueNumber(con, p.getId());
            history.record(con, id, user.getId(), "CREATED", "issue", null, draft.getIssueKey());
            return issues.findById(con, id);
        });
    }

    private void validateText(String title, String description) {
        if (ValidationUtil.isBlank(title) || title.length() < 3 || title.length() > 255)
            throw ServiceException.validation("Title must be 3 to 255 characters.");
        if (description != null && description.length() > 5000)
            throw ServiceException.validation("Description is too long (max 5000 characters).");
    }

    /** An assignee must be a member of the project and must not be a read-only viewer. */
    private void validateAssignee(Connection con, int projectId, Integer assigneeId) throws SQLException {
        if (assigneeId == null) return;
        String role = projectDAO.getMemberRole(con, projectId, assigneeId);
        if (role == null || !Role.canWrite(role))
            throw ServiceException.validation("Invalid assignee: choose a developer or admin who is a member of this project.");
    }

    private void validateParent(Connection con, int projectId, Issue draft) throws SQLException {
        boolean subtask = "SUBTASK".equals(draft.getIssueType());
        if (!subtask) { draft.setParentIssueId(null); return; }
        if (draft.getParentIssueId() == null) throw ServiceException.validation("A subtask needs a parent issue.");
        Issue parent = issues.findById(con, draft.getParentIssueId());
        if (parent == null || parent.getProjectId() != projectId || "SUBTASK".equals(parent.getIssueType()))
            throw ServiceException.validation("Invalid parent issue.");
    }

    // ---------- update ----------

    public Issue update(User user, int issueId, String title, String description, String priority, Integer assigneeId, Date dueDate) {
        title = ValidationUtil.trim(title);
        description = ValidationUtil.trim(description);
        validateText(title, description);
        if (Priority.from(priority) == null) throw ServiceException.validation("Please choose a valid priority.");
        final String t = title, d = ValidationUtil.isBlank(description) ? null : description;

        return Tx.run(con -> {
            Issue issue = issues.findById(con, issueId);
            if (issue == null) throw ServiceException.notFound("Issue not found.");
            String role = projectService.requireWrite(con, user, issue.getProjectId());
            if (!canEdit(role, user, issue))
                throw ServiceException.forbidden("You can only edit issues you reported or are assigned to.");
            if (!Objects.equals(assigneeId, issue.getAssigneeId())) validateAssignee(con, issue.getProjectId(), assigneeId);

            int uid = user.getId();
            if (!Objects.equals(t, issue.getTitle()))
                history.record(con, issueId, uid, "TITLE_CHANGED", "title", issue.getTitle(), t);
            if (!Objects.equals(d, issue.getDescription()))
                history.record(con, issueId, uid, "DESCRIPTION_CHANGED", "description", issue.getDescription(), d);
            if (!priority.equals(issue.getPriority()))
                history.record(con, issueId, uid, "PRIORITY_CHANGED", "priority", issue.getPriority(), priority);
            if (!Objects.equals(assigneeId, issue.getAssigneeId())) {
                String oldName = issue.getAssigneeName() == null ? "Unassigned" : issue.getAssigneeName();
                String newName = assigneeId == null ? "Unassigned" : users.findById(con, assigneeId).getFullName();
                history.record(con, issueId, uid, "ASSIGNEE_CHANGED", "assignee", oldName, newName);
            }
            if (!Objects.equals(dueDate, issue.getDueDate()))
                history.record(con, issueId, uid, "DUE_DATE_CHANGED", "due_date",
                        issue.getDueDate() == null ? "none" : issue.getDueDate().toString(), dueDate == null ? "none" : dueDate.toString());

            issue.setTitle(t);
            issue.setDescription(d);
            issue.setPriority(priority);
            issue.setAssigneeId(assigneeId);
            issue.setDueDate(dueDate);
            issues.update(con, issue);
            return issues.findById(con, issueId);
        });
    }

    /** Used when the user clicks "Accept" on the priority recommendation. */
    public void changePriority(User user, int issueId, String priority) {
        if (Priority.from(priority) == null) throw ServiceException.validation("Please choose a valid priority.");
        Tx.run(con -> {
            Issue issue = issues.findById(con, issueId);
            if (issue == null) throw ServiceException.notFound("Issue not found.");
            projectService.requireWrite(con, user, issue.getProjectId());
            String old = issue.getPriority();
            if (!priority.equals(old)) {
                issue.setPriority(priority);
                issues.update(con, issue);
                history.record(con, issueId, user.getId(), "PRIORITY_CHANGED", "priority", old, priority);
            }
            return null;
        });
    }

    public int delete(User user, int issueId) {
        return Tx.run(con -> {
            Issue issue = issues.findById(con, issueId);
            if (issue == null) throw ServiceException.notFound("Issue not found.");
            String role = projectService.requireWrite(con, user, issue.getProjectId());
            if (!canDelete(role, user, issue)) throw ServiceException.forbidden("You can only delete issues you reported.");
            issues.delete(con, issueId);
            return issue.getProjectId();
        });
    }

    // ---------- blocking links ----------

    public void addBlocksLink(User user, int blockerId, String blockedKey) {
        String key = blockedKey == null ? "" : blockedKey.trim().toUpperCase();
        Tx.run(con -> {
            Issue blocker = issues.findById(con, blockerId);
            if (blocker == null) throw ServiceException.notFound("Issue not found.");
            projectService.requireWrite(con, user, blocker.getProjectId());
            Issue blocked = issues.findByKey(con, key);
            if (blocked == null || blocked.getProjectId() != blocker.getProjectId())
                throw ServiceException.validation("No issue with key " + key + " in this project.");
            if (blocked.getId() == blocker.getId()) throw ServiceException.validation("An issue cannot block itself.");
            if (issues.listBlocks(con, blockerId).stream().anyMatch(i -> i.getId() == blocked.getId()))
                throw ServiceException.conflict(blocker.getIssueKey() + " already blocks " + key + ".");
            if (issues.listBlocks(con, blocked.getId()).stream().anyMatch(i -> i.getId() == blockerId))
                throw ServiceException.validation(key + " already blocks " + blocker.getIssueKey() + " (circular link).");
            issues.addLink(con, blockerId, blocked.getId());
            history.record(con, blockerId, user.getId(), "LINK_ADDED", "blocks", null, key);
            return null;
        });
    }

    // ---------- reading ----------

    public IssueDetail detail(User user, int issueId) {
        return Tx.read(con -> {
            Issue issue = issues.findById(con, issueId);
            if (issue == null) throw ServiceException.notFound("Issue not found.");
            String role = projectService.requireAccess(con, user, issue.getProjectId());

            IssueDetail d = new IssueDetail();
            d.issue = issue;
            d.myRole = role;
            d.canWrite = Role.canWrite(role);
            d.canEdit = canEdit(role, user, issue);
            d.canDelete = canDelete(role, user, issue);
            d.comments = comments.listByIssue(con, issueId);
            d.history = history.list(con, issueId);
            d.subtasks = issues.listSubtasks(con, issueId);
            d.blocks = issues.listBlocks(con, issueId);
            d.blockedBy = issues.listBlockedBy(con, issueId);
            d.nextStatuses = d.canWrite ? workflow.nextStatuses(con, issue.getProjectId(), issue.getStatus()) : List.of();
            d.assignees = assignable(con, issue.getProjectId());
            d.recommendation = priorityEngine.recommend(issue, issues.countOpenBlocked(con, issueId));
            return d;
        });
    }

    private List<Member> assignable(Connection con, int projectId) throws SQLException {
        return projectDAO.listMembers(con, projectId).stream()
                .filter(m -> Role.canWrite(m.getProjectRole())).collect(Collectors.toList());
    }

    /** Members who can be assigned issues in a project (for forms). */
    public List<Member> assignees(User user, int projectId) {
        return Tx.read(con -> {
            projectService.requireAccess(con, user, projectId);
            return assignable(con, projectId);
        });
    }

    public List<Issue> parentCandidates(User user, int projectId) {
        return Tx.read(con -> {
            projectService.requireAccess(con, user, projectId);
            return issues.listParentCandidates(con, projectId);
        });
    }

    public PageResult<Issue> search(User user, IssueFilter f) {
        return Tx.read(con -> {
            if (f.getProjectId() != null) projectService.requireAccess(con, user, f.getProjectId());
            int total = issues.count(con, user, f);
            return new PageResult<>(issues.search(con, user, f), total, f.getPage(), f.getSize());
        });
    }

    public List<Issue> board(User user, int projectId) {
        return Tx.read(con -> {
            projectService.requireAccess(con, user, projectId);
            return issues.listByProject(con, projectId);
        });
    }
}
