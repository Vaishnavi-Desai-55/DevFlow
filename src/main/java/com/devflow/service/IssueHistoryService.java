package com.devflow.service;

import com.devflow.dao.IssueHistoryDAO;
import com.devflow.model.IssueHistory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * The audit log. Other services call record() with THEIR connection, so the audit row is
 * committed (or rolled back) in the same transaction as the change it describes.
 * There is deliberately no servlet that lets a user write history entries.
 */
public class IssueHistoryService {
    private final IssueHistoryDAO dao = new IssueHistoryDAO();

    public void record(Connection con, int issueId, int userId, String actionType, String field, String oldValue, String newValue) throws SQLException {
        dao.insert(con, issueId, userId, actionType, field, oldValue, newValue);
    }

    public List<IssueHistory> list(Connection con, int issueId) throws SQLException {
        return dao.listByIssue(con, issueId);
    }
}
