package com.devflow.service;

import com.devflow.dao.CommentDAO;
import com.devflow.dao.IssueDAO;
import com.devflow.model.Comment;
import com.devflow.model.Issue;
import com.devflow.model.User;
import com.devflow.util.ServiceException;
import com.devflow.util.Tx;
import com.devflow.util.ValidationUtil;

/** Comments. Each add/edit/delete also writes an audit row in the same transaction. */
public class CommentService {
    private final CommentDAO comments = new CommentDAO();
    private final IssueDAO issues = new IssueDAO();
    private final ProjectService projectService = new ProjectService();
    private final IssueHistoryService history = new IssueHistoryService();

    private String clean(String text) {
        String t = ValidationUtil.trim(text);
        if (ValidationUtil.isBlank(t)) throw ServiceException.validation("Comment cannot be empty.");
        if (t.length() > 2000) throw ServiceException.validation("Comment is too long (max 2000 characters).");
        return t;
    }

    private static String shorten(String s) { return s.length() > 200 ? s.substring(0, 200) + "..." : s; }

    /** Returns the issue id so the servlet can redirect back to it. */
    public int add(User user, int issueId, String text) {
        String t = clean(text);
        return Tx.run(con -> {
            Issue issue = issues.findById(con, issueId);
            if (issue == null) throw ServiceException.notFound("Issue not found.");
            projectService.requireWrite(con, user, issue.getProjectId());
            comments.insert(con, issueId, user.getId(), t);
            history.record(con, issueId, user.getId(), "COMMENT_ADDED", "comment", null, shorten(t));
            return issueId;
        });
    }

    public int edit(User user, int commentId, String text) {
        String t = clean(text);
        return Tx.run(con -> {
            Comment c = comments.findById(con, commentId);
            if (c == null) throw ServiceException.notFound("Comment not found.");
            Issue issue = issues.findById(con, c.getIssueId());
            projectService.requireWrite(con, user, issue.getProjectId());
            if (c.getUserId() != user.getId()) throw ServiceException.forbidden("You can only edit your own comments.");
            comments.update(con, commentId, t);
            history.record(con, c.getIssueId(), user.getId(), "COMMENT_EDITED", "comment", shorten(c.getText()), shorten(t));
            return c.getIssueId();
        });
    }

    /** Authors can delete their own comments; project admins can delete any. */
    public int delete(User user, int commentId) {
        return Tx.run(con -> {
            Comment c = comments.findById(con, commentId);
            if (c == null) throw ServiceException.notFound("Comment not found.");
            Issue issue = issues.findById(con, c.getIssueId());
            String role = projectService.requireWrite(con, user, issue.getProjectId());
            if (c.getUserId() != user.getId() && !"ADMIN".equals(role))
                throw ServiceException.forbidden("You can only delete your own comments.");
            comments.delete(con, commentId);
            history.record(con, c.getIssueId(), user.getId(), "COMMENT_DELETED", "comment", shorten(c.getText()), null);
            return c.getIssueId();
        });
    }
}
