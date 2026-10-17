package com.example.unisync.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

/**
 * VolunteerAssignment model — represents a person assigned to an event duty in UniSync.
 * Stored in the "volunteerAssignments" Firestore collection.
 *
 * Workflow:
 * Organizer assigns user -> PENDING -> Admin reviews -> APPROVED / REJECTED / NEEDS_CORRECTION / CANCELLED.
 */
public class VolunteerAssignment {

    public static final String COLLECTION_NAME = "volunteerAssignments";

    // Status Constants
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_CANCELLED = "CANCELLED";
    // Legacy constants for backwards compatibility
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_NEEDS_CORRECTION = "NEEDS_CORRECTION";

    @DocumentId
    private String assignmentId;
    private String eventId;
    private String eventName;
    private String userId; // Authoritative Firebase UID of the assigned user
    private String userName;
    private String userEmail;
    private String userType; // STUDENT or TEACHER
    private String course;
    private String year;
    private String department;
    private String rollNumber;
    private String assignedDuty;
    private String eventDate;   // Format: "yyyy-MM-dd"
    private String startTime;   // Format: "HH:mm"
    private String endTime;     // Format: "HH:mm"
    private String venue;
    private String organizerId;
    private String organizerName;
    private String additionalInstructions;
    private String status;      // ACTIVE, CANCELLED, or legacy (APPROVED, PENDING)
    private String rejectionReason;
    private String reviewedBy;  // Admin UID (if modified by Admin)
    private Timestamp reviewedAt;

    @ServerTimestamp
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Required empty constructor for Firestore
    public VolunteerAssignment() {
        this.status = STATUS_ACTIVE;
    }

    public boolean isActive() {
        return STATUS_ACTIVE.equalsIgnoreCase(status) || STATUS_APPROVED.equalsIgnoreCase(status);
    }

    // ── Getters ──

    public String getAssignmentId() { return assignmentId; }
    public String getEventId() { return eventId; }
    public String getEventName() { return eventName; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserEmail() { return userEmail; }
    public String getUserType() { return userType; }
    public String getCourse() { return course; }
    public String getYear() { return year; }
    public String getDepartment() { return department; }
    public String getRollNumber() { return rollNumber; }
    public String getAssignedDuty() { return assignedDuty; }
    public String getEventDate() { return eventDate; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getVenue() { return venue; }
    public String getOrganizerId() { return organizerId; }
    public String getOrganizerName() { return organizerName; }
    public String getAdditionalInstructions() { return additionalInstructions; }
    public String getStatus() { return status; }
    public String getRejectionReason() { return rejectionReason; }
    public String getReviewedBy() { return reviewedBy; }
    public Timestamp getReviewedAt() { return reviewedAt; }
    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }

    // ── Setters ──

    public void setAssignmentId(String assignmentId) { this.assignmentId = assignmentId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public void setUserType(String userType) { this.userType = userType; }
    public void setCourse(String course) { this.course = course; }
    public void setYear(String year) { this.year = year; }
    public void setDepartment(String department) { this.department = department; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public void setAssignedDuty(String assignedDuty) { this.assignedDuty = assignedDuty; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setVenue(String venue) { this.venue = venue; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
    public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }
    public void setAdditionalInstructions(String additionalInstructions) { this.additionalInstructions = additionalInstructions; }
    public void setStatus(String status) { this.status = status; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    public void setReviewedAt(Timestamp reviewedAt) { this.reviewedAt = reviewedAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    // ── Helpers ──

    public boolean isPending() { return STATUS_PENDING.equals(status); }
    public boolean isApproved() { return STATUS_APPROVED.equals(status); }
    public boolean isRejected() { return STATUS_REJECTED.equals(status); }
    public boolean isNeedsCorrection() { return STATUS_NEEDS_CORRECTION.equals(status); }
    public boolean isCancelled() { return STATUS_CANCELLED.equals(status); }

    public boolean canEdit() {
        return !STATUS_CANCELLED.equals(status);
    }
}
