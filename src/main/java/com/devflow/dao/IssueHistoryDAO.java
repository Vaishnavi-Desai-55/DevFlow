package com.devflow.dao;

import com.devflow.model.IssueHistory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IssueHistoryDAO {

    public void insert(Connection con, int issueId, int userId, String actionType, String field, String oldValue, String newValue) throws SQLException {
        String sql = "INSERT INTO issue_history (issue_id, user_id, action_type, field_name, old_value, new_value) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, issueId);
            ps.setInt(2, userId);
            ps.setString(3, actionType);
            ps.setString(4, field);
            ps.setString(5, oldValue);
            ps.setString(6, newValue);
            ps.executeUpdate();
        }
    }

    public List<IssueHistory> listByIssue(Connection con, int issueId) throws SQLException {
        String sql = "SELECT h.*, u.full_name FROM issue_history h JOIN users u ON u.id = h.user_id "
                   + "WHERE h.issue_id = ? ORDER BY h.created_at DESC, h.id DESC";
        List<IssueHistory> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, issueId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    IssueHistory h = new IssueHistory();
                    h.setId(rs.getInt("id"));
                    h.setIssueId(rs.getInt("issue_id"));
                    h.setUserId(rs.getInt("user_id"));
                    h.setUserName(rs.getString("full_name"));
                    h.setActionType(rs.getString("action_type"));
                    h.setFieldName(rs.getString("field_name"));
                    h.setOldValue(rs.getString("old_value"));
                    h.setNewValue(rs.getString("new_value"));
                    h.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(h);
                }
            }
        }
        return list;
    }
}
