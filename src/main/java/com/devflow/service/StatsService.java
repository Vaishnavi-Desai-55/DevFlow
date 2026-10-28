package com.devflow.service;

import com.devflow.dao.StatsDAO;
import com.devflow.model.Priority;
import com.devflow.model.Status;
import com.devflow.model.User;
import com.devflow.util.Tx;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Numbers for the dashboard and analytics charts. All values come from MySQL. */
public class StatsService {
    private final StatsDAO dao = new StatsDAO();
    private final ProjectService projectService = new ProjectService();

    /** projectId == null means "all projects this user can see". */
    public Map<String, Object> compute(User user, Integer projectId) {
        return Tx.read(con -> {
            if (projectId != null) projectService.requireAccess(con, user, projectId);

            Map<String, Integer> rawStatus = dao.byStatus(con, user, projectId);
            Map<String, Integer> status = new LinkedHashMap<>();
            int total = 0;
            for (Status s : Status.values()) {
                int n = rawStatus.getOrDefault(s.name(), 0);
                status.put(s.getLabel(), n);
                total += n;
            }
            Map<String, Integer> rawPriority = dao.byPriority(con, user, projectId);
            Map<String, Integer> priority = new LinkedHashMap<>();
            for (Priority p : Priority.values()) priority.put(p.getLabel(), rawPriority.getOrDefault(p.name(), 0));

            List<Map<String, Object>> projects = dao.projectCompletion(con, user, projectId);

            Map<String, Object> out = new LinkedHashMap<>();
            out.put("totalProjects", projects.size());
            out.put("totalIssues", total);
            out.put("open", rawStatus.getOrDefault("OPEN", 0));
            out.put("inProgress", rawStatus.getOrDefault("IN_PROGRESS", 0));
            out.put("inReview", rawStatus.getOrDefault("IN_REVIEW", 0));
            out.put("testing", rawStatus.getOrDefault("TESTING", 0));
            out.put("done", rawStatus.getOrDefault("DONE", 0));
            out.put("overdue", dao.overdue(con, user, projectId));
            out.put("assignedToMe", dao.assignedTo(con, user, projectId));
            out.put("byStatus", status);
            out.put("byPriority", priority);
            out.put("byAssignee", dao.byAssignee(con, user, projectId));
            out.put("projects", projects);
            return out;
        });
    }
}
