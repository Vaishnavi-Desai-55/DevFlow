package com.devflow.service;

import com.devflow.dao.IssueDAO;
import com.devflow.dao.WorkflowDAO;
import com.devflow.model.Issue;
import com.devflow.model.Status;
import com.devflow.model.User;
import com.devflow.util.ServiceException;
import com.devflow.util.Tx;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * The workflow engine: the ONLY place that decides whether an issue may move from one status to another.
 * The Kanban board, the issue page and any future API all go through transition().
 *
 * Transitions are stored per project in the workflow_transitions table. If a project has none stored,
 * the default workflow below is used. To add a status later: add it to the Status enum and to the table.
 */
public class WorkflowService {
    private final IssueDAO issues = new IssueDAO();
    private final WorkflowDAO workflowDAO = new WorkflowDAO();
    private final ProjectService projectService = new ProjectService();
    private final IssueHistoryService history = new IssueHistoryService();

    /** OPEN to IN_PROGRESS to IN_REVIEW to TESTING to DONE, with the allowed back-steps. */
    public static Map<String, List<String>> defaultTransitions() {
        Map<String, List<String>> t = new LinkedHashMap<>();
        t.put("OPEN",        List.of("IN_PROGRESS"));
        t.put("IN_PROGRESS", List.of("IN_REVIEW"));
        t.put("IN_REVIEW",   List.of("IN_PROGRESS", "TESTING"));
        t.put("TESTING",     List.of("IN_PROGRESS", "DONE"));
        t.put("DONE",        List.of("OPEN"));
        return t;
    }

    /** Pure rule check (no database), so it is easy to unit test. */
    public static boolean isAllowed(Map<String, ? extends Collection<String>> transitions, String from, String to) {
        Collection<String> allowed = transitions.get(from);
        return allowed != null && allowed.contains(to);
    }

    /** Statuses reachable from "from" for this project (stored workflow, or the default one). */
    public Set<String> allowedFrom(Connection con, int projectId, String from) throws SQLException {
        Map<String, Set<String>> stored = workflowDAO.loadTransitions(con, projectId);
        if (stored.isEmpty()) {
            List<String> d = defaultTransitions().get(from);
            return d == null ? Set.of() : new LinkedHashSet<>(d);
        }
        return stored.getOrDefault(from, Set.of());
    }

    /** Same as allowedFrom but as Status objects in board order (for buttons on the issue page). */
    public List<Status> nextStatuses(Connection con, int projectId, String from) throws SQLException {
        Set<String> allowed = allowedFrom(con, projectId, from);
        List<Status> list = new ArrayList<>();
        for (Status s : Status.values()) if (allowed.contains(s.name())) list.add(s);
        return list;
    }

    /**
     * Moves an issue to a new status after validating: the status exists, the user may change this issue,
     * and the transition is allowed from the CURRENT status. The status change and its audit row are one transaction.
     */
    public Issue transition(User user, int issueId, String toStatus) {
        if (Status.from(toStatus) == null) throw ServiceException.validation("Unknown status: " + toStatus);

        return Tx.run(con -> {
            Issue issue = issues.findById(con, issueId);
            if (issue == null) throw ServiceException.notFound("Issue not found.");
            projectService.requireWrite(con, user, issue.getProjectId());

            String current = issues.lockStatus(con, issueId);   // lock so two people can't move it at once
            if (current.equals(toStatus))
                throw ServiceException.validation("Issue is already " + Status.from(current).getLabel() + ".");
            if (!allowedFrom(con, issue.getProjectId(), current).contains(toStatus))
                throw new ServiceException(ServiceException.Kind.INVALID_TRANSITION,
                        "Cannot move issue from " + current + " directly to " + toStatus + ".");

            issues.updateStatus(con, issueId, toStatus);
            history.record(con, issueId, user.getId(), "STATUS_CHANGED", "status", current, toStatus);
            return issues.findById(con, issueId);
        });
    }
}
