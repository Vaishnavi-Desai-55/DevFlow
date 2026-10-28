package com.devflow.controller;

import com.devflow.model.IssueFilter;
import com.devflow.model.User;
import com.devflow.service.IssueService;
import com.devflow.service.StatsService;
import com.devflow.util.ServiceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** /dashboard shows the page; /dashboard/data returns the same numbers as JSON so charts can refresh. */
@WebServlet({"/dashboard", "/dashboard/data"})
public class DashboardServlet extends BaseServlet {
    private final StatsService stats = new StatsService();
    private final IssueService issues = new IssueService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        try {
            if (req.getServletPath().equals("/dashboard/data")) {
                json(resp, 200, stats.compute(user, null));
                return;
            }
            IssueFilter f = new IssueFilter();
            f.setAssigneeId(user.getId());
            f.setOpenOnly(true);
            f.setSort("due");
            f.setDir("asc");
            f.setSize(6);
            req.setAttribute("stats", stats.compute(user, null));
            req.setAttribute("myIssues", issues.search(user, f).getItems());
            render(req, resp, "dashboard");
        } catch (ServiceException e) {
            showError(req, resp, e);
        }
    }
}
