package com.devflow.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

/**
 * Small helper that opens a connection, runs your code, and handles commit / rollback.
 * Everything inside run() succeeds together or fails together (a database transaction).
 */
public final class Tx {
    @FunctionalInterface
    public interface Work<T> { T run(Connection con) throws SQLException; }

    private Tx() { }

    /** Read-only work (no transaction needed). */
    public static <T> T read(Work<T> work) {
        try (Connection con = DBConnection.getConnection()) {
            return work.run(con);
        } catch (SQLException e) {
            throw ServiceException.system(e);
        }
    }

    /** Work that changes data: commit if it finishes, roll back if anything throws. */
    public static <T> T run(Work<T> work) {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                T result = work.run(con);
                con.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw ServiceException.conflict("That value already exists or is still in use.");
        } catch (SQLException e) {
            throw ServiceException.system(e);
        }
    }
}
