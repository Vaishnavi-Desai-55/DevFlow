package com.devflow.controller;

import com.devflow.service.AuthenticationService;
import com.devflow.util.ServiceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends BaseServlet {
    private final AuthenticationService auth = new AuthenticationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (currentUser(req) != null) { redirect(req, resp, "/dashboard"); return; }
        render(req, resp, "register");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            auth.register(req.getParameter("fullName"), req.getParameter("email"),
                          req.getParameter("password"), req.getParameter("confirm"), req.getParameter("role"));
            flash(req, "success", "Account created. Please sign in.");
            redirect(req, resp, "/login");
        } catch (ServiceException e) {
            flashNow(req, "error", e.getMessage());     // form is shown again with what the user typed (${param.x})
            resp.setStatus(httpStatus(e));
            render(req, resp, "register");
        }
    }
}
