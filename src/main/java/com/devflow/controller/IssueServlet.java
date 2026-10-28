package com.devflow.controller;

import com.devflow.model.*;
import com.devflow.service.IssueService;
import com.devflow.service.ProjectService;
import com.devflow.util.ServiceException;
import com.devflow.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Everything under /issues: search list, create, details, edit, delete, priority, blocking links. */
@WebServlet("/issues/*")
public class IssueServlet extends BaseServlet {
    private static final Set<String> SORTS = Set.of("created", "updated", "due", "priority");

    private final IssueService issues = new IssueService();
    private final ProjectService projects = new ProjectService();

    private String path(HttpServletRequest req) { return req.getPathInfo() == null ? "" : req.getPathInfo(); }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        try {
            switch (path(req)) {
                case "": case "/": showList(req, resp, user); break;
                case "/new": showForm(req, resp, user); break;
                case "/view": {
                    IssueDetail d = issues.detail(user, requireId(req, "id"));
                    req.setAttribute("d", d);
                    render(req, resp, "issue-details");
                    break;
                }
                default: throw ServiceException.notFound("The page you asked for does not exist.");
            }
        } catch (ServiceException e) {
            showError(req, resp, e);
        }
    }

    // ---------- list with search, filters, sorting and paging ----------

    private void showList(HttpServletRequest req, HttpServletResponse resp, User user) throws ServletException, IOException {
        IssueFilter f = new IssueFilter();
        f.setQ(param(req, "q"));
        f.setProjectId(intParam(req, "project"));
        f.setType(IssueType.from(req.getParameter("type")) == null ? null : req.getParameter("type"));
        f.setStatus(Status.from(req.getParameter("status")) == null ? null : req.getParameter("status"));
        f.setPriority(Priority.from(req.getParameter("priority")) == null ? null : req.getParameter("priority"));
        boolean mine = "1".equals(req.getParameter("mine"));
        f.setAssigneeId(mine ? Integer.valueOf(user.getId()) : intParam(req, "assignee"));
        String sort = req.getParameter("sort");
        f.setSort(sort != null && SORTS.contains(sort) ? sort : "created");
        f.setDir("asc".equals(req.getParameter("dir")) ? "asc" : "desc");
        Integer page = intParam(req, "page");
        f.setPage(page == null ? 1 : page);

        req.setAttribute("filter", f);
        req.setAttribute("mine", mine);
        req.setAttribute("result", issues.search(user, f));
        req.setAttribute("projects", projects.list(user));
        if (f.getProjectId() != null) req.setAttribute("members", projects.members(user, f.getProjectId()));
        req.setAttribute("qs", queryString(f, mine));
        render(req, resp, "issues");
    }

    /** Filter parameters as a URL query string (without page) so pagination links keep the filters. */
    private String queryString(IssueFilter f, boolean mine) {
        List<String> parts = new ArrayList<>();
        add(parts, "q", f.getQ());
        add(parts, "project", f.getProjectId());
        add(parts, "type", f.getType());
        add(parts, "status", f.getStatus());
        add(parts, "priority", f.getPriority());
        if (mine) add(parts, "mine", "1"); else add(parts, "assignee", f.getAssigneeId());
        add(parts, "sort", f.getSort());
        add(parts, "dir", f.getDir());
        return String.join("&", parts);
    }

    private void add(List<String> parts, String name, Object value) {
        if (value != null && !value.toString().isEmpty())
            parts.add(name + "=" + URLEncoder.encode(value.toString(), StandardCharsets.UTF_8));
    }

    // ---------- create form ----------

    private void showForm(HttpServletRequest req, HttpServletResponse resp, User user) throws ServletException, IOException {
        List<Project> writable = new ArrayList<>();
        for (Project p : projects.list(user)) if (Role.canWrite(p.getMyRole()) && !p.isArchived()) writable.add(p);
        req.setAttribute("projects", writable);

        Integer projectId = intParam(req, "projectId");
        if (projectId != null) {
            Project p = projects.get(user, projectId);
            if (!Role.canWrite(p.getMyRole())) throw ServiceException.forbidden("Viewers cannot create issues.");
            req.setAttribute("project", p);
            req.setAttribute("assignees", issues.assignees(user, projectId));
            req.setAttribute("parents", issues.parentCandidates(user, projectId));
        }
        render(req, resp, "issue-form");
    }

    // ---------- actions ----------

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        try {
            switch (path(req)) {
                case "/create": create(req, resp, user); break;
                case "/update": {
                    int id = requireId(req, "id");
                    try {
                        issues.update(user, id, req.getParameter("title"), req.getParameter("description"), req.getParameter("priority"),
                                intParam(req, "assigneeId"), ValidationUtil.parseDate(req.getParameter("dueDate")));
                        flash(req, "success", "Issue updated.");
                    } catch (ServiceException e) { fail(req, resp, e, "/issues/view?id=" + id); return; }
                    redirect(req, resp, "/issues/view?id=" + id);
                    break;
                }
                case "/delete": {
                    int id = requireId(req, "id");
                    int projectId;
                    try { projectId = issues.delete(user, id); }
                    catch (ServiceException e) { fail(req, resp, e, "/issues/view?id=" + id); return; }
                    flash(req, "success", "Issue deleted.");
                    redirect(req, resp, "/projects/view?id=" + projectId);
                    break;
                }
                case "/priority": {
                    int id = requireId(req, "id");
                    try {
                        issues.changePriority(user, id, req.getParameter("priority"));
                        flash(req, "success", "Priority updated to the suggested value.");
                    } catch (ServiceException e) { fail(req, resp, e, "/issues/view?id=" + id); return; }
                    redirect(req, resp, "/issues/view?id=" + id);
                    break;
                }
                case "/link": {
                    int id = requireId(req, "id");
                    try {
                        issues.addBlocksLink(user, id, req.getParameter("blockedKey"));
                        flash(req, "success", "Link added.");
                    } catch (ServiceException e) { fail(req, resp, e, "/issues/view?id=" + id); return; }
                    redirect(req, resp, "/issues/view?id=" + id);
                    break;
                }
                default: throw ServiceException.notFound("The page you asked for does not exist.");
            }
        } catch (ServiceException e) {
            showError(req, resp, e);
        }
    }

    private void create(HttpServletRequest req, HttpServletResponse resp, User user) throws ServletException, IOException {
        Issue draft = new Issue();
        try {
            draft.setProjectId(requireId(req, "projectId"));
            draft.setTitle(req.getParameter("title"));
            draft.setDescription(req.getParameter("description"));
            draft.setIssueType(req.getParameter("issueType"));
            draft.setPriority(req.getParameter("priority"));
            draft.setAssigneeId(intParam(req, "assigneeId"));
            draft.setParentIssueId(intParam(req, "parentId"));
            draft.setDueDate(ValidationUtil.parseDate(req.getParameter("dueDate")));
            Issue created = issues.create(user, draft);
            flash(req, "success", "Created " + created.getIssueKey() + ".");
            redirect(req, resp, "/issues/view?id=" + created.getId());
        } catch (ServiceException e) {
            if (e.getKind() == ServiceException.Kind.VALIDATION || e.getKind() == ServiceException.Kind.CONFLICT) {
                flashNow(req, "error", e.getMessage());
                resp.setStatus(400);
                showForm(req, resp, user);           // same form again with the user's input kept
            } else {
                showError(req, resp, e);
            }
        }
    }
}
