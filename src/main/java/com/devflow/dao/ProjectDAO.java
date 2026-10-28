package com.devflow.dao;

import com.devflow.model.Member;
import com.devflow.model.Project;
import com.devflow.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectDAO {

    private static final String SELECT =
        "SELECT p.*, u.full_name AS creator_name, "
      + "(SELECT COUNT(*) FROM issues i WHERE i.project_id = p.id) AS issue_count, "
      + "(SELECT COUNT(*) FROM issues i WHERE i.project_id = p.id AND i.status = 'DONE') AS done_count, "
      + "(SELECT COUNT(*) FROM project_members m WHERE m.project_id = p.id) AS member_count "
      + "FROM projects p JOIN users u ON u.id = p.created_by ";

    private Project map(ResultSet rs) throws SQLException {
        Project p = new Project();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setKey(rs.getString("project_key"));
        p.setDescription(rs.getString("description"));
        p.setStatus(rs.getString("status"));
        p.setNextIssueNumber(rs.getInt("next_issue_number"));
        p.setCreatedBy(rs.getInt("created_by"));
        p.setCreatedByName(rs.getString("creator_name"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setUpdatedAt(rs.getTimestamp("updated_at"));
        p.setIssueCount(rs.getInt("issue_count"));
        p.setDoneCount(rs.getInt("done_count"));
        p.setMemberCount(rs.getInt("member_count"));
        return p;
    }

    public Project findById(Connection con, int id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SELECT + "WHERE p.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        }
    }

    /** Global admins see every project; everyone else only the projects they are a member of. */
    public List<Project> listForUser(Connection con, User user) throws SQLException {
        String sql = user.isGlobalAdmin()
            ? SELECT + "ORDER BY p.created_at DESC"
            : SELECT + "WHERE p.id IN (SELECT pm.project_id FROM project_members pm WHERE pm.user_id = ?) ORDER BY p.created_at DESC";
        List<Project> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (!user.isGlobalAdmin()) ps.setInt(1, user.getId());
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    public boolean keyExists(Connection con, String key) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM projects WHERE project_key = ?")) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public int insert(Connection con, String name, String key, String description, int createdBy) throws SQLException {
        String sql = "INSERT INTO projects (name, project_key, description, created_by) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, key);
            ps.setString(3, description);
            ps.setInt(4, createdBy);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { keys.next(); return keys.getInt(1); }
        }
    }

    public void update(Connection con, int id, String name, String description, String status) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE projects SET name = ?, description = ?, status = ? WHERE id = ?")) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setString(3, status);
            ps.setInt(4, id);
            ps.executeUpdate();
        }
    }

    /**
     * Locks the project row until the transaction ends and returns {nextIssueNumber}.
     * Two people creating issues at the same moment must wait for each other here,
     * which is what guarantees unique issue numbers (ECOM-101, ECOM-102 ...).
     */
    public int lockNextIssueNumber(Connection con, int projectId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT next_issue_number FROM projects WHERE id = ? FOR UPDATE")) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : -1; }
        }
    }

    public void bumpIssueNumber(Connection con, int projectId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE projects SET next_issue_number = next_issue_number + 1 WHERE id = ?")) {
            ps.setInt(1, projectId);
            ps.executeUpdate();
        }
    }

    // ---- members ----

    public String getMemberRole(Connection con, int projectId, int userId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT project_role FROM project_members WHERE project_id = ? AND user_id = ?")) {
            ps.setInt(1, projectId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getString(1) : null; }
        }
    }

    public List<Member> listMembers(Connection con, int projectId) throws SQLException {
        String sql = "SELECT u.id, u.full_name, u.email, m.project_role, m.joined_at FROM project_members m "
                   + "JOIN users u ON u.id = m.user_id WHERE m.project_id = ? "
                   + "ORDER BY FIELD(m.project_role, 'ADMIN', 'DEVELOPER', 'VIEWER'), u.full_name";
        List<Member> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Member m = new Member();
                    m.setUserId(rs.getInt(1));
                    m.setFullName(rs.getString(2));
                    m.setEmail(rs.getString(3));
                    m.setProjectRole(rs.getString(4));
                    m.setJoinedAt(rs.getTimestamp(5));
                    list.add(m);
                }
            }
        }
        return list;
    }

    public void addMember(Connection con, int projectId, int userId, String role) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO project_members (project_id, user_id, project_role) VALUES (?, ?, ?)")) {
            ps.setInt(1, projectId);
            ps.setInt(2, userId);
            ps.setString(3, role);
            ps.executeUpdate();
        }
    }

    public void removeMember(Connection con, int projectId, int userId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM project_members WHERE project_id = ? AND user_id = ?")) {
            ps.setInt(1, projectId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement("UPDATE issues SET assignee_id = NULL WHERE project_id = ? AND assignee_id = ?")) {
            ps.setInt(1, projectId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public void updateMemberRole(Connection con, int projectId, int userId, String role) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE project_members SET project_role = ? WHERE project_id = ? AND user_id = ?")) {
            ps.setString(1, role);
            ps.setInt(2, projectId);
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

    public int countAdmins(Connection con, int projectId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM project_members WHERE project_id = ? AND project_role = 'ADMIN'")) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        }
    }
}
