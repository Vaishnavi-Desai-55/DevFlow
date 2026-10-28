package com.devflow.util;

import java.sql.Date;
import java.util.regex.Pattern;

public final class ValidationUtil {
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PROJECT_KEY = Pattern.compile("^[A-Z][A-Z0-9]{1,9}$");

    private ValidationUtil() { }

    public static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    public static String trim(String s) { return s == null ? null : s.trim(); }
    public static boolean isValidEmail(String s) { return s != null && s.length() <= 150 && EMAIL.matcher(s).matches(); }
    public static boolean isValidProjectKey(String s) { return s != null && PROJECT_KEY.matcher(s).matches(); }

    /** At least 8 characters with a letter and a digit. */
    public static boolean isStrongPassword(String s) {
        return s != null && s.length() >= 8 && s.length() <= 72
                && s.chars().anyMatch(Character::isLetter) && s.chars().anyMatch(Character::isDigit);
    }

    public static Integer parseInt(String s) {
        try { return isBlank(s) ? null : Integer.valueOf(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    /** Parses yyyy-MM-dd; blank gives null; garbage throws a validation error. */
    public static Date parseDate(String s) {
        if (isBlank(s)) return null;
        try { return Date.valueOf(s.trim()); }
        catch (IllegalArgumentException e) { throw ServiceException.validation("Invalid date. Use the date picker."); }
    }
}
