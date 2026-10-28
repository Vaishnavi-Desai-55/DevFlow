package com.devflow.controller;

import com.devflow.model.User;
import com.devflow.service.AuthenticationService;
import com.devflow.util.ServiceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends BaseServlet {
    private final AuthenticationService auth = new AuthenticationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (currentUser(req) != null) { redirect(req, resp, "/dashboard"); return; }
        render(req, resp, "login");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            User user = auth.login(req.getParameter("email"), req.getParameter("password"));
            req.changeSessionId();                       // new session id after login (prevents session fixation)
            HttpSession session = req.getSession();
            session.setAttribute("user", user);
            session.setMaxInactiveInterval(30 * 60);     // session timeout: 30 minutes of inactivity
            redirect(req, resp, "/dashboard");
        } catch (ServiceException e) {
            flashNow(req, "error", e.getMessage());
            resp.setStatus(e.getKind() == ServiceException.Kind.SYSTEM ? 500 : 401);
            render(req, resp, "login");
        }
    }
}
