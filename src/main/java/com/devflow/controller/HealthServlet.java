package com.devflow.controller;

import com.devflow.util.DBConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Public page to confirm Tomcat + Java + MySQL work together: /health */
@WebServlet("/health")
public class HealthServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(HealthServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain;charset=UTF-8");
        try (Connection con = DBConnection.getConnection()) {
            resp.getWriter().println("DevFlow is running. Database connection OK.");
        } catch (SQLException | RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Database health check failed", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().println("DevFlow is running, but the database connection failed. Check db.properties and MySQL.");
        }
    }
}
