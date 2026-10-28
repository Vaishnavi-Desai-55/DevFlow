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

@WebServlet("/profile")
public class ProfileServlet extends BaseServlet {
    private final ProjectService projects = new ProjectService();
    private final StatsService stats = new StatsService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        try {
            req.setAttribute("projects", projects.list(user));
            req.setAttribute("stats", stats.compute(user, null));
            render(req, resp, "profile");
        } catch (ServiceException e) {
            showError(req, resp, e);
        }
    }
}
