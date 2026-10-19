package com.example.unisync.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.unisync.models.User;

/**
 * SessionManager manages the user's login session
 * and stores user information using SharedPreferences.
 */
public class SessionManager {

    private static final String PREF_NAME = "UniSyncSession";

    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_UID = "uid";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";
    private static final String KEY_USER_TYPE = "userType";
    private static final String KEY_COURSE = "course";
    private static final String KEY_YEAR = "year";
    private static final String KEY_DEPARTMENT = "department";
    private static final String KEY_ROLL_NUMBER = "rollNumber";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );

        editor = prefs.edit();
    }

    /**
     * Save user session after successful login.
     */
    public void createSession(User user) {
        if (user == null) return;

        editor.putBoolean(KEY_IS_LOGGED_IN, true);

        editor.putString(KEY_UID, user.getUid() != null ? user.getUid() : "");
        editor.putString(KEY_FULL_NAME, user.getFullName() != null ? user.getFullName() : "");
        editor.putString(KEY_EMAIL, user.getEmail() != null ? user.getEmail() : "");
        editor.putString(KEY_ROLE, user.getRole() != null && !user.getRole().trim().isEmpty() ? user.getRole().trim() : User.ROLE_USER);
        editor.putString(KEY_USER_TYPE, user.getUserType() != null && !user.getUserType().trim().isEmpty() ? user.getUserType().trim() : User.USER_TYPE_STUDENT);

        editor.putString(KEY_COURSE, user.getCourse() != null ? user.getCourse() : "");
        editor.putString(KEY_YEAR, user.getYear() != null ? user.getYear() : "");
        editor.putString(KEY_DEPARTMENT, user.getDepartment() != null ? user.getDepartment() : "");
        editor.putString(KEY_ROLL_NUMBER, user.getRollNumber() != null ? user.getRollNumber() : "");

        editor.commit(); // Ensure synchronous write to disk
    }

    /**
     * Check whether the user is logged in.
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Return the cached user object.
     */
    public User getUser() {
        if (!isLoggedIn()) {
            return null;
        }

        User user = new User();

        user.setUid(getUid());
        user.setFullName(getFullName());
        user.setEmail(getEmail());
        user.setRole(getRole());
        user.setUserType(getUserType());

        user.setCourse(getCourse());
        user.setYear(getYear());
        user.setDepartment(getDepartment());
        user.setRollNumber(getRollNumber());

        return user;
    }

    /**
     * Get the user's Firebase UID.
     */
    public String getUid() {
        return prefs.getString(KEY_UID, "");
    }

    /**
     * Get the user's full name.
     */
    public String getFullName() {
        return prefs.getString(KEY_FULL_NAME, "");
    }

    /**
     * Get the user's email address.
     */
    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    /**
     * Get the user's role.
     */
    public String getRole() {
        String role = prefs.getString(KEY_ROLE, User.ROLE_USER);
        return (role != null && !role.trim().isEmpty()) ? role.trim() : User.ROLE_USER;
    }

    /**
     * Get the user's user type (STUDENT or TEACHER).
     */
    public String getUserType() {
        return prefs.getString(KEY_USER_TYPE, User.USER_TYPE_STUDENT);
    }

    /**
     * Get the user's course.
     */
    public String getCourse() {
        return prefs.getString(KEY_COURSE, "");
    }

    /**
     * Get the user's academic year.
     */
    public String getYear() {
        return prefs.getString(KEY_YEAR, "");
    }

    /**
     * Get the user's department.
     */
    public String getDepartment() {
        return prefs.getString(KEY_DEPARTMENT, "");
    }

    /**
     * Get the user's roll number.
     */
    public String getRollNumber() {
        return prefs.getString(KEY_ROLL_NUMBER, "");
    }

    /**
     * Check whether the current user is an admin.
     */
    public boolean isAdmin() {
        return User.ROLE_ADMIN.equals(getRole());
    }

    /**
     * Check whether the current user is an organizer.
     */
    public boolean isOrganizer() {
        return User.ROLE_ORGANIZER.equals(getRole());
    }

    /**
     * Check whether the current user is a student.
     */
    public boolean isStudent() {
        return User.ROLE_STUDENT.equals(getRole()) ||
                (User.ROLE_USER.equals(getRole()) && User.USER_TYPE_STUDENT.equalsIgnoreCase(getUserType()));
    }

    /**
     * Check whether the current user is a teacher.
     */
    public boolean isTeacher() {
        return User.ROLE_USER.equals(getRole()) && User.USER_TYPE_TEACHER.equalsIgnoreCase(getUserType());
    }

    /**
     * Check whether the current user is a student or teacher.
     */
    public boolean isUser() {
        return User.ROLE_USER.equals(getRole()) || User.ROLE_STUDENT.equals(getRole());
    }

    /**
     * Clear the session when the user logs out.
     */
    public void logout() {
        editor.clear();
        editor.commit();
    }
}