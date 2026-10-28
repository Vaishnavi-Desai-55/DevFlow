package com.devflow.controller;

import com.devflow.model.*;
import com.devflow.util.ServiceException;
import com.devflow.util.ValidationUtil;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Shared helpers for all controllers (servlets). Controllers read the request, call a service, then pick a view. */
public abstract class BaseServlet extends HttpServlet {
    protected static final Gson GSON = new Gson();

    /** name to label maps so JSPs can print "In Progress" for IN_PROGRESS: ${statusLabels[issue.status]} */
    private static final Map<String, String> STATUS_LABELS = new LinkedHashMap<>();
    private static final Map<String, String> PRIORITY_LABELS = new LinkedHashMap<>();
    private static final Map<String, String> TYPE_LABELS = new LinkedHashMap<>();
    private static final Map<String, String> ROLE_LABELS = new LinkedHashMap<>();
    static {
        for (Status s : Status.values()) STATUS_LABELS.put(s.name(), s.getLabel());
        for (Priority p : Priority.values()) PRIORITY_LABELS.put(p.name(), p.getLabel());
        for (IssueType t : IssueType.values()) TYPE_LABELS.put(t.name(), t.getLabel());
        for (Role r : Role.values()) ROLE_LABELS.put(r.name(), r.getLabel());
    }

    protected User currentUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s == null ? null : (User) s.getAttribute("user");
    }

    /** Forwards to /WEB-INF/views/<view>.jsp (JSPs there cannot be opened directly by URL). */
    protected void render(HttpServletRequest req, HttpServletResponse resp, String view) throws ServletException, IOException {
        req.setAttribute("ctx", req.getContextPath());
        req.setAttribute("statuses", Status.values());
        req.setAttribute("priorities", Priority.values());
        req.setAttribute("issueTypes", IssueType.values());
        req.setAttribute("roles", Role.values());
        req.setAttribute("statusLabels", STATUS_LABELS);
        req.setAttribute("priorityLabels", PRIORITY_LABELS);
        req.setAttribute("typeLabels", TYPE_LABELS);
        req.setAttribute("roleLabels", ROLE_LABELS);

        HttpSession s = req.getSession(false);
        if (s != null && s.getAttribute("flash") != null) {      // one-time message saved before a redirect
            if (req.getAttribute("flash") == null) {
                req.setAttribute("flash", s.getAttribute("flash"));
                req.setAttribute("flashType", s.getAttribute("flashType"));
            }
            s.removeAttribute("flash");
            s.removeAttribute("flashType");
        }
        req.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp").forward(req, resp);
    }

    /** Message shown on the NEXT page (survives a redirect). */
    protected void flash(HttpServletRequest req, String type, String message) {
        HttpSession s = req.getSession();
        s.setAttribute("flash", message);
        s.setAttribute("flashType", type);
    }

    /** Message shown on the page we are rendering right now (used when re-showing a form with an error). */
    protected void flashNow(HttpServletRequest req, String type, String message) {
        req.setAttribute("flash", message);
        req.setAttribute("flashType", type);
    }

    protected void redirect(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
        resp.sendRedirect(req.getContextPath() + path);
    }

    protected String param(HttpServletRequest req, String name) { return ValidationUtil.trim(req.getParameter(name)); }
    protected Integer intParam(HttpServletRequest req, String name) { return ValidationUtil.parseInt(req.getParameter(name)); }

    protected int requireId(HttpServletRequest req, String name) {
        Integer id = intParam(req, name);
        if (id == null) throw ServiceException.notFound("The page you asked for does not exist.");
        return id;
    }

    protected boolean isAjax(HttpServletRequest req) { return "fetch".equals(req.getHeader("X-Requested-With")); }

    protected void json(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(GSON.toJson(body));
    }

    protected int httpStatus(ServiceException e) {
        switch (e.getKind()) {
            case NOT_FOUND: return 404;
            case FORBIDDEN: return 403;
            case CONFLICT:
            case INVALID_TRANSITION: return 409;
            case SYSTEM: return 500;
            default: return 400;
        }
    }

    /** Full error page (404, 403, 500). */
    protected void showError(HttpServletRequest req, HttpServletResponse resp, ServiceException e) throws ServletException, IOException {
        resp.setStatus(httpStatus(e));
        req.setAttribute("errorMessage", e.getMessage());
        req.setAttribute("errorStatus", httpStatus(e));
        render(req, resp, "error");
    }

    /**
     * Standard handling for a failed action: serious problems get the error page,
     * ordinary validation problems flash a message and go back to a page the user can fix it on.
     */
    protected void fail(HttpServletRequest req, HttpServletResponse resp, ServiceException e, String backPath) throws ServletException, IOException {
        switch (e.getKind()) {
            case NOT_FOUND: case FORBIDDEN: case SYSTEM: showError(req, resp, e); break;
            default: flash(req, "error", e.getMessage()); redirect(req, resp, backPath);
        }
    }
}
