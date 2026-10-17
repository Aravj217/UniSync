package com.example.unisync.models;

import com.google.firebase.firestore.DocumentId;

/**
 * AcademicDepartment model — represents an academic department in UniSync.
 * Stored in the "academicDepartments" Firestore collection.
 */
public class AcademicDepartment {

    public static final String COLLECTION_NAME = "academicDepartments";

    @DocumentId
    private String departmentId;
    private String departmentName;
    private String schoolName;
    private String facultyInCharge;
    private String officialEmail;
    private boolean active;

    public AcademicDepartment() {
        this.active = true;
    }

    public AcademicDepartment(String departmentId, String departmentName, String schoolName, String facultyInCharge, String officialEmail) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.schoolName = schoolName;
        this.facultyInCharge = facultyInCharge;
        this.officialEmail = officialEmail;
        this.active = true;
    }

    public String getDepartmentId() { return departmentId; }
    public String getDepartmentName() { return departmentName; }
    public String getSchoolName() { return schoolName; }
    public String getFacultyInCharge() { return facultyInCharge; }
    public String getOfficialEmail() { return officialEmail; }
    public boolean isActive() { return active; }

    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public void setFacultyInCharge(String facultyInCharge) { this.facultyInCharge = facultyInCharge; }
    public void setOfficialEmail(String officialEmail) { this.officialEmail = officialEmail; }
    public void setActive(boolean active) { this.active = active; }
}
