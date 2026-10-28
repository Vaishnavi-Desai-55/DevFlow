package com.devflow.dao;

import com.devflow.model.Comment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentDAO {

    private Comment map(ResultSet rs) throws SQLException {
        Comment c = new Comment();
        c.setId(rs.getInt("id"));
        c.setIssueId(rs.getInt("issue_id"));
        c.setUserId(rs.getInt("user_id"));
        c.setAuthorName(rs.getString("full_name"));
        c.setText(rs.getString("comment_text"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        c.setUpdatedAt(rs.getTimestamp("updated_at"));
        return c;
    }

    private static final String SELECT = "SELECT c.*, u.full_name FROM comments c JOIN users u ON u.id = c.user_id ";

    public Comment findById(Connection con, int id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SELECT + "WHERE c.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        }
    }

    /** Oldest first (chronological order). */
    public List<Comment> listByIssue(Connection con, int issueId) throws SQLException {
        List<Comment> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(SELECT + "WHERE c.issue_id = ? ORDER BY c.created_at ASC, c.id ASC")) {
            ps.setInt(1, issueId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    public int insert(Connection con, int issueId, int userId, String text) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO comments (issue_id, user_id, comment_text) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, issueId);
            ps.setInt(2, userId);
            ps.setString(3, text);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { keys.next(); return keys.getInt(1); }
        }
    }

    public void update(Connection con, int id, String text) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE comments SET comment_text = ? WHERE id = ?")) {
            ps.setString(1, text);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void delete(Connection con, int id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM comments WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
