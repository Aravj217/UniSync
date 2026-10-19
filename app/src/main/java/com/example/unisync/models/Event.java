package com.example.unisync.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

/**
 * Event model — represents a college event in UniSync.
 * Stored in the "events" Firestore collection.
 *
 * Status lifecycle: DRAFT → PENDING → APPROVED/REJECTED → PUBLISHED → CANCELLED
 */
@IgnoreExtraProperties
public class Event {

    // Status constants
    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_PUBLISHED = "PUBLISHED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    // Category constants
    public static final String CAT_WORKSHOP = "Workshop";
    public static final String CAT_COMPETITION = "Competition";
    public static final String CAT_FESTIVAL = "Festival";
    public static final String CAT_SEMINAR = "Seminar";
    public static final String CAT_SPORTS = "Sports";
    public static final String CAT_CULTURAL = "Cultural";
    public static final String CAT_TECHNICAL = "Technical";
    public static final String CAT_SOCIAL = "Social Service";
    public static final String CAT_GUEST_LECTURE = "Guest Lecture";
    public static final String CAT_OTHER = "Other";

    public static final String COLLECTION_NAME = "events";

    @DocumentId
    private String eventId;
    private String eventName;
    private String category;
    private String shortDescription;
    private String description;
    private String posterUrl;
    private String eventDate;       // Format: "yyyy-MM-dd"
    private String startTime;       // Format: "HH:mm"
    private String endTime;         // Format: "HH:mm"
    private String venue;
    private String organizingTeam;
    private String facultyInCharge;
    private String organizerId;
    private String registrationInfo;
    private String contactInfo;
    private String status;
    private String rejectionReason;
    private String reviewedBy;
    private Timestamp reviewedAt;
    private Timestamp publishedAt;

    @ServerTimestamp
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Required empty constructor for Firestore
    public Event() {
        this.status = STATUS_DRAFT;
    }

    // ── Getters ──

    public String getEventId() { return eventId; }
    public String getEventName() { return eventName; }
    public String getCategory() { return category; }
    public String getShortDescription() { return shortDescription; }
    public String getDescription() { return description; }
    public String getPosterUrl() { return posterUrl; }
    public String getEventDate() { return eventDate; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getVenue() { return venue; }
    public String getOrganizingTeam() { return organizingTeam; }
    public String getFacultyInCharge() { return facultyInCharge; }
    public String getOrganizerId() { return organizerId; }
    public String getRegistrationInfo() { return registrationInfo; }
    public String getContactInfo() { return contactInfo; }
    public String getStatus() { return status; }
    public String getRejectionReason() { return rejectionReason; }
    public String getReviewedBy() { return reviewedBy; }
    public Timestamp getReviewedAt() { return reviewedAt; }
    public Timestamp getPublishedAt() { return publishedAt; }
    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }

    // ── Setters ──

    public void setEventId(String eventId) { this.eventId = eventId; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public void setCategory(String category) { this.category = category; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }
    public void setDescription(String description) { this.description = description; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setVenue(String venue) { this.venue = venue; }
    public void setOrganizingTeam(String organizingTeam) { this.organizingTeam = organizingTeam; }
    public void setFacultyInCharge(String facultyInCharge) { this.facultyInCharge = facultyInCharge; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
    public void setRegistrationInfo(String registrationInfo) { this.registrationInfo = registrationInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
    public void setStatus(String status) { this.status = status; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    public void setReviewedAt(Timestamp reviewedAt) { this.reviewedAt = reviewedAt; }
    public void setPublishedAt(Timestamp publishedAt) { this.publishedAt = publishedAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    // ── Helpers ──

    @Exclude
    public boolean isDraft() { return STATUS_DRAFT.equals(status); }
    @Exclude
    public boolean isPending() { return STATUS_PENDING.equals(status); }
    @Exclude
    public boolean isApproved() { return STATUS_APPROVED.equals(status); }
    @Exclude
    public boolean isRejected() { return STATUS_REJECTED.equals(status); }
    @Exclude
    public boolean isPublished() { return STATUS_PUBLISHED.equals(status); }
    @Exclude
    public boolean isCancelled() { return STATUS_CANCELLED.equals(status); }

    @Exclude
    public boolean canEdit() {
        return STATUS_DRAFT.equals(status) || STATUS_REJECTED.equals(status);
    }

    @Exclude
    public boolean canSubmit() {
        return STATUS_DRAFT.equals(status) || STATUS_REJECTED.equals(status);
    }

    @Exclude
    public boolean canPublish() {
        return STATUS_APPROVED.equals(status);
    }

    /**
     * Returns an array of all event category options.
     */
    public static String[] getAllCategories() {
        return new String[]{
            CAT_WORKSHOP, CAT_COMPETITION, CAT_FESTIVAL, CAT_SEMINAR,
            CAT_SPORTS, CAT_CULTURAL, CAT_TECHNICAL, CAT_SOCIAL,
            CAT_GUEST_LECTURE, CAT_OTHER
        };
    }
}
