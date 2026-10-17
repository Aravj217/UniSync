package com.example.unisync.utils;

/**
 * Constants — centralized constants for the UniSync application.
 */
public final class Constants {

    private Constants() {} // Prevent instantiation

    // ── Intent Extra Keys ──
    public static final String EXTRA_EVENT_ID = "eventId";
    public static final String EXTRA_REQUEST_ID = "requestId";
    public static final String EXTRA_VOLUNTEER_ID = "volunteerId";
    public static final String EXTRA_USER_ID = "userId";
    public static final String EXTRA_MODE = "mode";
    public static final String EXTRA_IS_EDIT = "isEdit";

    // ── Mode Values ──
    public static final String MODE_CREATE = "create";
    public static final String MODE_EDIT = "edit";
    public static final String MODE_VIEW = "view";

    // ── Firebase Storage Paths ──
    public static final String STORAGE_EVENT_POSTERS = "event_posters";

    // ── Validation ──
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PEOPLE_COUNT = 50;
    public static final long MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB

    // ── Date/Time Formats ──
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_DISPLAY_FORMAT = "dd MMM yyyy";
    public static final String TIME_FORMAT = "HH:mm";
    public static final String TIME_DISPLAY_FORMAT = "hh:mm a";
    public static final String DATETIME_FORMAT = "dd MMM yyyy, hh:mm a";

    // ── Academic Year Options ──
    public static final String[] YEAR_OPTIONS = {
        "1st Year", "2nd Year", "3rd Year", "4th Year", "5th Year"
    };

    // ── Department Options ──
    public static final String[] DEPARTMENT_OPTIONS = {
        "Computer Engineering",
        "Information Technology",
        "Electronics & Telecom",
        "Mechanical Engineering",
        "Civil Engineering",
        "Data Science",
        "AI & ML",
        "Biotechnology",
        "Chemical Engineering",
        "MBA",
        "BBA",
        "B.Sc",
        "B.Com",
        "BA",
        "Other"
    };
}
