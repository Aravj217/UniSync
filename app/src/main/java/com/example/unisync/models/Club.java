package com.example.unisync.models;

import com.google.firebase.firestore.DocumentId;

/**
 * Club model — represents an authorized college club in UniSync.
 * Stored in the "clubs" Firestore collection.
 */
public class Club {

    public static final String COLLECTION_NAME = "clubs";

    @DocumentId
    private String clubId;
    private String clubName;
    private String description;
    private String facultyInCharge;
    private String officialEmail;
    private boolean active;

    public Club() {
        this.active = true;
    }

    public Club(String clubId, String clubName, String description, String facultyInCharge, String officialEmail) {
        this.clubId = clubId;
        this.clubName = clubName;
        this.description = description;
        this.facultyInCharge = facultyInCharge;
        this.officialEmail = officialEmail;
        this.active = true;
    }

    public String getClubId() { return clubId; }
    public String getClubName() { return clubName; }
    public String getDescription() { return description; }
    public String getFacultyInCharge() { return facultyInCharge; }
    public String getOfficialEmail() { return officialEmail; }
    public boolean isActive() { return active; }

    public void setClubId(String clubId) { this.clubId = clubId; }
    public void setClubName(String clubName) { this.clubName = clubName; }
    public void setDescription(String description) { this.description = description; }
    public void setFacultyInCharge(String facultyInCharge) { this.facultyInCharge = facultyInCharge; }
    public void setOfficialEmail(String officialEmail) { this.officialEmail = officialEmail; }
    public void setActive(boolean active) { this.active = active; }
}
