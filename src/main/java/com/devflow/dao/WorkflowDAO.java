package com.devflow.dao;

import java.sql.*;
import java.util.*;

public class WorkflowDAO {

    /** Loads allowed transitions for a project: from-status mapped to the set of to-statuses. Empty if none stored. */
    public Map<String, Set<String>> loadTransitions(Connection con, int projectId) throws SQLException {
        String sql = "SELECT t.from_status, t.to_status FROM workflow_transitions t "
                   + "JOIN workflows w ON w.id = t.workflow_id WHERE w.project_id = ?";
        Map<String, Set<String>> result = new LinkedHashMap<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.computeIfAbsent(rs.getString(1), k -> new LinkedHashSet<>()).add(rs.getString(2));
                }
            }
        }
        return result;
    }

    public void createWorkflow(Connection con, int projectId, String name, Map<String, List<String>> transitions) throws SQLException {
        int workflowId;
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO workflows (project_id, workflow_name) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, projectId);
            ps.setString(2, name);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { keys.next(); workflowId = keys.getInt(1); }
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO workflow_transitions (workflow_id, from_status, to_status) VALUES (?, ?, ?)")) {
            for (Map.Entry<String, List<String>> e : transitions.entrySet()) {
                for (String to : e.getValue()) {
                    ps.setInt(1, workflowId);
                    ps.setString(2, e.getKey());
                    ps.setString(3, to);
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        }
    }
}
