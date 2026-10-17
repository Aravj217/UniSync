package com.example.unisync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.unisync.models.AdminLog;
import com.example.unisync.models.Event;
import com.example.unisync.models.Organizer;
import com.example.unisync.models.User;
import com.example.unisync.models.VolunteerAssignment;

import org.junit.Test;

/**
 * Unit tests verifying UniSync models, role permissions, and assignment rules.
 */
public class UniSyncUnitTest {

    @Test
    public void testUserRolesAndTypes() {
        User studentUser = new User("uid_1", "Jane Student", "student@nmims.edu", User.ROLE_USER, User.USER_TYPE_STUDENT);
        assertTrue(studentUser.isUser());
        assertTrue(studentUser.isStudent());
        assertFalse(studentUser.isTeacher());
        assertFalse(studentUser.isAdmin());
        assertFalse(studentUser.isOrganizer());

        User teacherUser = new User("uid_2", "Prof. Sharma", "prof@nmims.edu", User.ROLE_USER, User.USER_TYPE_TEACHER);
        assertTrue(teacherUser.isUser());
        assertFalse(teacherUser.isStudent());
        assertTrue(teacherUser.isTeacher());

        User adminUser = new User("uid_admin", "Admin Officer", "admin@nmims.edu", User.ROLE_ADMIN);
        assertTrue(adminUser.isAdmin());
        assertFalse(adminUser.isOrganizer());

        User orgUser = new User("uid_org", "ACM President", "acm@nmims.edu", User.ROLE_ORGANIZER);
        assertTrue(orgUser.isOrganizer());
        assertFalse(orgUser.isAdmin());
    }

    @Test
    public void testVolunteerAssignmentLifecycle() {
        VolunteerAssignment assignment = new VolunteerAssignment();
        assignment.setAssignmentId("asgn_101");
        assignment.setEventId("event_techfest");
        assignment.setEventName("NMIMS Tech Fest");
        assignment.setUserId("student_123");
        assignment.setUserName("Jane Doe");
        assignment.setUserEmail("jane.doe@nmims.edu");
        assignment.setAssignedDuty("Registration Desk");
        assignment.setEventDate("2027-02-16");
        assignment.setStartTime("09:00");
        assignment.setEndTime("12:00");
        assignment.setVenue("Main Auditorium");
        assignment.setStatus(VolunteerAssignment.STATUS_PENDING);

        assertEquals(VolunteerAssignment.STATUS_PENDING, assignment.getStatus());
        assertEquals("student_123", assignment.getUserId());
        assertEquals("jane.doe@nmims.edu", assignment.getUserEmail());

        // Admin decision flow: Reject with reason
        assignment.setStatus(VolunteerAssignment.STATUS_REJECTED);
        assignment.setRejectionReason("Duty conflict with morning lecture");
        assignment.setReviewedBy("admin_uid");
        assertEquals(VolunteerAssignment.STATUS_REJECTED, assignment.getStatus());
        assertNotNull(assignment.getRejectionReason());

        // Correction and resubmission
        assignment.setStatus(VolunteerAssignment.STATUS_PENDING);
        assignment.setStartTime("13:00");
        assignment.setEndTime("16:00");
        assertEquals(VolunteerAssignment.STATUS_PENDING, assignment.getStatus());

        // Admin Approval
        assignment.setStatus(VolunteerAssignment.STATUS_APPROVED);
        assertEquals(VolunteerAssignment.STATUS_APPROVED, assignment.getStatus());
    }

    @Test
    public void testTimeOverlapLogic() {
        // Time 1: 09:00 to 12:00
        int start1 = 9 * 60;
        int end1 = 12 * 60;

        // Time 2 (overlapping): 11:00 to 14:00
        int start2 = 11 * 60;
        int end2 = 14 * 60;

        // Overlap condition: start1 < end2 && end1 > start2
        boolean overlaps = (start1 < end2) && (end1 > start2);
        assertTrue(overlaps);

        // Time 3 (non-overlapping): 13:00 to 16:00
        int start3 = 13 * 60;
        int end3 = 16 * 60;
        boolean overlapsNonOverlapping = (start1 < end3) && (end1 > start3);
        assertFalse(overlapsNonOverlapping);
    }

    @Test
    public void testOrganizerOrganizationTypes() {
        Organizer clubOrg = new Organizer("org_1", "uid_org1", "ACM Student Chapter", "acm@nmims.edu", Organizer.TYPE_CLUB);
        assertEquals(Organizer.TYPE_CLUB, clubOrg.getOrganizationType());

        Organizer deptOrg = new Organizer("org_2", "uid_org2", "Computer Engineering Dept", "ce@nmims.edu", Organizer.TYPE_ACADEMIC_DEPARTMENT);
        assertEquals(Organizer.TYPE_ACADEMIC_DEPARTMENT, deptOrg.getOrganizationType());

        Organizer teamOrg = new Organizer("org_3", "uid_org3", "Cultural Core Team", "cultural@nmims.edu", Organizer.TYPE_EVENT_TEAM);
        assertEquals(Organizer.TYPE_EVENT_TEAM, teamOrg.getOrganizationType());
    }

    @Test
    public void testAdminLogActions() {
        assertEquals("APPROVE_ASSIGNMENT", AdminLog.ACTION_APPROVE_ASSIGNMENT);
        assertEquals("REJECT_ASSIGNMENT", AdminLog.ACTION_REJECT_ASSIGNMENT);
        assertEquals("RETURN_ASSIGNMENT", AdminLog.ACTION_RETURN_ASSIGNMENT);
        assertEquals("WITHDRAW_ASSIGNMENT", AdminLog.ACTION_WITHDRAW_ASSIGNMENT);
        assertEquals("PUBLISH_EVENT", AdminLog.ACTION_PUBLISH_EVENT);
    }
}
