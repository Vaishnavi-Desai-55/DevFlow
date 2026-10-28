package com.devflow.controller;

import com.devflow.model.User;
import com.devflow.service.ProjectService;
import com.devflow.service.StatsService;
import com.devflow.util.ServiceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** /analytics?project=ID shows the page, /analytics/data?project=ID returns chart numbers as JSON. */
@WebServlet({"/analytics", "/analytics/data"})
public class AnalyticsServlet extends BaseServlet {
    private final StatsService stats = new StatsService();
    private final ProjectService projects = new ProjectService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        try {
            int projectId = requireId(req, "project");
            if (req.getServletPath().equals("/analytics/data")) {
                json(resp, 200, stats.compute(user, projectId));
                return;
            }
            req.setAttribute("project", projects.get(user, projectId));
            req.setAttribute("stats", stats.compute(user, projectId));
            render(req, resp, "analytics");
        } catch (ServiceException e) {
            if (req.getServletPath().equals("/analytics/data")) json(resp, httpStatus(e), java.util.Map.of("ok", false, "message", e.getMessage()));
            else showError(req, resp, e);
        }
    }
}
