package com.example.unisync.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

/**
 * RequestPerson model — represents an individual person selected
 * for a team support request.
 *
 * Stored as subcollection: teamRequests/{requestId}/people
 */
public class RequestPerson {

    // Assignment status constants
    public static final String STATUS_ASSIGNED = "ASSIGNED";
    public static final String STATUS_ACCEPTED = "ACCEPTED";
    public static final String STATUS_DECLINED = "DECLINED";
    public static final String STATUS_COMPLETED = "COMPLETED";

    @DocumentId
    private String personId;
    private String fullName;
    private String course;
    private String year;
    private String department;
    private String rollNumber;
    private String assignedDuty;
    private String team;
    private String assignmentStatus;
    private String notes;

    @ServerTimestamp
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Required empty constructor for Firestore
    public RequestPerson() {
        this.assignmentStatus = STATUS_ASSIGNED;
    }

    // ── Getters ──

    public String getPersonId() { return personId; }
    public String getFullName() { return fullName; }
    public String getCourse() { return course; }
    public String getYear() { return year; }
    public String getDepartment() { return department; }
    public String getRollNumber() { return rollNumber; }
    public String getAssignedDuty() { return assignedDuty; }
    public String getTeam() { return team; }
    public String getAssignmentStatus() { return assignmentStatus; }
    public String getNotes() { return notes; }
    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }

    // ── Setters ──

    public void setPersonId(String personId) { this.personId = personId; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setCourse(String course) { this.course = course; }
    public void setYear(String year) { this.year = year; }
    public void setDepartment(String department) { this.department = department; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public void setAssignedDuty(String assignedDuty) { this.assignedDuty = assignedDuty; }
    public void setTeam(String team) { this.team = team; }
    public void setAssignmentStatus(String assignmentStatus) { this.assignmentStatus = assignmentStatus; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
