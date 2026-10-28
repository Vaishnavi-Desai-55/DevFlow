package com.devflow.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thrown by services when something the USER can fix goes wrong (bad input, no permission,
 * not found...). Servlets turn it into a friendly message. Technical errors become Kind.SYSTEM.
 */
public class ServiceException extends RuntimeException {
    public enum Kind { VALIDATION, NOT_FOUND, FORBIDDEN, CONFLICT, INVALID_TRANSITION, SYSTEM }

    private final Kind kind;

    public ServiceException(Kind kind, String message) { super(message); this.kind = kind; }
    public Kind getKind() { return kind; }

    public static ServiceException validation(String m) { return new ServiceException(Kind.VALIDATION, m); }
    public static ServiceException notFound(String m) { return new ServiceException(Kind.NOT_FOUND, m); }
    public static ServiceException forbidden(String m) { return new ServiceException(Kind.FORBIDDEN, m); }
    public static ServiceException conflict(String m) { return new ServiceException(Kind.CONFLICT, m); }

    /** Logs the real cause and returns a safe message (no stack trace for the user). */
    public static ServiceException system(Exception cause) {
        Logger.getLogger(ServiceException.class.getName()).log(Level.SEVERE, "Unexpected system error", cause);
        return new ServiceException(Kind.SYSTEM, "Something went wrong on our side. Please try again.");
    }
}
