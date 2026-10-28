package com.devflow.service;

import com.devflow.dao.ProjectDAO;
import com.devflow.dao.UserDAO;
import com.devflow.dao.WorkflowDAO;
import com.devflow.model.*;
import com.devflow.util.ServiceException;
import com.devflow.util.Tx;
import com.devflow.util.ValidationUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Project rules and, just as important, the permission checks every other service reuses. */
public class ProjectService {
    private final ProjectDAO projects = new ProjectDAO();
    private final UserDAO users = new UserDAO();
    private final WorkflowDAO workflows = new WorkflowDAO();

    // ---------- permission helpers (used by IssueService, WorkflowService, CommentService...) ----------

    /** The user's role in this project, or null if they have no access. Global admins act as project admins. */
    public String roleOf(Connection con, User u, int projectId) throws SQLException {
        String role = projects.getMemberRole(con, projectId, u.getId());
        if (role != null) return role;
        return u.isGlobalAdmin() ? "ADMIN" : null;
    }

    /** Throws "not found" (not "forbidden") so outsiders cannot discover which project ids exist. */
    public String requireAccess(Connection con, User u, int projectId) throws SQLException {
        String role = projects.findById(con, projectId) == null ? null : roleOf(con, u, projectId);
        if (role == null) throw ServiceException.notFound("Project not found.");
        return role;
    }

    public String requireWrite(Connection con, User u, int projectId) throws SQLException {
        String role = requireAccess(con, u, projectId);
        if (!Role.canWrite(role)) throw ServiceException.forbidden("You have read-only (Viewer) access in this project.");
        return role;
    }

    public String requireAdmin(Connection con, User u, int projectId) throws SQLException {
        String role = requireAccess(con, u, projectId);
        if (!"ADMIN".equals(role)) throw ServiceException.forbidden("Only project admins can do that.");
        return role;
    }

    // ---------- projects ----------

    public List<Project> list(User u) {
        return Tx.read(con -> {
            List<Project> list = projects.listForUser(con, u);
            for (Project p : list) p.setMyRole(roleOf(con, u, p.getId()));
            return list;
        });
    }

    public Project get(User u, int id) {
        return Tx.read(con -> {
            String role = requireAccess(con, u, id);
            Project p = projects.findById(con, id);
            p.setMyRole(role);
            return p;
        });
    }

    public int create(User u, String name, String key, String description) {
        if (!u.isGlobalAdmin()) throw ServiceException.forbidden("Only admins can create projects.");
        name = ValidationUtil.trim(name);
        key = key == null ? null : key.trim().toUpperCase();
        description = ValidationUtil.trim(description);
        if (ValidationUtil.isBlank(name) || name.length() < 2 || name.length() > 120)
            throw ServiceException.validation("Project name must be 2 to 120 characters.");
        if (!ValidationUtil.isValidProjectKey(key))
            throw ServiceException.validation("Project key must be 2 to 10 characters: capital letters and digits, starting with a letter (e.g. ECOM).");
        if (description != null && description.length() > 2000)
            throw ServiceException.validation("Description is too long (max 2000 characters).");

        final String n = name, k = key, d = description;
        return Tx.run(con -> {
            if (projects.keyExists(con, k)) throw ServiceException.conflict("Project key " + k + " is already in use.");
            int id = projects.insert(con, n, k, d, u.getId());
            projects.addMember(con, id, u.getId(), "ADMIN");
            workflows.createWorkflow(con, id, "Default workflow", WorkflowService.defaultTransitions());
            return id;
        });
    }

    public void update(User u, int id, String name, String description, String status) {
        name = ValidationUtil.trim(name);
        description = ValidationUtil.trim(description);
        if (ValidationUtil.isBlank(name) || name.length() < 2 || name.length() > 120)
            throw ServiceException.validation("Project name must be 2 to 120 characters.");
        if (description != null && description.length() > 2000)
            throw ServiceException.validation("Description is too long (max 2000 characters).");
        if (!"ACTIVE".equals(status) && !"ARCHIVED".equals(status))
            throw ServiceException.validation("Invalid project status.");
        final String n = name, d = description;
        Tx.run(con -> {
            requireAdmin(con, u, id);
            projects.update(con, id, n, d, status);
            return null;
        });
    }

    // ---------- members ----------

    public List<Member> members(User u, int projectId) {
        return Tx.read(con -> {
            requireAccess(con, u, projectId);
            return projects.listMembers(con, projectId);
        });
    }

    public void addMember(User actor, int projectId, String email, String role) {
        String e = email == null ? "" : email.trim().toLowerCase();
        if (Role.from(role) == null) throw ServiceException.validation("Please choose a valid role.");
        Tx.run(con -> {
            requireAdmin(con, actor, projectId);
            User target = users.findByEmail(con, e);
            if (target == null) throw ServiceException.validation("No registered user has the email " + e + ". Ask them to register first.");
            if (projects.getMemberRole(con, projectId, target.getId()) != null)
                throw ServiceException.conflict(target.getFullName() + " is already a member of this project.");
            projects.addMember(con, projectId, target.getId(), role);
            return null;
        });
    }

    public void removeMember(User actor, int projectId, int userId) {
        Tx.run(con -> {
            requireAdmin(con, actor, projectId);
            String role = projects.getMemberRole(con, projectId, userId);
            if (role == null) throw ServiceException.notFound("That user is not a member of this project.");
            if ("ADMIN".equals(role) && projects.countAdmins(con, projectId) <= 1)
                throw ServiceException.conflict("A project needs at least one admin. Promote someone else first.");
            projects.removeMember(con, projectId, userId);
            return null;
        });
    }

    public void changeRole(User actor, int projectId, int userId, String newRole) {
        if (Role.from(newRole) == null) throw ServiceException.validation("Please choose a valid role.");
        Tx.run(con -> {
            requireAdmin(con, actor, projectId);
            String role = projects.getMemberRole(con, projectId, userId);
            if (role == null) throw ServiceException.notFound("That user is not a member of this project.");
            if ("ADMIN".equals(role) && !"ADMIN".equals(newRole) && projects.countAdmins(con, projectId) <= 1)
                throw ServiceException.conflict("A project needs at least one admin.");
            projects.updateMemberRole(con, projectId, userId, newRole);
            return null;
        });
    }
}
