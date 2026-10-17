package com.example.unisync.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

/**
 * Organizer model — represents an official college club, academic department,
 * or event management team organizer in UniSync.
 * Stored in the "organizers" Firestore collection.
 */
public class Organizer {

    public static final String COLLECTION_NAME = "organizers";

    // Organization types
    public static final String TYPE_CLUB = "CLUB";
    public static final String TYPE_ACADEMIC_DEPARTMENT = "ACADEMIC_DEPARTMENT";
    public static final String TYPE_EVENT_TEAM = "EVENT_TEAM";

    @DocumentId
    private String organizerId;
    private String uid;
    private String officialName;
    private String officialEmail;
    private String organizationType;
    private String clubId;
    private String departmentId;
    private String facultyInCharge;
    private boolean active;

    @ServerTimestamp
    private Timestamp createdAt;

    public Organizer() {
        this.active = true;
        this.organizationType = TYPE_CLUB;
    }

    public Organizer(String organizerId, String uid, String officialName, String officialEmail, String organizationType) {
        this.organizerId = organizerId;
        this.uid = uid;
        this.officialName = officialName;
        this.officialEmail = officialEmail;
        this.organizationType = organizationType;
        this.active = true;
    }

    // ── Getters ──

    public String getOrganizerId() { return organizerId; }
    public String getUid() { return uid; }
    public String getOfficialName() { return officialName; }
    public String getOfficialEmail() { return officialEmail; }
    public String getOrganizationType() { return organizationType; }
    public String getClubId() { return clubId; }
    public String getDepartmentId() { return departmentId; }
    public String getFacultyInCharge() { return facultyInCharge; }
    public boolean isActive() { return active; }
    public Timestamp getCreatedAt() { return createdAt; }

    // ── Setters ──

    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
    public void setUid(String uid) { this.uid = uid; }
    public void setOfficialName(String officialName) { this.officialName = officialName; }
    public void setOfficialEmail(String officialEmail) { this.officialEmail = officialEmail; }
    public void setOrganizationType(String organizationType) { this.organizationType = organizationType; }
    public void setClubId(String clubId) { this.clubId = clubId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }
    public void setFacultyInCharge(String facultyInCharge) { this.facultyInCharge = facultyInCharge; }
    public void setActive(boolean active) { this.active = active; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
