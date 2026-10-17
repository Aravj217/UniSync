package com.example.unisync.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

/**
 * Volunteer model — represents an assigned volunteer record.
 * Stored in the "volunteers" or bridged with "volunteerAssignments" collection.
 */
public class Volunteer {

    public static final String COLLECTION_NAME = "volunteers";

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_NEEDS_CORRECTION = "NEEDS_CORRECTION";
    public static final String STATUS_CANCELLED = "CANCELLED";

    @DocumentId
    private String volunteerId;
    private String eventId;
    private String eventName;
    private String userId; // Authenticated UID
    private String studentName;
    private String userEmail;
    private String userType;
    private String course;
    private String year;
    private String department;
    private String rollNumber;
    private String assignedDuty;
    private String team;
    private String eventDate;
    private String startTime;
    private String endTime;
    private String venue;
    private String organizerId;
    private String additionalInstructions;
    private String status;
    private String rejectionReason;
    private String reviewedBy;
    private Timestamp reviewedAt;
    private String createdBy;

    @ServerTimestamp
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Required empty constructor for Firestore
    public Volunteer() {
        this.status = STATUS_APPROVED; // default for legacy compatibility
    }

    // ── Getters ──

    public String getVolunteerId() { return volunteerId; }
    public String getEventId() { return eventId; }
    public String getEventName() { return eventName; }
    public String getUserId() { return userId; }
    public String getStudentName() { return studentName; }
    public String getUserEmail() { return userEmail; }
    public String getUserType() { return userType; }
    public String getCourse() { return course; }
    public String getYear() { return year; }
    public String getDepartment() { return department; }
    public String getRollNumber() { return rollNumber; }
    public String getAssignedDuty() { return assignedDuty; }
    public String getTeam() { return team; }
    public String getEventDate() { return eventDate; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getVenue() { return venue; }
    public String getOrganizerId() { return organizerId; }
    public String getAdditionalInstructions() { return additionalInstructions; }
    public String getStatus() { return status != null ? status : STATUS_APPROVED; }
    public String getRejectionReason() { return rejectionReason; }
    public String getReviewedBy() { return reviewedBy; }
    public Timestamp getReviewedAt() { return reviewedAt; }
    public String getCreatedBy() { return createdBy; }
    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }

    // ── Setters ──

    public void setVolunteerId(String volunteerId) { this.volunteerId = volunteerId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public void setUserType(String userType) { this.userType = userType; }
    public void setCourse(String course) { this.course = course; }
    public void setYear(String year) { this.year = year; }
    public void setDepartment(String department) { this.department = department; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public void setAssignedDuty(String assignedDuty) { this.assignedDuty = assignedDuty; }
    public void setTeam(String team) { this.team = team; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setVenue(String venue) { this.venue = venue; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
    public void setAdditionalInstructions(String additionalInstructions) { this.additionalInstructions = additionalInstructions; }
    public void setStatus(String status) { this.status = status; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    public void setReviewedAt(Timestamp reviewedAt) { this.reviewedAt = reviewedAt; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public boolean isApproved() { return STATUS_APPROVED.equals(status); }
}
