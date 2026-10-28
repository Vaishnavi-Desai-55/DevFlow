package com.devflow.controller;

import com.devflow.model.User;
import com.devflow.service.CommentService;
import com.devflow.util.ServiceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** POST /comments/add | /comments/edit | /comments/delete. Always returns to the issue page. */
@WebServlet("/comments/*")
public class CommentServlet extends BaseServlet {
    private final CommentService comments = new CommentService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        String action = req.getPathInfo() == null ? "" : req.getPathInfo();
        Integer issueId = intParam(req, "issueId");
        String back = "/issues/view?id=" + issueId;
        try {
            int id;
            switch (action) {
                case "/add":    id = comments.add(user, requireId(req, "issueId"), req.getParameter("text")); flash(req, "success", "Comment added."); break;
                case "/edit":   id = comments.edit(user, requireId(req, "commentId"), req.getParameter("text")); flash(req, "success", "Comment updated."); break;
                case "/delete": id = comments.delete(user, requireId(req, "commentId")); flash(req, "success", "Comment deleted."); break;
                default: throw ServiceException.notFound("The page you asked for does not exist.");
            }
            redirect(req, resp, "/issues/view?id=" + id + "#comments");
        } catch (ServiceException e) {
            fail(req, resp, e, back + "#comments");
        }
    }
}
