package com.example.unisync.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

/**
 * User model — represents a user in the UniSync system.
 * Stored in the "users" Firestore collection.
 *
 * Roles: ADMIN, ORGANIZER, USER (also supports legacy STUDENT)
 * User Types: STUDENT, TEACHER
 */
@IgnoreExtraProperties
public class User {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_ORGANIZER = "ORGANIZER";
    public static final String ROLE_USER = "USER";
    public static final String ROLE_STUDENT = "STUDENT"; // for backward compatibility

    public static final String USER_TYPE_STUDENT = "STUDENT";
    public static final String USER_TYPE_TEACHER = "TEACHER";
    public static final String TYPE_STUDENT = USER_TYPE_STUDENT;
    public static final String TYPE_TEACHER = USER_TYPE_TEACHER;

    public static final String COLLECTION_NAME = "users";

    @DocumentId
    private String uid;
    private String fullName;
    private String email;
    private String role;
    private String userType;
    private String course;
    private String year;
    private String department;
    private String rollNumber;
    private boolean active = true;

    @ServerTimestamp
    private Timestamp createdAt;

    // Required empty constructor for Firestore deserialization
    public User() {
        this.active = true;
        this.role = ROLE_USER;
        this.userType = USER_TYPE_STUDENT;
    }

    public User(String uid, String fullName, String email, String role) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.role = role != null && !role.trim().isEmpty() ? role.trim() : ROLE_USER;
        this.userType = USER_TYPE_STUDENT;
        this.active = true;
    }

    public User(String uid, String fullName, String email, String role, String userType) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.role = role != null && !role.trim().isEmpty() ? role.trim() : ROLE_USER;
        this.userType = userType != null && !userType.trim().isEmpty() ? userType.trim() : USER_TYPE_STUDENT;
        this.active = true;
    }

    // ── Getters ──

    public String getUid() { return uid != null ? uid : ""; }
    public String getFullName() { return fullName != null ? fullName : ""; }
    public String getEmail() { return email != null ? email : ""; }
    public String getRole() { return role != null && !role.trim().isEmpty() ? role.trim() : ROLE_USER; }
    public String getUserType() { return userType != null && !userType.trim().isEmpty() ? userType.trim() : USER_TYPE_STUDENT; }
    public String getCourse() { return course != null ? course : ""; }
    public String getYear() { return year != null ? year : ""; }
    public String getDepartment() { return department != null ? department : ""; }
    public String getRollNumber() { return rollNumber != null ? rollNumber : ""; }
    public boolean isActive() { return active; }
    public boolean getActive() { return active; }
    public Timestamp getCreatedAt() { return createdAt; }

    // ── Setters ──

    public void setUid(String uid) { this.uid = uid; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = (role != null && !role.trim().isEmpty()) ? role.trim() : ROLE_USER; }
    public void setUserType(String userType) { this.userType = (userType != null && !userType.trim().isEmpty()) ? userType.trim() : USER_TYPE_STUDENT; }
    public void setCourse(String course) { this.course = course; }
    public void setYear(String year) { this.year = year; }
    public void setDepartment(String department) { this.department = department; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public void setActive(boolean active) { this.active = active; }
    public void setActive(Boolean active) { this.active = active != null ? active : true; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    // ── Helpers ──

    @Exclude
    public boolean isAdmin() { return ROLE_ADMIN.equalsIgnoreCase(getRole()); }
    @Exclude
    public boolean isOrganizer() { return ROLE_ORGANIZER.equalsIgnoreCase(getRole()); }
    @Exclude
    public boolean isStudent() {
        return ROLE_STUDENT.equalsIgnoreCase(getRole()) || (ROLE_USER.equalsIgnoreCase(getRole()) && USER_TYPE_STUDENT.equalsIgnoreCase(getUserType()));
    }
    @Exclude
    public boolean isTeacher() {
        return ROLE_USER.equalsIgnoreCase(getRole()) && USER_TYPE_TEACHER.equalsIgnoreCase(getUserType());
    }
    @Exclude
    public boolean isUser() {
        return ROLE_USER.equalsIgnoreCase(getRole()) || ROLE_STUDENT.equalsIgnoreCase(getRole());
    }

    /**
     * Safely constructs a User object from a DocumentSnapshot,
     * ensuring no runtime exceptions occur if any field is missing or of unexpected type.
     */
    public static User fromDocumentSnapshot(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        User user = null;
        try {
            user = doc.toObject(User.class);
        } catch (Exception ignored) {}

        if (user == null) {
            user = new User();
        }

        if (user.getUid().isEmpty()) {
            user.setUid(doc.getId());
        }
        if (doc.contains("fullName") && doc.getString("fullName") != null) {
            user.setFullName(doc.getString("fullName"));
        }
        if (doc.contains("email") && doc.getString("email") != null) {
            user.setEmail(doc.getString("email"));
        }
        if (doc.contains("role") && doc.getString("role") != null) {
            user.setRole(doc.getString("role"));
        }
        if (doc.contains("userType") && doc.getString("userType") != null) {
            user.setUserType(doc.getString("userType"));
        }
        if (doc.contains("course") && doc.getString("course") != null) {
            user.setCourse(doc.getString("course"));
        }
        if (doc.contains("year") && doc.getString("year") != null) {
            user.setYear(doc.getString("year"));
        }
        if (doc.contains("department") && doc.getString("department") != null) {
            user.setDepartment(doc.getString("department"));
        }
        if (doc.contains("rollNumber") && doc.getString("rollNumber") != null) {
            user.setRollNumber(doc.getString("rollNumber"));
        }
        if (doc.contains("active")) {
            Boolean act = doc.getBoolean("active");
            user.setActive(act != null ? act : true);
        }
        if (doc.contains("createdAt")) {
            user.setCreatedAt(doc.getTimestamp("createdAt"));
        }
        return user;
    }
}
