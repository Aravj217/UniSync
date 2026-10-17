package com.example.unisync.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

/**
 * User model — represents a user in the UniSync system.
 * Stored in the "users" Firestore collection.
 *
 * Roles: ADMIN, ORGANIZER, USER (also supports legacy STUDENT)
 * User Types: STUDENT, TEACHER
 */
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
    private boolean active;

    @ServerTimestamp
    private Timestamp createdAt;

    // Required empty constructor for Firestore deserialization
    public User() {
        this.active = true;
        this.userType = USER_TYPE_STUDENT;
    }

    public User(String uid, String fullName, String email, String role) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.userType = USER_TYPE_STUDENT;
        this.active = true;
    }

    public User(String uid, String fullName, String email, String role, String userType) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.userType = userType;
        this.active = true;
    }

    // ── Getters ──

    public String getUid() { return uid; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getUserType() { return userType != null ? userType : USER_TYPE_STUDENT; }
    public String getCourse() { return course; }
    public String getYear() { return year; }
    public String getDepartment() { return department; }
    public String getRollNumber() { return rollNumber; }
    public boolean isActive() { return active; }
    public Timestamp getCreatedAt() { return createdAt; }

    // ── Setters ──

    public void setUid(String uid) { this.uid = uid; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setUserType(String userType) { this.userType = userType; }
    public void setCourse(String course) { this.course = course; }
    public void setYear(String year) { this.year = year; }
    public void setDepartment(String department) { this.department = department; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public void setActive(boolean active) { this.active = active; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    // ── Helpers ──

    public boolean isAdmin() { return ROLE_ADMIN.equals(role); }
    public boolean isOrganizer() { return ROLE_ORGANIZER.equals(role); }
    public boolean isStudent() {
        return ROLE_STUDENT.equals(role) || (ROLE_USER.equals(role) && USER_TYPE_STUDENT.equalsIgnoreCase(userType));
    }
    public boolean isTeacher() {
        return ROLE_USER.equals(role) && USER_TYPE_TEACHER.equalsIgnoreCase(userType);
    }
    public boolean isUser() {
        return ROLE_USER.equals(role) || ROLE_STUDENT.equals(role);
    }
}
