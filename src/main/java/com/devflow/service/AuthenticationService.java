package com.devflow.service;

import com.devflow.dao.UserDAO;
import com.devflow.model.Role;
import com.devflow.model.User;
import com.devflow.util.PasswordUtil;
import com.devflow.util.ServiceException;
import com.devflow.util.Tx;
import com.devflow.util.ValidationUtil;

public class AuthenticationService {
    private final UserDAO users = new UserDAO();
    /** Used so a login for an unknown email takes about as long as one for a known email. */
    private static final String DUMMY_HASH = PasswordUtil.hash("not-a-real-password-1");

    public User register(String fullName, String email, String password, String confirm, String requestedRole) {
        fullName = ValidationUtil.trim(fullName);
        email = ValidationUtil.trim(email) == null ? null : email.trim().toLowerCase();

        if (ValidationUtil.isBlank(fullName) || fullName.length() < 2 || fullName.length() > 100)
            throw ServiceException.validation("Please enter your full name (2 to 100 characters).");
        if (!ValidationUtil.isValidEmail(email))
            throw ServiceException.validation("Please enter a valid email address.");
        if (!ValidationUtil.isStrongPassword(password))
            throw ServiceException.validation("Password must be at least 8 characters and contain a letter and a digit.");
        if (!password.equals(confirm))
            throw ServiceException.validation("Passwords do not match.");

        // Self-registration can only create Developer or Viewer accounts. The very first user becomes Admin.
        String role = "VIEWER".equals(requestedRole) ? "VIEWER" : "DEVELOPER";
        final String finalEmail = email, finalName = fullName, finalRole = role;

        return Tx.run(con -> {
            if (users.findByEmail(con, finalEmail) != null)
                throw ServiceException.conflict("An account with this email already exists.");
            User u = new User();
            u.setFullName(finalName);
            u.setEmail(finalEmail);
            u.setPasswordHash(PasswordUtil.hash(password));
            u.setRole(users.count(con) == 0 ? Role.ADMIN.name() : finalRole);
            u.setId(users.insert(con, u));
            u.setPasswordHash(null);
            return u;
        });
    }

    public User login(String email, String password) {
        String e = email == null ? "" : email.trim().toLowerCase();
        User u = Tx.read(con -> users.findByEmail(con, e));
        boolean ok = PasswordUtil.verify(password, u == null ? DUMMY_HASH : u.getPasswordHash());
        if (u == null || !ok) throw ServiceException.validation("Invalid email or password.");
        u.setPasswordHash(null);   // never keep the hash in the session
        return u;
    }
}
