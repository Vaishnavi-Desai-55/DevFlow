package com.devflow.controller;

import com.devflow.model.*;
import com.devflow.service.IssueService;
import com.devflow.service.ProjectService;
import com.devflow.service.StatsService;
import com.devflow.util.ServiceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Everything under /projects: list, create, edit, details, members and the Kanban board. */
@WebServlet("/projects/*")
public class ProjectServlet extends BaseServlet {
    private final ProjectService projects = new ProjectService();
    private final IssueService issues = new IssueService();
    private final StatsService stats = new StatsService();

    private String path(HttpServletRequest req) { return req.getPathInfo() == null ? "" : req.getPathInfo(); }

    /** Puts the project and the user's permissions where the JSPs can read them. */
    private Project loadProject(HttpServletRequest req, int id) {
        Project p = projects.get(currentUser(req), id);
        req.setAttribute("project", p);
        req.setAttribute("canWrite", Role.canWrite(p.getMyRole()));
        req.setAttribute("isProjectAdmin", "ADMIN".equals(p.getMyRole()));
        return p;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        try {
            switch (path(req)) {
                case "": case "/":
                    req.setAttribute("projects", projects.list(user));
                    render(req, resp, "projects");
                    break;
                case "/new":
                    if (!user.isGlobalAdmin()) throw ServiceException.forbidden("Only admins can create projects.");
                    render(req, resp, "project-form");
                    break;
                case "/edit": {
                    Project p = loadProject(req, requireId(req, "id"));
                    if (!"ADMIN".equals(p.getMyRole())) throw ServiceException.forbidden("Only project admins can edit a project.");
                    render(req, resp, "project-form");
                    break;
                }
                case "/view": {
                    int id = requireId(req, "id");
                    loadProject(req, id);
                    IssueFilter f = new IssueFilter();
                    f.setProjectId(id);
                    f.setSort("updated");
                    f.setSize(8);
                    req.setAttribute("recentIssues", issues.search(user, f).getItems());
                    req.setAttribute("stats", stats.compute(user, id));
                    req.setAttribute("members", projects.members(user, id));
                    render(req, resp, "project-details");
                    break;
                }
                case "/members": {
                    int id = requireId(req, "id");
                    loadProject(req, id);
                    req.setAttribute("members", projects.members(user, id));
                    render(req, resp, "members");
                    break;
                }
                case "/board": {
                    int id = requireId(req, "id");
                    loadProject(req, id);
                    Map<Status, List<Issue>> columns = new LinkedHashMap<>();
                    for (Status s : Status.values()) columns.put(s, new ArrayList<>());
                    for (Issue i : issues.board(user, id)) columns.get(Status.from(i.getStatus())).add(i);
                    req.setAttribute("columns", columns);
                    req.setAttribute("members", projects.members(user, id));
                    render(req, resp, "board");
                    break;
                }
                default:
                    throw ServiceException.notFound("The page you asked for does not exist.");
            }
        } catch (ServiceException e) {
            showError(req, resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        String p = path(req);
        try {
            switch (p) {
                case "/create": {
                    int id;
                    try {
                        id = projects.create(user, req.getParameter("name"), req.getParameter("key"), req.getParameter("description"));
                    } catch (ServiceException e) {
                        if (e.getKind() == ServiceException.Kind.VALIDATION || e.getKind() == ServiceException.Kind.CONFLICT) {
                            flashNow(req, "error", e.getMessage());
                            resp.setStatus(400);
                            render(req, resp, "project-form");    // show the form again, keeping what was typed
                            return;
                        }
                        throw e;
                    }
                    flash(req, "success", "Project created.");
                    redirect(req, resp, "/projects/view?id=" + id);
                    break;
                }
                case "/update": {
                    int id = requireId(req, "id");
                    try {
                        projects.update(user, id, req.getParameter("name"), req.getParameter("description"), req.getParameter("status"));
                    } catch (ServiceException e) { fail(req, resp, e, "/projects/edit?id=" + id); return; }
                    flash(req, "success", "Project updated.");
                    redirect(req, resp, "/projects/view?id=" + id);
                    break;
                }
                case "/members/add": {
                    int id = requireId(req, "projectId");
                    try {
                        projects.addMember(user, id, req.getParameter("email"), req.getParameter("role"));
                        flash(req, "success", "Member added.");
                    } catch (ServiceException e) { fail(req, resp, e, "/projects/members?id=" + id); return; }
                    redirect(req, resp, "/projects/members?id=" + id);
                    break;
                }
                case "/members/remove": {
                    int id = requireId(req, "projectId");
                    try {
                        projects.removeMember(user, id, requireId(req, "userId"));
                        flash(req, "success", "Member removed.");
                    } catch (ServiceException e) { fail(req, resp, e, "/projects/members?id=" + id); return; }
                    redirect(req, resp, "/projects/members?id=" + id);
                    break;
                }
                case "/members/role": {
                    int id = requireId(req, "projectId");
                    try {
                        projects.changeRole(user, id, requireId(req, "userId"), req.getParameter("role"));
                        flash(req, "success", "Role updated.");
                    } catch (ServiceException e) { fail(req, resp, e, "/projects/members?id=" + id); return; }
                    redirect(req, resp, "/projects/members?id=" + id);
                    break;
                }
                default:
                    throw ServiceException.notFound("The page you asked for does not exist.");
            }
        } catch (ServiceException e) {
            showError(req, resp, e);
        }
    }
}
