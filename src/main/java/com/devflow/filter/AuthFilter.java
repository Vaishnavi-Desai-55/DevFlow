package com.devflow.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.IOException;

/** Runs before every request: anyone who is not logged in is sent to the login page. */
public class AuthFilter implements Filter {

    private boolean isPublic(String path) {
        return path.equals("/login") || path.equals("/register") || path.equals("/health") || path.startsWith("/assets/");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String path = req.getServletPath();

        if (isPublic(path)) { chain.doFilter(req, resp); return; }

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            if ("fetch".equals(req.getHeader("X-Requested-With"))) {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please sign in again.");
            } else {
                resp.sendRedirect(req.getContextPath() + "/login");
            }
            return;
        }
        // Stop the browser caching private pages (so the Back button after logout shows nothing sensitive)
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);
        chain.doFilter(req, resp);
    }
}
