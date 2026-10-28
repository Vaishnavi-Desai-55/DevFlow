package com.devflow.dao;

import com.devflow.model.User;

import java.sql.*;
import java.util.*;

/** Aggregate queries for the dashboard and analytics pages. */
public class StatsDAO {

    /** Which projects are counted: one project, or everything the user may see. */
    private static final class Scope {
        final String sql;
        final List<Object> params = new ArrayList<>();
        Scope(User user, Integer projectId, String column) {
            if (projectId != null) { sql = " AND " + column + " = ?"; params.add(projectId); }
            else if (!user.isGlobalAdmin()) {
                sql = " AND " + column + " IN (SELECT pm.project_id FROM project_members pm WHERE pm.user_id = ?)";
                params.add(user.getId());
            } else sql = "";
        }
    }

    private void bind(PreparedStatement ps, int start, List<Object> params) throws SQLException {
        for (int n = 0; n < params.size(); n++) ps.setObject(start + n, params.get(n));
    }

    private Map<String, Integer> groupCount(Connection con, String sql, Scope s) throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, 1, s.params);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) map.put(rs.getString(1), rs.getInt(2)); }
        }
        return map;
    }

    public Map<String, Integer> byStatus(Connection con, User u, Integer projectId) throws SQLException {
        Scope s = new Scope(u, projectId, "i.project_id");
        return groupCount(con, "SELECT i.status, COUNT(*) FROM issues i WHERE 1=1" + s.sql + " GROUP BY i.status", s);
    }

    public Map<String, Integer> byPriority(Connection con, User u, Integer projectId) throws SQLException {
        Scope s = new Scope(u, projectId, "i.project_id");
        return groupCount(con, "SELECT i.priority, COUNT(*) FROM issues i WHERE 1=1" + s.sql + " GROUP BY i.priority", s);
    }

    public Map<String, Integer> byAssignee(Connection con, User u, Integer projectId) throws SQLException {
        Scope s = new Scope(u, projectId, "i.project_id");
        String sql = "SELECT COALESCE(a.full_name, 'Unassigned'), COUNT(*) FROM issues i LEFT JOIN users a ON a.id = i.assignee_id "
                   + "WHERE 1=1" + s.sql + " GROUP BY a.id, a.full_name ORDER BY COUNT(*) DESC LIMIT 10";
        return groupCount(con, sql, s);
    }

    public int overdue(Connection con, User u, Integer projectId) throws SQLException {
        Scope s = new Scope(u, projectId, "i.project_id");
        String sql = "SELECT COUNT(*) FROM issues i WHERE i.due_date < CURDATE() AND i.status <> 'DONE'" + s.sql;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, 1, s.params);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        }
    }

    /** Unfinished issues assigned to this user. */
    public int assignedTo(Connection con, User u, Integer projectId) throws SQLException {
        Scope s = new Scope(u, projectId, "i.project_id");
        String sql = "SELECT COUNT(*) FROM issues i WHERE i.assignee_id = ? AND i.status <> 'DONE'" + s.sql;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, u.getId());
            bind(ps, 2, s.params);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        }
    }

    /** One row per project: name, key, total issues, done issues. */
    public List<Map<String, Object>> projectCompletion(Connection con, User u, Integer projectId) throws SQLException {
        Scope s = new Scope(u, projectId, "p.id");
        String sql = "SELECT p.name, p.project_key, COUNT(i.id) AS total, COALESCE(SUM(i.status = 'DONE'), 0) AS done "
                   + "FROM projects p LEFT JOIN issues i ON i.project_id = p.id WHERE 1=1" + s.sql
                   + " GROUP BY p.id, p.name, p.project_key ORDER BY p.name";
        List<Map<String, Object>> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, 1, s.params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    int total = rs.getInt("total"), done = rs.getInt("done");
                    row.put("name", rs.getString("name"));
                    row.put("key", rs.getString("project_key"));
                    row.put("total", total);
                    row.put("done", done);
                    row.put("percent", total == 0 ? 0 : Math.round(done * 100f / total));
                    list.add(row);
                }
            }
        }
        return list;
    }
}
