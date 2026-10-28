package com.devflow.dao;

import com.devflow.model.Issue;
import com.devflow.model.IssueFilter;
import com.devflow.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IssueDAO {

    private static final String SELECT =
        "SELECT i.*, p.project_key, r.full_name AS reporter_name, a.full_name AS assignee_name, par.issue_key AS parent_key "
      + "FROM issues i "
      + "JOIN projects p ON p.id = i.project_id "
      + "JOIN users r ON r.id = i.reporter_id "
      + "LEFT JOIN users a ON a.id = i.assignee_id "
      + "LEFT JOIN issues par ON par.id = i.parent_issue_id ";

    private Issue map(ResultSet rs) throws SQLException {
        Issue i = new Issue();
        i.setId(rs.getInt("id"));
        i.setProjectId(rs.getInt("project_id"));
        i.setProjectKey(rs.getString("project_key"));
        i.setIssueNumber(rs.getInt("issue_number"));
        i.setIssueKey(rs.getString("issue_key"));
        i.setTitle(rs.getString("title"));
        i.setDescription(rs.getString("description"));
        i.setIssueType(rs.getString("issue_type"));
        i.setPriority(rs.getString("priority"));
        i.setStatus(rs.getString("status"));
        i.setReporterId(rs.getInt("reporter_id"));
        i.setReporterName(rs.getString("reporter_name"));
        int assignee = rs.getInt("assignee_id");
        i.setAssigneeId(rs.wasNull() ? null : assignee);
        i.setAssigneeName(rs.getString("assignee_name"));
        int parent = rs.getInt("parent_issue_id");
        i.setParentIssueId(rs.wasNull() ? null : parent);
        i.setParentKey(rs.getString("parent_key"));
        i.setDueDate(rs.getDate("due_date"));
        i.setCreatedAt(rs.getTimestamp("created_at"));
        i.setUpdatedAt(rs.getTimestamp("updated_at"));
        return i;
    }

    public Issue findById(Connection con, int id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SELECT + "WHERE i.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        }
    }

    public Issue findByKey(Connection con, String key) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SELECT + "WHERE i.issue_key = ?")) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        }
    }

    /** Locks one issue row and returns its current status (used while changing status). */
    public String lockStatus(Connection con, int id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT status FROM issues WHERE id = ? FOR UPDATE")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getString(1) : null; }
        }
    }

    public int insert(Connection con, Issue i) throws SQLException {
        String sql = "INSERT INTO issues (project_id, issue_number, issue_key, title, description, issue_type, priority, status, "
                   + "reporter_id, assignee_id, parent_issue_id, due_date) VALUES (?, ?, ?, ?, ?, ?, ?, 'OPEN', ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, i.getProjectId());
            ps.setInt(2, i.getIssueNumber());
            ps.setString(3, i.getIssueKey());
            ps.setString(4, i.getTitle());
            ps.setString(5, i.getDescription());
            ps.setString(6, i.getIssueType());
            ps.setString(7, i.getPriority());
            ps.setInt(8, i.getReporterId());
            ps.setObject(9, i.getAssigneeId(), Types.INTEGER);
            ps.setObject(10, i.getParentIssueId(), Types.INTEGER);
            ps.setDate(11, i.getDueDate());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { keys.next(); return keys.getInt(1); }
        }
    }

    public void update(Connection con, Issue i) throws SQLException {
        String sql = "UPDATE issues SET title = ?, description = ?, priority = ?, assignee_id = ?, due_date = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, i.getTitle());
            ps.setString(2, i.getDescription());
            ps.setString(3, i.getPriority());
            ps.setObject(4, i.getAssigneeId(), Types.INTEGER);
            ps.setDate(5, i.getDueDate());
            ps.setInt(6, i.getId());
            ps.executeUpdate();
        }
    }

    public void updateStatus(Connection con, int id, String status) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE issues SET status = ? WHERE id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void delete(Connection con, int id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM issues WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Whole board for one project (newest first inside each column). */
    public List<Issue> listByProject(Connection con, int projectId) throws SQLException {
        List<Issue> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(SELECT + "WHERE i.project_id = ? ORDER BY i.updated_at DESC, i.id DESC")) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    public List<Issue> listSubtasks(Connection con, int parentId) throws SQLException {
        List<Issue> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(SELECT + "WHERE i.parent_issue_id = ? ORDER BY i.id")) {
            ps.setInt(1, parentId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    /** Issues that can be the parent of a subtask (same project, not a subtask themselves). */
    public List<Issue> listParentCandidates(Connection con, int projectId) throws SQLException {
        List<Issue> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(SELECT + "WHERE i.project_id = ? AND i.issue_type <> 'SUBTASK' ORDER BY i.id DESC")) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    // ---- blocking links ----

    public void addLink(Connection con, int blockerId, int blockedId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO issue_links (blocker_id, blocked_id) VALUES (?, ?)")) {
            ps.setInt(1, blockerId);
            ps.setInt(2, blockedId);
            ps.executeUpdate();
        }
    }

    /** Issues that THIS issue blocks. */
    public List<Issue> listBlocks(Connection con, int issueId) throws SQLException {
        return linked(con, "WHERE i.id IN (SELECT blocked_id FROM issue_links WHERE blocker_id = ?) ORDER BY i.id", issueId);
    }

    /** Issues that block THIS issue. */
    public List<Issue> listBlockedBy(Connection con, int issueId) throws SQLException {
        return linked(con, "WHERE i.id IN (SELECT blocker_id FROM issue_links WHERE blocked_id = ?) ORDER BY i.id", issueId);
    }

    private List<Issue> linked(Connection con, String where, int issueId) throws SQLException {
        List<Issue> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(SELECT + where)) {
            ps.setInt(1, issueId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    /** How many NOT-finished issues does this issue block? */
    public int countOpenBlocked(Connection con, int issueId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM issue_links l JOIN issues b ON b.id = l.blocked_id WHERE l.blocker_id = ? AND b.status <> 'DONE'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, issueId);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        }
    }

    // ---- search / filter / sort / paging ----

    /** Builds the WHERE part once so the COUNT query and the data query always agree. */
    private static final class Where {
        final StringBuilder sql = new StringBuilder(" WHERE 1=1");
        final List<Object> params = new ArrayList<>();
    }

    private Where buildWhere(User user, IssueFilter f) {
        Where w = new Where();
        if (!user.isGlobalAdmin()) {   // users only ever see issues of projects they belong to
            w.sql.append(" AND i.project_id IN (SELECT pm.project_id FROM project_members pm WHERE pm.user_id = ?)");
            w.params.add(user.getId());
        }
        if (f.getProjectId() != null) { w.sql.append(" AND i.project_id = ?"); w.params.add(f.getProjectId()); }
        if (f.getType() != null)      { w.sql.append(" AND i.issue_type = ?");  w.params.add(f.getType()); }
        if (f.getStatus() != null)    { w.sql.append(" AND i.status = ?");      w.params.add(f.getStatus()); }
        if (f.getPriority() != null)  { w.sql.append(" AND i.priority = ?");    w.params.add(f.getPriority()); }
        if (f.getAssigneeId() != null){ w.sql.append(" AND i.assignee_id = ?"); w.params.add(f.getAssigneeId()); }
        if (f.getReporterId() != null){ w.sql.append(" AND i.reporter_id = ?"); w.params.add(f.getReporterId()); }
        if (f.isOpenOnly())           { w.sql.append(" AND i.status <> 'DONE'"); }
        if (f.getQ() != null) {
            w.sql.append(" AND (i.issue_key LIKE ? OR i.title LIKE ? OR i.description LIKE ?)");
            String like = "%" + f.getQ() + "%";
            w.params.add(like); w.params.add(like); w.params.add(like);
        }
        return w;
    }

    private void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int n = 0; n < params.size(); n++) ps.setObject(n + 1, params.get(n));
    }

    public int count(Connection con, User user, IssueFilter f) throws SQLException {
        Where w = buildWhere(user, f);
        try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM issues i" + w.sql)) {
            bind(ps, w.params);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        }
    }

    public List<Issue> search(Connection con, User user, IssueFilter f) throws SQLException {
        Where w = buildWhere(user, f);
        // ORDER BY cannot use "?" placeholders, so the column comes from this fixed whitelist (never from user text)
        String column;
        switch (f.getSort()) {
            case "updated":  column = "i.updated_at"; break;
            case "due":      column = "(i.due_date IS NULL), i.due_date"; break;
            case "priority": column = "FIELD(i.priority, 'LOWEST','LOW','MEDIUM','HIGH','CRITICAL')"; break;
            default:         column = "i.created_at";
        }
        String dir = "asc".equalsIgnoreCase(f.getDir()) ? "ASC" : "DESC";
        String sql = SELECT + w.sql + " ORDER BY " + column + " " + dir + ", i.id DESC LIMIT ? OFFSET ?";
        List<Issue> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, w.params);
            ps.setInt(w.params.size() + 1, f.getSize());
            ps.setInt(w.params.size() + 2, (f.getPage() - 1) * f.getSize());
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }
}
