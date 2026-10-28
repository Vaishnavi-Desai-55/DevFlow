package com.devflow.controller;

import com.devflow.model.Issue;
import com.devflow.model.Status;
import com.devflow.service.WorkflowService;
import com.devflow.util.ServiceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * POST /workflow/transition (issueId, toStatus).
 * The Kanban board calls this with fetch() and gets JSON back. The issue page uses a normal form.
 * Either way WorkflowService is the one that decides if the move is allowed.
 */
@WebServlet("/workflow/transition")
public class WorkflowServlet extends BaseServlet {
    private final WorkflowService workflow = new WorkflowService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer issueId = intParam(req, "issueId");
        String to = req.getParameter("toStatus");
        try {
            if (issueId == null) throw ServiceException.notFound("Issue not found.");
            Issue issue = workflow.transition(currentUser(req), issueId, to);
            if (isAjax(req)) {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("ok", true);
                body.put("status", issue.getStatus());
                body.put("label", Status.from(issue.getStatus()).getLabel());
                body.put("message", issue.getIssueKey() + " moved to " + Status.from(issue.getStatus()).getLabel() + ".");
                json(resp, 200, body);
            } else {
                flash(req, "success", "Status changed to " + Status.from(issue.getStatus()).getLabel() + ".");
                redirect(req, resp, "/issues/view?id=" + issueId);
            }
        } catch (ServiceException e) {
            if (isAjax(req)) {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("ok", false);
                body.put("message", e.getMessage());
                json(resp, httpStatus(e), body);
            } else {
                fail(req, resp, e, "/issues/view?id=" + issueId);
            }
        }
    }
}
