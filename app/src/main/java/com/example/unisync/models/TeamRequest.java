package com.example.unisync.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

/**
 * TeamRequest model — represents a team support request in UniSync.
 * Stored in the "teamRequests" Firestore collection.
 *
 * This allows organizers to request people for event preparation,
 * logistics, technical support, and other college activities.
 */
public class TeamRequest {

    // Status constants
    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_NEEDS_CORRECTION = "NEEDS_CORRECTION";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_PUBLISHED = "PUBLISHED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_COMPLETED = "COMPLETED";

    // Request mode constants
    public static final String MODE_ROLE_BASED = "ROLE_BASED";
    public static final String MODE_PRESELECTED = "PRESELECTED_PEOPLE";
    public static final String MODE_MIXED = "MIXED";

    // Visibility constants
    public static final String VIS_PRIVATE = "PRIVATE_ASSIGNMENT";
    public static final String VIS_STUDENT_VOLUNTEER = "STUDENT_VOLUNTEER_REQUEST";
    public static final String VIS_ORGANIZER_ADMIN = "ORGANIZER_AND_ADMIN";

    // Category constants
    public static final String CAT_EVENT_PREP = "Event Preparation";
    public static final String CAT_DECORATION = "Decoration";
    public static final String CAT_STAGE_SETUP = "Stage Setup";
    public static final String CAT_TECH_SUPPORT = "Technical Support";
    public static final String CAT_REGISTRATION = "Registration Desk";
    public static final String CAT_CROWD = "Crowd Coordination";
    public static final String CAT_LOGISTICS = "Logistics";
    public static final String CAT_PHOTOGRAPHY = "Photography & Media";
    public static final String CAT_HOSPITALITY = "Hospitality";
    public static final String CAT_EQUIPMENT = "Equipment Setup";
    public static final String CAT_CLEANING = "Cleaning & Arrangement";
    public static final String CAT_OTHER = "Other College Work";

    public static final String COLLECTION_NAME = "teamRequests";
    public static final String SUBCOLLECTION_PEOPLE = "people";
    public static final String SUBCOLLECTION_ROLES = "roles";

    @DocumentId
    private String requestId;
    private String title;
    private String description;
    private String reason;
    private String category;
    private String relatedEventId;
    private String requestMode;
    private String workLocation;
    private String requiredDate;        // Format: "yyyy-MM-dd"
    private String startTime;           // Format: "HH:mm"
    private String endTime;             // Format: "HH:mm"
    private int peopleRequired;
    private String requiredDepartment;
    private String requiredSkills;
    private String organizerId;
    private String facultyInCharge;
    private String status;
    private String visibility;
    private String rejectionReason;
    private String reviewedBy;
    private Timestamp reviewedAt;
    private String additionalInstructions;

    @ServerTimestamp
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Required empty constructor for Firestore
    public TeamRequest() {
        this.status = STATUS_DRAFT;
        this.visibility = VIS_ORGANIZER_ADMIN;
    }

    // ── Getters ──

    public String getRequestId() { return requestId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getReason() { return reason; }
    public String getCategory() { return category; }
    public String getRelatedEventId() { return relatedEventId; }
    public String getRequestMode() { return requestMode; }
    public String getWorkLocation() { return workLocation; }
    public String getRequiredDate() { return requiredDate; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public int getPeopleRequired() { return peopleRequired; }
    public String getRequiredDepartment() { return requiredDepartment; }
    public String getRequiredSkills() { return requiredSkills; }
    public String getOrganizerId() { return organizerId; }
    public String getFacultyInCharge() { return facultyInCharge; }
    public String getStatus() { return status; }
    public String getVisibility() { return visibility; }
    public String getRejectionReason() { return rejectionReason; }
    public String getReviewedBy() { return reviewedBy; }
    public Timestamp getReviewedAt() { return reviewedAt; }
    public String getAdditionalInstructions() { return additionalInstructions; }
    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }

    // ── Setters ──

    public void setRequestId(String requestId) { this.requestId = requestId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setReason(String reason) { this.reason = reason; }
    public void setCategory(String category) { this.category = category; }
    public void setRelatedEventId(String relatedEventId) { this.relatedEventId = relatedEventId; }
    public void setRequestMode(String requestMode) { this.requestMode = requestMode; }
    public void setWorkLocation(String workLocation) { this.workLocation = workLocation; }
    public void setRequiredDate(String requiredDate) { this.requiredDate = requiredDate; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setPeopleRequired(int peopleRequired) { this.peopleRequired = peopleRequired; }
    public void setRequiredDepartment(String requiredDepartment) { this.requiredDepartment = requiredDepartment; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
    public void setFacultyInCharge(String facultyInCharge) { this.facultyInCharge = facultyInCharge; }
    public void setStatus(String status) { this.status = status; }
    public void setVisibility(String visibility) { this.visibility = visibility; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    public void setReviewedAt(Timestamp reviewedAt) { this.reviewedAt = reviewedAt; }
    public void setAdditionalInstructions(String additionalInstructions) { this.additionalInstructions = additionalInstructions; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    // ── Helpers ──

    public boolean canEdit() {
        return STATUS_DRAFT.equals(status) || STATUS_REJECTED.equals(status)
                || STATUS_NEEDS_CORRECTION.equals(status);
    }

    public boolean canSubmit() {
        return STATUS_DRAFT.equals(status) || STATUS_REJECTED.equals(status)
                || STATUS_NEEDS_CORRECTION.equals(status);
    }

    public boolean isVisibleToStudents() {
        return VIS_STUDENT_VOLUNTEER.equals(visibility)
                && (STATUS_APPROVED.equals(status) || STATUS_PUBLISHED.equals(status));
    }

    /**
     * Returns an array of all support request category options.
     */
    public static String[] getAllCategories() {
        return new String[]{
            CAT_EVENT_PREP, CAT_DECORATION, CAT_STAGE_SETUP, CAT_TECH_SUPPORT,
            CAT_REGISTRATION, CAT_CROWD, CAT_LOGISTICS, CAT_PHOTOGRAPHY,
            CAT_HOSPITALITY, CAT_EQUIPMENT, CAT_CLEANING, CAT_OTHER
        };
    }

    /**
     * Returns an array of request mode options.
     */
    public static String[] getRequestModes() {
        return new String[]{
            "Request Volunteers by Role",
            "Add Already-Selected People",
            "Both (Mixed)"
        };
    }

    /**
     * Converts display mode name to constant value.
     */
    public static String modeDisplayToValue(String displayName) {
        switch (displayName) {
            case "Request Volunteers by Role": return MODE_ROLE_BASED;
            case "Add Already-Selected People": return MODE_PRESELECTED;
            case "Both (Mixed)": return MODE_MIXED;
            default: return MODE_ROLE_BASED;
        }
    }

    /**
     * Converts constant value to display mode name.
     */
    public static String modeValueToDisplay(String value) {
        switch (value) {
            case MODE_ROLE_BASED: return "Request Volunteers by Role";
            case MODE_PRESELECTED: return "Add Already-Selected People";
            case MODE_MIXED: return "Both (Mixed)";
            default: return "Request Volunteers by Role";
        }
    }
}
