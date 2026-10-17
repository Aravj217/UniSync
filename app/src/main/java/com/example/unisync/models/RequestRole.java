package com.example.unisync.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

/**
 * RequestRole model — represents a role/duty that still needs
 * people to be assigned for a team support request.
 *
 * Stored as subcollection: teamRequests/{requestId}/roles
 */
public class RequestRole {

    @DocumentId
    private String roleId;
    private String dutyTitle;
    private String description;
    private int peopleRequired;
    private int peopleAssigned;
    private String requiredSkills;
    private String requiredDepartment;

    @ServerTimestamp
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Required empty constructor for Firestore
    public RequestRole() {
        this.peopleAssigned = 0;
    }

    // ── Getters ──

    public String getRoleId() { return roleId; }
    public String getDutyTitle() { return dutyTitle; }
    public String getDescription() { return description; }
    public int getPeopleRequired() { return peopleRequired; }
    public int getPeopleAssigned() { return peopleAssigned; }
    public String getRequiredSkills() { return requiredSkills; }
    public String getRequiredDepartment() { return requiredDepartment; }
    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }

    // ── Setters ──

    public void setRoleId(String roleId) { this.roleId = roleId; }
    public void setDutyTitle(String dutyTitle) { this.dutyTitle = dutyTitle; }
    public void setDescription(String description) { this.description = description; }
    public void setPeopleRequired(int peopleRequired) { this.peopleRequired = peopleRequired; }
    public void setPeopleAssigned(int peopleAssigned) { this.peopleAssigned = peopleAssigned; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }
    public void setRequiredDepartment(String requiredDepartment) { this.requiredDepartment = requiredDepartment; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    // ── Helpers ──

    public int getRemainingSlots() {
        return Math.max(0, peopleRequired - peopleAssigned);
    }

    public boolean isFilled() {
        return peopleAssigned >= peopleRequired;
    }
}
