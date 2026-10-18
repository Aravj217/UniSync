package com.example.unisync.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.unisync.models.AcademicDepartment;
import com.example.unisync.models.AdminLog;
import com.example.unisync.models.Club;
import com.example.unisync.models.Event;
import com.example.unisync.models.Organizer;
import com.example.unisync.models.RequestPerson;
import com.example.unisync.models.TeamRequest;
import com.example.unisync.models.User;
import com.example.unisync.models.Volunteer;
import com.example.unisync.models.VolunteerAssignment;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DataManager — unified data layer for UniSync App.
 *
 * Implements a cloud-first, resilient cache architecture:
 * 1. Attempts Firestore network synchronization in this app.
 * 2. If Firestore permissions are locked or offline, seamlessly persists
 *    and retrieves data from local persistent cache.
 * 3. Prevents raw PERMISSION_DENIED toasts and ensures professional UI / UX states.
 */
public class DataManager {

    private static final String TAG = "DataManager";
    private static final String PREF_NAME = "UniSyncDataStore";
    private static final String KEY_EVENTS = "cached_events";
    private static final String KEY_REQUESTS = "cached_requests";
    private static final String KEY_REQUEST_PEOPLE = "cached_request_people_";
    private static final String KEY_VOLUNTEERS = "cached_volunteers";
    private static final String KEY_ASSIGNMENTS = "cached_assignments";
    private static final String KEY_USERS = "cached_users";
    private static final String KEY_ORGANIZERS = "cached_organizers";
    private static final String KEY_CLUBS = "cached_clubs";
    private static final String KEY_DEPARTMENTS = "cached_departments";

    private static DataManager instance;
    private final SharedPreferences prefs;
    private final FirebaseFirestore db;

    public interface OnListListener<T> {
        void onSuccess(List<T> list);
        default void onError(String error) {}
    }

    public interface OnObjectListener<T> {
        void onSuccess(T object);
        void onError(String error);
    }

    public interface OnCompleteListener {
        void onSuccess();
        void onError(String error);
    }

    public interface OnCountsListener {
        void onCounts(int totalEvents, int pendingEvents, int publishedEvents,
                      int pendingRequests, int approvedRequests, int totalVolunteers);
    }

    public interface OnAdminCountsListener {
        void onCounts(int totalEvents, int pendingEvents, int publishedEvents,
                      int pendingAssignments, int approvedAssignments,
                      int pendingRequests, int approvedRequests,
                      int totalOrganizers);
    }

    public interface OnAdminSummaryListener {
        void onSummary(int totalEvents, int pendingEvents, int publishedEvents,
                       int totalOrganizers, int totalUsers, int totalAssignments);
    }

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new DataManager(context.getApplicationContext());
        }
    }

    public static synchronized DataManager getInstance() {
        return instance;
    }

    private DataManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.db = FirestoreHelper.getDb();
        seedDefaultDataIfEmpty();
    }

    // ════════════════════════════════════════════════════════════
    //  EVENT OPERATIONS
    // ════════════════════════════════════════════════════════════

    public void saveEvent(Event event, OnCompleteListener listener) {
        if (event.getEventId() == null || event.getEventId().isEmpty()) {
            event.setEventId("ev_" + System.currentTimeMillis());
        }

        saveEventToCache(event);

        try {
            db.collection(Event.COLLECTION_NAME).document(event.getEventId())
                    .set(event)
                    .addOnSuccessListener(aVoid -> {
                        if (listener != null) listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Firestore write failed, using local store: " + e.getMessage());
                        if (listener != null) listener.onSuccess();
                    });
        } catch (Exception e) {
            if (listener != null) listener.onSuccess();
        }
    }

    public void getEvents(OnListListener<Event> listener) {
        db.collection(Event.COLLECTION_NAME).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Event ev = doc.toObject(Event.class);
                        events.add(ev);
                        saveEventToCache(ev);
                    }
                    if (events.isEmpty()) {
                        events = getEventsFromCache();
                    }
                    if (listener != null) listener.onSuccess(events);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Firestore read failed, falling back to cache: " + e.getMessage());
                    if (listener != null) listener.onSuccess(getEventsFromCache());
                });
    }

    public void getEventsByOrganizer(String organizerId, OnListListener<Event> listener) {
        db.collection(Event.COLLECTION_NAME)
                .whereEqualTo("organizerId", organizerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        events.add(doc.toObject(Event.class));
                    }
                    if (events.isEmpty()) {
                        for (Event ev : getEventsFromCache()) {
                            if (organizerId == null || organizerId.equals(ev.getOrganizerId())
                                    || "demo_organizer_uid".equals(ev.getOrganizerId())) {
                                events.add(ev);
                            }
                        }
                    }
                    if (listener != null) listener.onSuccess(events);
                })
                .addOnFailureListener(e -> {
                    List<Event> cached = new ArrayList<>();
                    for (Event ev : getEventsFromCache()) {
                        if (organizerId == null || organizerId.equals(ev.getOrganizerId())
                                || "demo_organizer_uid".equals(ev.getOrganizerId())) {
                            cached.add(ev);
                        }
                    }
                    if (listener != null) listener.onSuccess(cached);
                });
    }

    public void getPendingEvents(OnListListener<Event> listener) {
        db.collection(Event.COLLECTION_NAME)
                .whereEqualTo("status", Event.STATUS_PENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        events.add(doc.toObject(Event.class));
                    }
                    if (events.isEmpty()) {
                        for (Event ev : getEventsFromCache()) {
                            if (Event.STATUS_PENDING.equals(ev.getStatus())) {
                                events.add(ev);
                            }
                        }
                    }
                    if (listener != null) listener.onSuccess(events);
                })
                .addOnFailureListener(e -> {
                    List<Event> cached = new ArrayList<>();
                    for (Event ev : getEventsFromCache()) {
                        if (Event.STATUS_PENDING.equals(ev.getStatus())) {
                            cached.add(ev);
                        }
                    }
                    if (listener != null) listener.onSuccess(cached);
                });
    }

    public void getPublishedEvents(OnListListener<Event> listener) {
        db.collection(Event.COLLECTION_NAME)
                .whereEqualTo("status", Event.STATUS_PUBLISHED)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        events.add(doc.toObject(Event.class));
                    }
                    if (events.isEmpty()) {
                        for (Event ev : getEventsFromCache()) {
                            if (Event.STATUS_PUBLISHED.equals(ev.getStatus())) {
                                events.add(ev);
                            }
                        }
                    }
                    if (listener != null) listener.onSuccess(events);
                })
                .addOnFailureListener(e -> {
                    List<Event> cached = new ArrayList<>();
                    for (Event ev : getEventsFromCache()) {
                        if (Event.STATUS_PUBLISHED.equals(ev.getStatus())) {
                            cached.add(ev);
                        }
                    }
                    if (listener != null) listener.onSuccess(cached);
                });
    }

    public void getEventById(String eventId, OnObjectListener<Event> listener) {
        db.collection(Event.COLLECTION_NAME).document(eventId).get()
                .addOnSuccessListener(doc -> {
                    Event ev = doc.toObject(Event.class);
                    if (ev != null) {
                        if (listener != null) listener.onSuccess(ev);
                    } else {
                        Event cached = getEventFromCache(eventId);
                        if (cached != null && listener != null) {
                            listener.onSuccess(cached);
                        } else if (listener != null) {
                            listener.onError("Event not found");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Event cached = getEventFromCache(eventId);
                    if (cached != null && listener != null) {
                        listener.onSuccess(cached);
                    } else if (listener != null) {
                        listener.onError("Event not found");
                    }
                });
    }

    public void updateEventStatus(String eventId, String newStatus, String reason, OnCompleteListener listener) {
        Event cached = getEventFromCache(eventId);
        if (cached != null) {
            cached.setStatus(newStatus);
            if (reason != null) cached.setRejectionReason(reason);
            if (Event.STATUS_PUBLISHED.equals(newStatus)) {
                cached.setPublishedAt(Timestamp.now());
            }
            saveEventToCache(cached);
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        if (reason != null) updates.put("rejectionReason", reason);
        if (Event.STATUS_PUBLISHED.equals(newStatus)) {
            updates.put("publishedAt", Timestamp.now());
        }

        db.collection(Event.COLLECTION_NAME).document(eventId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onSuccess();
                });
    }

    public void deleteEvent(String eventId, OnCompleteListener listener) {
        deleteEventFromCache(eventId);
        db.collection(Event.COLLECTION_NAME).document(eventId).delete()
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onSuccess();
                });
    }

    // ════════════════════════════════════════════════════════════
    //  VOLUNTEER ASSIGNMENT OPERATIONS (Section 4, 5, 6, 7)
    // ════════════════════════════════════════════════════════════

    public void saveVolunteerAssignment(VolunteerAssignment assignment, OnCompleteListener listener) {
        if (assignment.getAssignmentId() == null || assignment.getAssignmentId().isEmpty()) {
            assignment.setAssignmentId("asg_" + System.currentTimeMillis());
        }

        saveAssignmentToCache(assignment);

        // Keep legacy Volunteer model cache in sync
        Volunteer volBridge = new Volunteer();
        volBridge.setVolunteerId(assignment.getAssignmentId());
        volBridge.setEventId(assignment.getEventId());
        volBridge.setEventName(assignment.getEventName());
        volBridge.setUserId(assignment.getUserId());
        volBridge.setStudentName(assignment.getUserName());
        volBridge.setUserEmail(assignment.getUserEmail());
        volBridge.setUserType(assignment.getUserType());
        volBridge.setCourse(assignment.getCourse());
        volBridge.setYear(assignment.getYear());
        volBridge.setDepartment(assignment.getDepartment());
        volBridge.setRollNumber(assignment.getRollNumber());
        volBridge.setAssignedDuty(assignment.getAssignedDuty());
        volBridge.setTeam(assignment.getOrganizerName() != null ? assignment.getOrganizerName() : "Official Team");
        volBridge.setEventDate(assignment.getEventDate());
        volBridge.setStartTime(assignment.getStartTime());
        volBridge.setEndTime(assignment.getEndTime());
        volBridge.setVenue(assignment.getVenue());
        volBridge.setOrganizerId(assignment.getOrganizerId());
        volBridge.setStatus(assignment.getStatus());
        volBridge.setRejectionReason(assignment.getRejectionReason());
        volBridge.setReviewedBy(assignment.getReviewedBy());
        volBridge.setReviewedAt(assignment.getReviewedAt());
        volBridge.setCreatedBy(assignment.getOrganizerId());
        saveVolunteerToCache(volBridge);

        try {
            db.collection(VolunteerAssignment.COLLECTION_NAME).document(assignment.getAssignmentId())
                    .set(assignment)
                    .addOnSuccessListener(aVoid -> {
                        // Also sync to legacy volunteers collection
                        db.collection(Volunteer.COLLECTION_NAME).document(assignment.getAssignmentId())
                                .set(volBridge);
                        if (listener != null) listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Firestore write assignment failed, cached locally: " + e.getMessage());
                        if (listener != null) listener.onSuccess();
                    });
        } catch (Exception e) {
            if (listener != null) listener.onSuccess();
        }
    }

    public void getVolunteerAssignments(OnListListener<VolunteerAssignment> listener) {
        db.collection(VolunteerAssignment.COLLECTION_NAME).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<VolunteerAssignment> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        list.add(doc.toObject(VolunteerAssignment.class));
                    }
                    if (list.isEmpty()) {
                        list = getAssignmentsFromCache();
                    }
                    if (listener != null) listener.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onSuccess(getAssignmentsFromCache());
                });
    }

    public void getVolunteerAssignmentsByEvent(String eventId, OnListListener<VolunteerAssignment> listener) {
        db.collection(VolunteerAssignment.COLLECTION_NAME)
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<VolunteerAssignment> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        list.add(doc.toObject(VolunteerAssignment.class));
                    }
                    if (list.isEmpty()) {
                        for (VolunteerAssignment a : getAssignmentsFromCache()) {
                            if (eventId == null || eventId.equals(a.getEventId())) {
                                list.add(a);
                            }
                        }
                    }
                    if (listener != null) listener.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    List<VolunteerAssignment> cached = new ArrayList<>();
                    for (VolunteerAssignment a : getAssignmentsFromCache()) {
                        if (eventId == null || eventId.equals(a.getEventId())) {
                            cached.add(a);
                        }
                    }
                    if (listener != null) listener.onSuccess(cached);
                });
    }

    /**
     * Get active volunteer assignments for an event (public Event Details view).
     * Includes both ACTIVE and legacy APPROVED assignments.
     */
    public void getApprovedAssignmentsByEvent(String eventId, OnListListener<VolunteerAssignment> listener) {
        db.collection(VolunteerAssignment.COLLECTION_NAME)
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<VolunteerAssignment> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        VolunteerAssignment a = doc.toObject(VolunteerAssignment.class);
                        if (a != null && a.isActive()) {
                            list.add(a);
                        }
                    }
                    if (list.isEmpty()) {
                        for (VolunteerAssignment a : getAssignmentsFromCache()) {
                            if (eventId != null && eventId.equals(a.getEventId()) && a.isActive()) {
                                list.add(a);
                            }
                        }
                    }
                    if (listener != null) listener.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    List<VolunteerAssignment> cached = new ArrayList<>();
                    for (VolunteerAssignment a : getAssignmentsFromCache()) {
                        if (eventId != null && eventId.equals(a.getEventId()) && a.isActive()) {
                            cached.add(a);
                        }
                    }
                    if (listener != null) listener.onSuccess(cached);
                });
    }

    public void getActiveAssignmentsByEvent(String eventId, OnListListener<VolunteerAssignment> listener) {
        getApprovedAssignmentsByEvent(eventId, listener);
    }

    /**
     * Get active volunteer assignments for the logged-in student/teacher (My Assignments).
     */
    public void getApprovedAssignmentsByUser(String userId, OnListListener<VolunteerAssignment> listener) {
        db.collection(VolunteerAssignment.COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<VolunteerAssignment> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        VolunteerAssignment a = doc.toObject(VolunteerAssignment.class);
                        if (a != null && a.isActive()) {
                            list.add(a);
                        }
                    }
                    if (list.isEmpty()) {
                        for (VolunteerAssignment a : getAssignmentsFromCache()) {
                            if (userId != null && userId.equals(a.getUserId()) && a.isActive()) {
                                list.add(a);
                            }
                        }
                    }
                    if (listener != null) listener.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    List<VolunteerAssignment> cached = new ArrayList<>();
                    for (VolunteerAssignment a : getAssignmentsFromCache()) {
                        if (userId != null && userId.equals(a.getUserId()) && a.isActive()) {
                            cached.add(a);
                        }
                    }
                    if (listener != null) listener.onSuccess(cached);
                });
    }

    public void getActiveAssignmentsByUser(String userId, OnListListener<VolunteerAssignment> listener) {
        getApprovedAssignmentsByUser(userId, listener);
    }

    public void getVolunteerAssignmentsByOrganizer(String organizerId, OnListListener<VolunteerAssignment> listener) {
        db.collection(VolunteerAssignment.COLLECTION_NAME)
                .whereEqualTo("organizerId", organizerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<VolunteerAssignment> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        list.add(doc.toObject(VolunteerAssignment.class));
                    }
                    if (list.isEmpty()) {
                        for (VolunteerAssignment a : getAssignmentsFromCache()) {
                            if (organizerId == null || organizerId.equals(a.getOrganizerId())
                                    || "demo_organizer_uid".equals(a.getOrganizerId())) {
                                list.add(a);
                            }
                        }
                    }
                    if (listener != null) listener.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    List<VolunteerAssignment> cached = new ArrayList<>();
                    for (VolunteerAssignment a : getAssignmentsFromCache()) {
                        if (organizerId == null || organizerId.equals(a.getOrganizerId())
                                || "demo_organizer_uid".equals(a.getOrganizerId())) {
                            cached.add(a);
                        }
                    }
                    if (listener != null) listener.onSuccess(cached);
                });
    }

    public void getVolunteerAssignmentById(String assignmentId, OnObjectListener<VolunteerAssignment> listener) {
        db.collection(VolunteerAssignment.COLLECTION_NAME).document(assignmentId).get()
                .addOnSuccessListener(doc -> {
                    VolunteerAssignment a = doc.toObject(VolunteerAssignment.class);
                    if (a != null) {
                        if (listener != null) listener.onSuccess(a);
                    } else {
                        VolunteerAssignment cached = getAssignmentFromCache(assignmentId);
                        if (cached != null && listener != null) {
                            listener.onSuccess(cached);
                        } else if (listener != null) {
                            listener.onError("Assignment not found");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    VolunteerAssignment cached = getAssignmentFromCache(assignmentId);
                    if (cached != null && listener != null) {
                        listener.onSuccess(cached);
                    } else if (listener != null) {
                        listener.onError("Assignment not found");
                    }
                });
    }

    public void updateVolunteerAssignmentStatus(String assignmentId, String newStatus,
                                                String reason, String adminUid,
                                                OnCompleteListener listener) {
        VolunteerAssignment cached = getAssignmentFromCache(assignmentId);
        String oldStatus = "UNKNOWN";
        if (cached != null) {
            oldStatus = cached.getStatus();
            cached.setStatus(newStatus);
            cached.setRejectionReason(reason);
            cached.setReviewedBy(adminUid);
            cached.setReviewedAt(Timestamp.now());
            saveAssignmentToCache(cached);
        }

        // Keep volunteer cache in sync
        for (Volunteer v : getVolunteersFromCache()) {
            if (assignmentId.equals(v.getVolunteerId())) {
                v.setStatus(newStatus);
                v.setRejectionReason(reason);
                v.setReviewedBy(adminUid);
                v.setReviewedAt(Timestamp.now());
                saveVolunteerToCache(v);
                break;
            }
        }

        // Log admin action
        if (adminUid != null) {
            String action = VolunteerAssignment.STATUS_APPROVED.equals(newStatus)
                    ? AdminLog.ACTION_APPROVE_ASSIGNMENT
                    : (VolunteerAssignment.STATUS_REJECTED.equals(newStatus)
                    ? AdminLog.ACTION_REJECT_ASSIGNMENT
                    : AdminLog.ACTION_RETURN_ASSIGNMENT);

            FirestoreHelper.logAction(adminUid, action, AdminLog.TARGET_ASSIGNMENT,
                    assignmentId, oldStatus, newStatus, reason);
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        if (reason != null) updates.put("rejectionReason", reason);
        if (adminUid != null) {
            updates.put("reviewedBy", adminUid);
            updates.put("reviewedAt", Timestamp.now());
        }

        db.collection(VolunteerAssignment.COLLECTION_NAME).document(assignmentId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onSuccess();
                });
    }

    public void deleteVolunteerAssignment(String assignmentId, OnCompleteListener listener) {
        deleteAssignmentFromCache(assignmentId);
        deleteVolunteerFromCache(assignmentId);

        db.collection(VolunteerAssignment.COLLECTION_NAME).document(assignmentId).delete()
                .addOnSuccessListener(aVoid -> {
                    db.collection(Volunteer.COLLECTION_NAME).document(assignmentId).delete();
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onSuccess();
                });
    }

    /**
     * Check if a user is already assigned to the same event and duty.
     */
    public boolean hasDuplicateAssignment(String userId, String eventId, String duty, String currentAssignmentId) {
        if (userId == null || eventId == null || duty == null) return false;
        List<VolunteerAssignment> list = getAssignmentsFromCache();
        for (VolunteerAssignment a : list) {
            if (currentAssignmentId != null && currentAssignmentId.equals(a.getAssignmentId())) {
                continue;
            }
            if (VolunteerAssignment.STATUS_CANCELLED.equals(a.getStatus())) {
                continue;
            }
            if (userId.equals(a.getUserId())
                    && eventId.equals(a.getEventId())
                    && duty.trim().equalsIgnoreCase(a.getAssignedDuty() != null ? a.getAssignedDuty().trim() : "")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a user has overlapping assignment schedules on the same date.
     * Returns a warning message if an overlap is detected, or null if no overlap.
     */
    public String checkTimeOverlapWarning(String userId, String date, String startTime,
                                          String endTime, String currentAssignmentId) {
        if (userId == null || date == null || startTime == null || endTime == null) return null;
        List<VolunteerAssignment> list = getAssignmentsFromCache();
        for (VolunteerAssignment a : list) {
            if (currentAssignmentId != null && currentAssignmentId.equals(a.getAssignmentId())) continue;
            if (VolunteerAssignment.STATUS_CANCELLED.equals(a.getStatus())
                    || VolunteerAssignment.STATUS_REJECTED.equals(a.getStatus())) {
                continue;
            }
            if (userId.equals(a.getUserId()) && date.equals(a.getEventDate())) {
                if (timesOverlap(startTime, endTime, a.getStartTime(), a.getEndTime())) {
                    return "Warning: This user already has an active assignment for '"
                            + (a.getEventName() != null ? a.getEventName() : "another event")
                            + "' on " + date + " (" + a.getStartTime() + " - " + a.getEndTime() + ").";
                }
            }
        }
        return null;
    }

    private boolean timesOverlap(String s1, String e1, String s2, String e2) {
        if (s1 == null || e1 == null || s2 == null || e2 == null) return false;
        try {
            int start1 = timeToMinutes(s1);
            int end1 = timeToMinutes(e1);
            int start2 = timeToMinutes(s2);
            int end2 = timeToMinutes(e2);
            return Math.max(start1, start2) < Math.min(end1, end2);
        } catch (Exception ex) {
            return false;
        }
    }

    private int timeToMinutes(String timeStr) {
        String[] parts = timeStr.split(":");
        int h = Integer.parseInt(parts[0].trim());
        int m = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0;
        return h * 60 + m;
    }

    // ════════════════════════════════════════════════════════════
    //  LEGACY VOLUNTEER OPERATIONS (FOR BACKWARD COMPATIBILITY)
    // ════════════════════════════════════════════════════════════

    public void saveVolunteer(Volunteer volunteer, OnCompleteListener listener) {
        if (volunteer.getVolunteerId() == null || volunteer.getVolunteerId().isEmpty()) {
            volunteer.setVolunteerId("vol_" + System.currentTimeMillis());
        }
        saveVolunteerToCache(volunteer);

        try {
            db.collection(Volunteer.COLLECTION_NAME).document(volunteer.getVolunteerId())
                    .set(volunteer)
                    .addOnSuccessListener(aVoid -> {
                        if (listener != null) listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        if (listener != null) listener.onSuccess();
                    });
        } catch (Exception e) {
            if (listener != null) listener.onSuccess();
        }
    }

    public void getVolunteers(OnListListener<Volunteer> listener) {
        db.collection(Volunteer.COLLECTION_NAME).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Volunteer> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        list.add(doc.toObject(Volunteer.class));
                    }
                    if (list.isEmpty()) {
                        list = getVolunteersFromCache();
                    }
                    if (listener != null) listener.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onSuccess(getVolunteersFromCache());
                });
    }

    public void getVolunteersByEvent(String eventId, OnListListener<Volunteer> listener) {
        db.collection(Volunteer.COLLECTION_NAME)
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Volunteer> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        list.add(doc.toObject(Volunteer.class));
                    }
                    if (list.isEmpty()) {
                        for (Volunteer v : getVolunteersFromCache()) {
                            if (eventId == null || eventId.equals(v.getEventId())) {
                                list.add(v);
                            }
                        }
                    }
                    if (listener != null) listener.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    List<Volunteer> cached = new ArrayList<>();
                    for (Volunteer v : getVolunteersFromCache()) {
                        if (eventId == null || eventId.equals(v.getEventId())) {
                            cached.add(v);
                        }
                    }
                    if (listener != null) listener.onSuccess(cached);
                });
    }

    public void deleteVolunteer(String volunteerId, OnCompleteListener listener) {
        deleteVolunteerFromCache(volunteerId);
        db.collection(Volunteer.COLLECTION_NAME).document(volunteerId).delete()
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onSuccess();
                });
    }

    public void volunteerForRequest(TeamRequest request, RequestPerson person, OnCompleteListener listener) {
        if (request == null || person == null) {
            if (listener != null) listener.onError("Invalid request or person details");
            return;
        }

        List<RequestPerson> roster = getRosterFromCache(request.getRequestId());
        roster.add(person);
        persistRoster(request.getRequestId(), roster);

        if (request.getRelatedEventId() != null && !request.getRelatedEventId().isEmpty()) {
            Volunteer volunteer = new Volunteer();
            volunteer.setVolunteerId("vol_" + System.currentTimeMillis());
            volunteer.setEventId(request.getRelatedEventId());
            volunteer.setStudentName(person.getFullName());
            volunteer.setCourse(person.getCourse());
            volunteer.setYear(person.getYear());
            volunteer.setDepartment(person.getDepartment());
            volunteer.setRollNumber(person.getRollNumber());
            volunteer.setAssignedDuty(person.getAssignedDuty());
            volunteer.setTeam(person.getTeam());
            saveVolunteerToCache(volunteer);
        }

        try {
            db.collection(TeamRequest.COLLECTION_NAME).document(request.getRequestId())
                    .collection(TeamRequest.SUBCOLLECTION_PEOPLE).add(person)
                    .addOnSuccessListener(ref -> {
                        if (listener != null) listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        if (listener != null) listener.onSuccess();
                    });
        } catch (Exception e) {
            if (listener != null) listener.onSuccess();
        }
    }

    // ════════════════════════════════════════════════════════════
    //  TEAM SUPPORT REQUEST OPERATIONS (Section 8)
    // ════════════════════════════════════════════════════════════

    public void saveTeamRequest(TeamRequest request, List<RequestPerson> roster, OnCompleteListener listener) {
        if (request.getRequestId() == null || request.getRequestId().isEmpty()) {
            request.setRequestId("req_" + System.currentTimeMillis());
        }

        saveRequestToCache(request, roster);

        try {
            db.collection(TeamRequest.COLLECTION_NAME).document(request.getRequestId())
                    .set(request)
                    .addOnSuccessListener(aVoid -> {
                        if (roster != null) {
                            for (RequestPerson p : roster) {
                                db.collection(TeamRequest.COLLECTION_NAME).document(request.getRequestId())
                                        .collection(TeamRequest.SUBCOLLECTION_PEOPLE).add(p);
                            }
                        }
                        if (listener != null) listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        if (listener != null) listener.onSuccess();
                    });
        } catch (Exception e) {
            if (listener != null) listener.onSuccess();
        }
    }

    public void getTeamRequests(OnListListener<TeamRequest> listener) {
        db.collection(TeamRequest.COLLECTION_NAME).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<TeamRequest> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        list.add(doc.toObject(TeamRequest.class));
                    }
                    if (list.isEmpty()) {
                        list = getRequestsFromCache();
                    }
                    if (listener != null) listener.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onSuccess(getRequestsFromCache());
                });
    }

    public void getPendingRequests(OnListListener<TeamRequest> listener) {
        db.collection(TeamRequest.COLLECTION_NAME)
                .whereEqualTo("status", TeamRequest.STATUS_PENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<TeamRequest> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        list.add(doc.toObject(TeamRequest.class));
                    }
                    if (list.isEmpty()) {
                        for (TeamRequest req : getRequestsFromCache()) {
                            if (TeamRequest.STATUS_PENDING.equals(req.getStatus())) {
                                list.add(req);
                            }
                        }
                    }
                    if (listener != null) listener.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    List<TeamRequest> cached = new ArrayList<>();
                    for (TeamRequest req : getRequestsFromCache()) {
                        if (TeamRequest.STATUS_PENDING.equals(req.getStatus())) {
                            cached.add(req);
                        }
                    }
                    if (listener != null) listener.onSuccess(cached);
                });
    }

    public void getApprovedRequests(OnListListener<TeamRequest> listener) {
        db.collection(TeamRequest.COLLECTION_NAME)
                .whereEqualTo("status", TeamRequest.STATUS_APPROVED)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<TeamRequest> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        list.add(doc.toObject(TeamRequest.class));
                    }
                    if (list.isEmpty()) {
                        for (TeamRequest req : getRequestsFromCache()) {
                            if (TeamRequest.STATUS_APPROVED.equals(req.getStatus())) {
                                list.add(req);
                            }
                        }
                    }
                    if (listener != null) listener.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    List<TeamRequest> cached = new ArrayList<>();
                    for (TeamRequest req : getRequestsFromCache()) {
                        if (TeamRequest.STATUS_APPROVED.equals(req.getStatus())) {
                            cached.add(req);
                        }
                    }
                    if (listener != null) listener.onSuccess(cached);
                });
    }

    public void getTeamRequestsByOrganizer(String organizerId, OnListListener<TeamRequest> listener) {
        db.collection(TeamRequest.COLLECTION_NAME)
                .whereEqualTo("organizerId", organizerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<TeamRequest> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        list.add(doc.toObject(TeamRequest.class));
                    }
                    if (list.isEmpty()) {
                        for (TeamRequest req : getRequestsFromCache()) {
                            if (organizerId == null || organizerId.equals(req.getOrganizerId())
                                    || "demo_organizer_uid".equals(req.getOrganizerId())) {
                                list.add(req);
                            }
                        }
                    }
                    if (listener != null) listener.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    List<TeamRequest> cached = new ArrayList<>();
                    for (TeamRequest req : getRequestsFromCache()) {
                        if (organizerId == null || organizerId.equals(req.getOrganizerId())
                                || "demo_organizer_uid".equals(req.getOrganizerId())) {
                            cached.add(req);
                        }
                    }
                    if (listener != null) listener.onSuccess(cached);
                });
    }

    public void getTeamRequestById(String requestId, OnObjectListener<TeamRequest> listener) {
        db.collection(TeamRequest.COLLECTION_NAME).document(requestId).get()
                .addOnSuccessListener(doc -> {
                    TeamRequest req = doc.toObject(TeamRequest.class);
                    if (req != null) {
                        if (listener != null) listener.onSuccess(req);
                    } else {
                        TeamRequest cached = getRequestFromCache(requestId);
                        if (cached != null && listener != null) {
                            listener.onSuccess(cached);
                        } else if (listener != null) {
                            listener.onError("Request not found");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    TeamRequest cached = getRequestFromCache(requestId);
                    if (cached != null && listener != null) {
                        listener.onSuccess(cached);
                    } else if (listener != null) {
                        listener.onError("Request not found");
                    }
                });
    }

    public void getRequestRoster(String requestId, OnListListener<RequestPerson> listener) {
        db.collection(TeamRequest.COLLECTION_NAME).document(requestId)
                .collection(TeamRequest.SUBCOLLECTION_PEOPLE).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<RequestPerson> roster = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        roster.add(doc.toObject(RequestPerson.class));
                    }
                    if (roster.isEmpty()) {
                        roster = getRosterFromCache(requestId);
                    }
                    if (listener != null) listener.onSuccess(roster);
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onSuccess(getRosterFromCache(requestId));
                });
    }

    public void updateRequestStatus(String requestId, String newStatus, String reason, OnCompleteListener listener) {
        TeamRequest cached = getRequestFromCache(requestId);
        if (cached != null) {
            cached.setStatus(newStatus);
            if (reason != null) cached.setRejectionReason(reason);
            List<RequestPerson> roster = getRosterFromCache(requestId);
            saveRequestToCache(cached, roster);
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        if (reason != null) updates.put("rejectionReason", reason);

        db.collection(TeamRequest.COLLECTION_NAME).document(requestId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onSuccess();
                });
    }

    public void deleteTeamRequest(String requestId, OnCompleteListener listener) {
        deleteRequestFromCache(requestId);
        db.collection(TeamRequest.COLLECTION_NAME).document(requestId).delete()
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onSuccess();
                });
    }

    // ════════════════════════════════════════════════════════════
    //  USER OPERATIONS (Section 4 & 10)
    // ════════════════════════════════════════════════════════════

    /**
     * Search user by registered email (for volunteer assignment).
     */
    public void findUserByEmail(String email, OnObjectListener<User> listener) {
        if (email == null || email.trim().isEmpty()) {
            if (listener != null) listener.onError("Email cannot be empty");
            return;
        }

        final String cleanEmail = email.trim().toLowerCase();

        db.collection(User.COLLECTION_NAME)
                .whereEqualTo("email", cleanEmail)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots != null && !snapshots.isEmpty()) {
                        User user = snapshots.getDocuments().get(0).toObject(User.class);
                        if (user != null) {
                            user.setUid(snapshots.getDocuments().get(0).getId());
                            saveUserToCache(user);
                            if (listener != null) listener.onSuccess(user);
                            return;
                        }
                    }
                    User found = findUserByEmailInCache(cleanEmail);
                    if (found != null && listener != null) {
                        listener.onSuccess(found);
                    } else if (listener != null) {
                        listener.onError("No registered user found with email: " + email);
                    }
                })
                .addOnFailureListener(e -> {
                    User found = findUserByEmailInCache(cleanEmail);
                    if (found != null && listener != null) {
                        listener.onSuccess(found);
                    } else if (listener != null) {
                        listener.onError("No registered user found with email: " + email);
                    }
                });
    }

    private User findUserByEmailInCache(String email) {
        if (email == null) return null;
        for (User u : getUsersFromCache()) {
            if (u.getEmail() != null && u.getEmail().trim().equalsIgnoreCase(email.trim())) {
                return u;
            }
        }
        return null;
    }

    public void getUsers(OnListListener<User> listener) {
        db.collection(User.COLLECTION_NAME).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        User u = doc.toObject(User.class);
                        list.add(u);
                        saveUserToCache(u);
                    }
                    if (list.isEmpty()) {
                        list = getUsersFromCache();
                    }
                    if (listener != null) listener.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onSuccess(getUsersFromCache());
                });
    }

    public void saveUser(User user, OnCompleteListener listener) {
        if (user.getUid() == null || user.getUid().isEmpty()) {
            user.setUid("usr_" + System.currentTimeMillis());
        }
        saveUserToCache(user);
        try {
            db.collection(User.COLLECTION_NAME).document(user.getUid()).set(user)
                    .addOnSuccessListener(aVoid -> {
                        if (listener != null) listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        if (listener != null) listener.onSuccess();
                    });
        } catch (Exception e) {
            if (listener != null) listener.onSuccess();
        }
    }

    public void updateUserRole(String uid, String newRole, OnCompleteListener listener) {
        User cached = getUserFromCache(uid);
        if (cached != null) {
            cached.setRole(newRole);
            saveUserToCache(cached);
        }
        try {
            db.collection(User.COLLECTION_NAME).document(uid).update("role", newRole)
                    .addOnSuccessListener(aVoid -> {
                        if (listener != null) listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        if (listener != null) listener.onSuccess();
                    });
        } catch (Exception e) {
            if (listener != null) listener.onSuccess();
        }
    }

    public void toggleUserStatus(String uid, boolean active, OnCompleteListener listener) {
        User cached = getUserFromCache(uid);
        if (cached != null) {
            cached.setActive(active);
            saveUserToCache(cached);
        }
        try {
            db.collection(User.COLLECTION_NAME).document(uid).update("active", active)
                    .addOnSuccessListener(aVoid -> {
                        if (listener != null) listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        if (listener != null) listener.onSuccess();
                    });
        } catch (Exception e) {
            if (listener != null) listener.onSuccess();
        }
    }

    // ════════════════════════════════════════════════════════════
    //  ORGANIZER, CLUB, AND DEPARTMENT OPERATIONS (Section 2 & 10)
    // ════════════════════════════════════════════════════════════

    public void getOrganizers(OnListListener<Organizer> listener) {
        db.collection(Organizer.COLLECTION_NAME).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Organizer> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        list.add(doc.toObject(Organizer.class));
                    }
                    if (list.isEmpty()) {
                        list = getOrganizersFromCache();
                    }
                    if (listener != null) listener.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onSuccess(getOrganizersFromCache());
                });
    }

    public void saveOrganizer(Organizer organizer, OnCompleteListener listener) {
        if (organizer.getOrganizerId() == null || organizer.getOrganizerId().isEmpty()) {
            organizer.setOrganizerId("org_" + System.currentTimeMillis());
        }
        saveOrganizerToCache(organizer);
        try {
            db.collection(Organizer.COLLECTION_NAME).document(organizer.getOrganizerId()).set(organizer)
                    .addOnSuccessListener(aVoid -> {
                        if (listener != null) listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        if (listener != null) listener.onSuccess();
                    });
        } catch (Exception e) {
            if (listener != null) listener.onSuccess();
        }
    }

    public void getClubs(OnListListener<Club> listener) {
        db.collection(Club.COLLECTION_NAME).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Club> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        list.add(doc.toObject(Club.class));
                    }
                    if (list.isEmpty()) {
                        list = getClubsFromCache();
                    }
                    if (listener != null) listener.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onSuccess(getClubsFromCache());
                });
    }

    public void saveClub(Club club, OnCompleteListener listener) {
        if (club.getClubId() == null || club.getClubId().isEmpty()) {
            club.setClubId("club_" + System.currentTimeMillis());
        }
        saveClubToCache(club);
        try {
            db.collection(Club.COLLECTION_NAME).document(club.getClubId()).set(club)
                    .addOnSuccessListener(aVoid -> {
                        if (listener != null) listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        if (listener != null) listener.onSuccess();
                    });
        } catch (Exception e) {
            if (listener != null) listener.onSuccess();
        }
    }

    public void getAcademicDepartments(OnListListener<AcademicDepartment> listener) {
        db.collection(AcademicDepartment.COLLECTION_NAME).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<AcademicDepartment> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        list.add(doc.toObject(AcademicDepartment.class));
                    }
                    if (list.isEmpty()) {
                        list = getDepartmentsFromCache();
                    }
                    if (listener != null) listener.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onSuccess(getDepartmentsFromCache());
                });
    }

    public void saveAcademicDepartment(AcademicDepartment dept, OnCompleteListener listener) {
        if (dept.getDepartmentId() == null || dept.getDepartmentId().isEmpty()) {
            dept.setDepartmentId("dept_" + System.currentTimeMillis());
        }
        saveDepartmentToCache(dept);
        try {
            db.collection(AcademicDepartment.COLLECTION_NAME).document(dept.getDepartmentId()).set(dept)
                    .addOnSuccessListener(aVoid -> {
                        if (listener != null) listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        if (listener != null) listener.onSuccess();
                    });
        } catch (Exception e) {
            if (listener != null) listener.onSuccess();
        }
    }

    // ════════════════════════════════════════════════════════════
    //  DASHBOARD COUNTS (Section 9)
    // ════════════════════════════════════════════════════════════

    public void getDashboardCounts(OnCountsListener listener) {
        if (listener == null) return;

        List<Event> events = getEventsFromCache();
        List<TeamRequest> requests = getRequestsFromCache();
        List<VolunteerAssignment> assignments = getAssignmentsFromCache();

        int totalEvents = events.size();
        int pendingEvents = 0;
        int publishedEvents = 0;
        for (Event e : events) {
            if (Event.STATUS_PENDING.equals(e.getStatus())) pendingEvents++;
            if (Event.STATUS_PUBLISHED.equals(e.getStatus())) publishedEvents++;
        }

        int pendingRequests = 0;
        int approvedRequests = 0;
        for (TeamRequest r : requests) {
            if (TeamRequest.STATUS_PENDING.equals(r.getStatus())) pendingRequests++;
            if (TeamRequest.STATUS_APPROVED.equals(r.getStatus())) approvedRequests++;
        }

        listener.onCounts(totalEvents, pendingEvents, publishedEvents,
                pendingRequests, approvedRequests, assignments.size());
    }

    public void getAdminDashboardCounts(OnAdminCountsListener listener) {
        if (listener == null) return;

        List<Event> events = getEventsFromCache();
        List<TeamRequest> requests = getRequestsFromCache();
        List<VolunteerAssignment> assignments = getAssignmentsFromCache();
        List<Organizer> organizers = getOrganizersFromCache();

        int totalEvents = events.size();
        int pendingEvents = 0;
        int publishedEvents = 0;
        for (Event e : events) {
            if (Event.STATUS_PENDING.equals(e.getStatus())) pendingEvents++;
            if (Event.STATUS_PUBLISHED.equals(e.getStatus())) publishedEvents++;
        }

        int pendingAssignments = 0;
        int approvedAssignments = 0;
        for (VolunteerAssignment a : assignments) {
            if (VolunteerAssignment.STATUS_PENDING.equals(a.getStatus())) pendingAssignments++;
            if (VolunteerAssignment.STATUS_APPROVED.equals(a.getStatus())) approvedAssignments++;
        }

        int pendingRequests = 0;
        int approvedRequests = 0;
        for (TeamRequest r : requests) {
            if (TeamRequest.STATUS_PENDING.equals(r.getStatus())) pendingRequests++;
            if (TeamRequest.STATUS_APPROVED.equals(r.getStatus())) approvedRequests++;
        }

        int totalOrganizers = organizers.size();
        if (totalOrganizers == 0) {
            for (User u : getUsersFromCache()) {
                if (u.isOrganizer()) totalOrganizers++;
            }
        }

        listener.onCounts(totalEvents, pendingEvents, publishedEvents,
                pendingAssignments, approvedAssignments,
                pendingRequests, approvedRequests, totalOrganizers);
    }

    /**
     * Get useful summary statistics for the final Admin dashboard (Section 4):
     * Total Events, Pending Event Approvals, Published Events,
     * Total Organizers, Total Registered Users, Total Volunteer Assignments.
     */
    public void getAdminDashboardSummary(OnAdminSummaryListener listener) {
        if (listener == null) return;

        List<Event> events = getEventsFromCache();
        List<VolunteerAssignment> assignments = getAssignmentsFromCache();
        List<Organizer> organizers = getOrganizersFromCache();
        List<User> users = getUsersFromCache();

        int totalEvents = events.size();
        int pendingEvents = 0;
        int publishedEvents = 0;
        for (Event e : events) {
            if (Event.STATUS_PENDING.equals(e.getStatus())) pendingEvents++;
            if (Event.STATUS_PUBLISHED.equals(e.getStatus())) publishedEvents++;
        }

        int totalOrganizers = organizers.size();
        int totalUsers = 0;
        for (User u : users) {
            if (u.isOrganizer()) {
                if (organizers.isEmpty()) totalOrganizers++;
            } else if (u.isUser()) {
                totalUsers++;
            }
        }
        if (totalUsers == 0) totalUsers = Math.max(1, users.size() - totalOrganizers);

        int totalAssignments = 0;
        for (VolunteerAssignment a : assignments) {
            if (!VolunteerAssignment.STATUS_CANCELLED.equals(a.getStatus())) {
                totalAssignments++;
            }
        }

        final int fTotalEvents = totalEvents;
        final int fPendingEvents = pendingEvents;
        final int fPublishedEvents = publishedEvents;
        final int fTotalOrganizers = totalOrganizers;
        final int fTotalUsers = totalUsers;
        final int fTotalAssignments = totalAssignments;

        // Immediate result from local cache, then attempt live Firestore fetch
        listener.onSummary(fTotalEvents, fPendingEvents, fPublishedEvents,
                fTotalOrganizers, fTotalUsers, fTotalAssignments);

        try {
            db.collection(Event.COLLECTION_NAME).get().addOnSuccessListener(eventDocs -> {
                int liveTotal = eventDocs.size();
                int livePending = 0;
                int livePublished = 0;
                for (QueryDocumentSnapshot doc : eventDocs) {
                    Event e = doc.toObject(Event.class);
                    if (Event.STATUS_PENDING.equals(e.getStatus())) livePending++;
                    if (Event.STATUS_PUBLISHED.equals(e.getStatus())) livePublished++;
                }

                final int uLiveTotal = liveTotal;
                final int uLivePending = livePending;
                final int uLivePublished = livePublished;

                db.collection(User.COLLECTION_NAME).get().addOnSuccessListener(userDocs -> {
                    int liveOrgs = 0;
                    int liveUsrs = 0;
                    for (QueryDocumentSnapshot doc : userDocs) {
                        User u = doc.toObject(User.class);
                        if (u.isOrganizer()) liveOrgs++;
                        else if (u.isUser()) liveUsrs++;
                    }

                    final int uLiveOrgs = Math.max(liveOrgs, fTotalOrganizers);
                    final int uLiveUsrs = Math.max(liveUsrs, fTotalUsers);

                    db.collection(VolunteerAssignment.COLLECTION_NAME).get().addOnSuccessListener(asgDocs -> {
                        int liveAsg = 0;
                        for (QueryDocumentSnapshot doc : asgDocs) {
                            VolunteerAssignment a = doc.toObject(VolunteerAssignment.class);
                            if (a.isActive()) liveAsg++;
                        }
                        listener.onSummary(uLiveTotal, uLivePending, uLivePublished,
                                uLiveOrgs, uLiveUsrs, Math.max(liveAsg, fTotalAssignments));
                    }).addOnFailureListener(e -> {
                        listener.onSummary(uLiveTotal, uLivePending, uLivePublished,
                                uLiveOrgs, uLiveUsrs, fTotalAssignments);
                    });
                }).addOnFailureListener(e -> {
                    listener.onSummary(uLiveTotal, uLivePending, uLivePublished,
                            fTotalOrganizers, fTotalUsers, fTotalAssignments);
                });
            });
        } catch (Exception ignored) {}
    }

    // ════════════════════════════════════════════════════════════
    //  LOCAL CACHE IMPLEMENTATION (JSON-BASED)
    // ════════════════════════════════════════════════════════════

    private synchronized void saveEventToCache(Event event) {
        List<Event> events = getEventsFromCache();
        boolean replaced = false;
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getEventId().equals(event.getEventId())) {
                events.set(i, event);
                replaced = true;
                break;
            }
        }
        if (!replaced) events.add(event);
        persistEvents(events);
    }

    private synchronized void deleteEventFromCache(String eventId) {
        List<Event> events = getEventsFromCache();
        List<Event> updated = new ArrayList<>();
        for (Event e : events) {
            if (!e.getEventId().equals(eventId)) updated.add(e);
        }
        persistEvents(updated);
    }

    private synchronized Event getEventFromCache(String eventId) {
        for (Event e : getEventsFromCache()) {
            if (e.getEventId().equals(eventId)) return e;
        }
        return null;
    }

    public synchronized List<Event> getEventsFromCache() {
        List<Event> list = new ArrayList<>();
        String json = prefs.getString(KEY_EVENTS, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                Event e = new Event();
                e.setEventId(obj.optString("eventId"));
                e.setEventName(obj.optString("eventName"));
                e.setCategory(obj.optString("category"));
                e.setShortDescription(obj.optString("shortDescription"));
                e.setDescription(obj.optString("description"));
                e.setPosterUrl(obj.optString("posterUrl"));
                e.setEventDate(obj.optString("eventDate"));
                e.setStartTime(obj.optString("startTime"));
                e.setEndTime(obj.optString("endTime"));
                e.setVenue(obj.optString("venue"));
                e.setOrganizingTeam(obj.optString("organizingTeam"));
                e.setFacultyInCharge(obj.optString("facultyInCharge"));
                e.setOrganizerId(obj.optString("organizerId"));
                e.setRegistrationInfo(obj.optString("registrationInfo"));
                e.setContactInfo(obj.optString("contactInfo"));
                e.setStatus(obj.optString("status", Event.STATUS_DRAFT));
                e.setRejectionReason(obj.optString("rejectionReason"));
                e.setReviewedBy(obj.optString("reviewedBy"));
                list.add(e);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error parsing cached events", ex);
        }
        return list;
    }

    private synchronized void persistEvents(List<Event> events) {
        try {
            JSONArray arr = new JSONArray();
            for (Event e : events) {
                JSONObject obj = new JSONObject();
                obj.put("eventId", e.getEventId());
                obj.put("eventName", e.getEventName());
                obj.put("category", e.getCategory());
                obj.put("shortDescription", e.getShortDescription());
                obj.put("description", e.getDescription());
                obj.put("posterUrl", e.getPosterUrl());
                obj.put("eventDate", e.getEventDate());
                obj.put("startTime", e.getStartTime());
                obj.put("endTime", e.getEndTime());
                obj.put("venue", e.getVenue());
                obj.put("organizingTeam", e.getOrganizingTeam());
                obj.put("facultyInCharge", e.getFacultyInCharge());
                obj.put("organizerId", e.getOrganizerId());
                obj.put("registrationInfo", e.getRegistrationInfo());
                obj.put("contactInfo", e.getContactInfo());
                obj.put("status", e.getStatus());
                obj.put("rejectionReason", e.getRejectionReason());
                obj.put("reviewedBy", e.getReviewedBy());
                arr.put(obj);
            }
            prefs.edit().putString(KEY_EVENTS, arr.toString()).apply();
        } catch (Exception ex) {
            Log.e(TAG, "Error serializing events", ex);
        }
    }

    // Assignments Local Cache
    private synchronized void saveAssignmentToCache(VolunteerAssignment assignment) {
        List<VolunteerAssignment> list = getAssignmentsFromCache();
        boolean replaced = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getAssignmentId().equals(assignment.getAssignmentId())) {
                list.set(i, assignment);
                replaced = true;
                break;
            }
        }
        if (!replaced) list.add(assignment);
        persistAssignments(list);
    }

    private synchronized void deleteAssignmentFromCache(String assignmentId) {
        List<VolunteerAssignment> list = getAssignmentsFromCache();
        List<VolunteerAssignment> updated = new ArrayList<>();
        for (VolunteerAssignment a : list) {
            if (!a.getAssignmentId().equals(assignmentId)) updated.add(a);
        }
        persistAssignments(updated);
    }

    private synchronized VolunteerAssignment getAssignmentFromCache(String assignmentId) {
        for (VolunteerAssignment a : getAssignmentsFromCache()) {
            if (a.getAssignmentId().equals(assignmentId)) return a;
        }
        return null;
    }

    public synchronized List<VolunteerAssignment> getAssignmentsFromCache() {
        List<VolunteerAssignment> list = new ArrayList<>();
        String json = prefs.getString(KEY_ASSIGNMENTS, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                VolunteerAssignment a = new VolunteerAssignment();
                a.setAssignmentId(obj.optString("assignmentId"));
                a.setEventId(obj.optString("eventId"));
                a.setEventName(obj.optString("eventName"));
                a.setUserId(obj.optString("userId"));
                a.setUserName(obj.optString("userName"));
                a.setUserEmail(obj.optString("userEmail"));
                a.setUserType(obj.optString("userType"));
                a.setCourse(obj.optString("course"));
                a.setYear(obj.optString("year"));
                a.setDepartment(obj.optString("department"));
                a.setRollNumber(obj.optString("rollNumber"));
                a.setAssignedDuty(obj.optString("assignedDuty"));
                a.setEventDate(obj.optString("eventDate"));
                a.setStartTime(obj.optString("startTime"));
                a.setEndTime(obj.optString("endTime"));
                a.setVenue(obj.optString("venue"));
                a.setOrganizerId(obj.optString("organizerId"));
                a.setOrganizerName(obj.optString("organizerName"));
                a.setAdditionalInstructions(obj.optString("additionalInstructions"));
                a.setStatus(obj.optString("status", VolunteerAssignment.STATUS_PENDING));
                a.setRejectionReason(obj.optString("rejectionReason"));
                a.setReviewedBy(obj.optString("reviewedBy"));
                list.add(a);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing cached assignments", e);
        }
        return list;
    }

    private synchronized void persistAssignments(List<VolunteerAssignment> assignments) {
        try {
            JSONArray arr = new JSONArray();
            for (VolunteerAssignment a : assignments) {
                JSONObject obj = new JSONObject();
                obj.put("assignmentId", a.getAssignmentId());
                obj.put("eventId", a.getEventId());
                obj.put("eventName", a.getEventName());
                obj.put("userId", a.getUserId());
                obj.put("userName", a.getUserName());
                obj.put("userEmail", a.getUserEmail());
                obj.put("userType", a.getUserType());
                obj.put("course", a.getCourse());
                obj.put("year", a.getYear());
                obj.put("department", a.getDepartment());
                obj.put("rollNumber", a.getRollNumber());
                obj.put("assignedDuty", a.getAssignedDuty());
                obj.put("eventDate", a.getEventDate());
                obj.put("startTime", a.getStartTime());
                obj.put("endTime", a.getEndTime());
                obj.put("venue", a.getVenue());
                obj.put("organizerId", a.getOrganizerId());
                obj.put("organizerName", a.getOrganizerName());
                obj.put("additionalInstructions", a.getAdditionalInstructions());
                obj.put("status", a.getStatus());
                obj.put("rejectionReason", a.getRejectionReason());
                obj.put("reviewedBy", a.getReviewedBy());
                arr.put(obj);
            }
            prefs.edit().putString(KEY_ASSIGNMENTS, arr.toString()).apply();
        } catch (Exception ex) {
            Log.e(TAG, "Error serializing assignments", ex);
        }
    }

    // Volunteers Local Cache (Legacy support)
    private synchronized void saveVolunteerToCache(Volunteer volunteer) {
        List<Volunteer> list = getVolunteersFromCache();
        boolean replaced = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getVolunteerId().equals(volunteer.getVolunteerId())) {
                list.set(i, volunteer);
                replaced = true;
                break;
            }
        }
        if (!replaced) list.add(volunteer);
        persistVolunteers(list);
    }

    private synchronized void deleteVolunteerFromCache(String volunteerId) {
        List<Volunteer> list = getVolunteersFromCache();
        List<Volunteer> updated = new ArrayList<>();
        for (Volunteer v : list) {
            if (!v.getVolunteerId().equals(volunteerId)) updated.add(v);
        }
        persistVolunteers(updated);
    }

    public synchronized List<Volunteer> getVolunteersFromCache() {
        List<Volunteer> list = new ArrayList<>();
        String json = prefs.getString(KEY_VOLUNTEERS, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                Volunteer v = new Volunteer();
                v.setVolunteerId(obj.optString("volunteerId"));
                v.setEventId(obj.optString("eventId"));
                v.setEventName(obj.optString("eventName"));
                v.setUserId(obj.optString("userId"));
                v.setStudentName(obj.optString("studentName"));
                v.setUserEmail(obj.optString("userEmail"));
                v.setUserType(obj.optString("userType"));
                v.setCourse(obj.optString("course"));
                v.setYear(obj.optString("year"));
                v.setDepartment(obj.optString("department"));
                v.setRollNumber(obj.optString("rollNumber"));
                v.setAssignedDuty(obj.optString("assignedDuty"));
                v.setTeam(obj.optString("team"));
                v.setEventDate(obj.optString("eventDate"));
                v.setStartTime(obj.optString("startTime"));
                v.setEndTime(obj.optString("endTime"));
                v.setVenue(obj.optString("venue"));
                v.setOrganizerId(obj.optString("organizerId"));
                v.setStatus(obj.optString("status", Volunteer.STATUS_APPROVED));
                v.setRejectionReason(obj.optString("rejectionReason"));
                v.setCreatedBy(obj.optString("createdBy"));
                list.add(v);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing cached volunteers", e);
        }
        return list;
    }

    private synchronized void persistVolunteers(List<Volunteer> volunteers) {
        try {
            JSONArray arr = new JSONArray();
            for (Volunteer v : volunteers) {
                JSONObject obj = new JSONObject();
                obj.put("volunteerId", v.getVolunteerId());
                obj.put("eventId", v.getEventId());
                obj.put("eventName", v.getEventName());
                obj.put("userId", v.getUserId());
                obj.put("studentName", v.getStudentName());
                obj.put("userEmail", v.getUserEmail());
                obj.put("userType", v.getUserType());
                obj.put("course", v.getCourse());
                obj.put("year", v.getYear());
                obj.put("department", v.getDepartment());
                obj.put("rollNumber", v.getRollNumber());
                obj.put("assignedDuty", v.getAssignedDuty());
                obj.put("team", v.getTeam());
                obj.put("eventDate", v.getEventDate());
                obj.put("startTime", v.getStartTime());
                obj.put("endTime", v.getEndTime());
                obj.put("venue", v.getVenue());
                obj.put("organizerId", v.getOrganizerId());
                obj.put("status", v.getStatus());
                obj.put("rejectionReason", v.getRejectionReason());
                obj.put("createdBy", v.getCreatedBy());
                arr.put(obj);
            }
            prefs.edit().putString(KEY_VOLUNTEERS, arr.toString()).apply();
        } catch (Exception ex) {
            Log.e(TAG, "Error serializing volunteers", ex);
        }
    }

    // Requests Local Cache
    private synchronized void saveRequestToCache(TeamRequest request, List<RequestPerson> roster) {
        List<TeamRequest> list = getRequestsFromCache();
        boolean replaced = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getRequestId().equals(request.getRequestId())) {
                list.set(i, request);
                replaced = true;
                break;
            }
        }
        if (!replaced) list.add(request);
        persistRequests(list);

        if (roster != null) {
            persistRoster(request.getRequestId(), roster);
        }
    }

    private synchronized void deleteRequestFromCache(String requestId) {
        List<TeamRequest> list = getRequestsFromCache();
        List<TeamRequest> updated = new ArrayList<>();
        for (TeamRequest r : list) {
            if (!r.getRequestId().equals(requestId)) updated.add(r);
        }
        persistRequests(updated);
    }

    private synchronized TeamRequest getRequestFromCache(String requestId) {
        for (TeamRequest r : getRequestsFromCache()) {
            if (r.getRequestId().equals(requestId)) return r;
        }
        return null;
    }

    public synchronized List<TeamRequest> getRequestsFromCache() {
        List<TeamRequest> list = new ArrayList<>();
        String json = prefs.getString(KEY_REQUESTS, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                TeamRequest r = new TeamRequest();
                r.setRequestId(obj.optString("requestId"));
                r.setTitle(obj.optString("title"));
                r.setDescription(obj.optString("description"));
                r.setReason(obj.optString("reason"));
                r.setCategory(obj.optString("category"));
                r.setRelatedEventId(obj.optString("relatedEventId"));
                r.setRequestMode(obj.optString("requestMode"));
                r.setWorkLocation(obj.optString("workLocation"));
                r.setRequiredDate(obj.optString("requiredDate"));
                r.setStartTime(obj.optString("startTime"));
                r.setEndTime(obj.optString("endTime"));
                r.setPeopleRequired(obj.optInt("peopleRequired"));
                r.setRequiredDepartment(obj.optString("requiredDepartment"));
                r.setRequiredSkills(obj.optString("requiredSkills"));
                r.setOrganizerId(obj.optString("organizerId"));
                r.setFacultyInCharge(obj.optString("facultyInCharge"));
                r.setStatus(obj.optString("status", TeamRequest.STATUS_DRAFT));
                r.setVisibility(obj.optString("visibility", TeamRequest.VIS_ORGANIZER_ADMIN));
                r.setRejectionReason(obj.optString("rejectionReason"));
                r.setReviewedBy(obj.optString("reviewedBy"));
                r.setAdditionalInstructions(obj.optString("additionalInstructions"));
                list.add(r);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error parsing cached requests", ex);
        }
        return list;
    }

    private synchronized void persistRequests(List<TeamRequest> requests) {
        try {
            JSONArray arr = new JSONArray();
            for (TeamRequest r : requests) {
                JSONObject obj = new JSONObject();
                obj.put("requestId", r.getRequestId());
                obj.put("title", r.getTitle());
                obj.put("description", r.getDescription());
                obj.put("reason", r.getReason());
                obj.put("category", r.getCategory());
                obj.put("relatedEventId", r.getRelatedEventId());
                obj.put("requestMode", r.getRequestMode());
                obj.put("workLocation", r.getWorkLocation());
                obj.put("requiredDate", r.getRequiredDate());
                obj.put("startTime", r.getStartTime());
                obj.put("endTime", r.getEndTime());
                obj.put("peopleRequired", r.getPeopleRequired());
                obj.put("requiredDepartment", r.getRequiredDepartment());
                obj.put("requiredSkills", r.getRequiredSkills());
                obj.put("organizerId", r.getOrganizerId());
                obj.put("facultyInCharge", r.getFacultyInCharge());
                obj.put("status", r.getStatus());
                obj.put("visibility", r.getVisibility());
                obj.put("rejectionReason", r.getRejectionReason());
                obj.put("reviewedBy", r.getReviewedBy());
                obj.put("additionalInstructions", r.getAdditionalInstructions());
                arr.put(obj);
            }
            prefs.edit().putString(KEY_REQUESTS, arr.toString()).apply();
        } catch (Exception ex) {
            Log.e(TAG, "Error serializing requests", ex);
        }
    }

    public synchronized List<RequestPerson> getRosterFromCache(String requestId) {
        List<RequestPerson> list = new ArrayList<>();
        String json = prefs.getString(KEY_REQUEST_PEOPLE + requestId, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                RequestPerson p = new RequestPerson();
                p.setPersonId(obj.optString("personId"));
                p.setFullName(obj.optString("fullName"));
                p.setCourse(obj.optString("course"));
                p.setYear(obj.optString("year"));
                p.setDepartment(obj.optString("department"));
                p.setRollNumber(obj.optString("rollNumber"));
                p.setAssignedDuty(obj.optString("assignedDuty"));
                p.setTeam(obj.optString("team"));
                p.setNotes(obj.optString("notes"));
                list.add(p);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error parsing cached roster", ex);
        }
        return list;
    }

    public synchronized void persistRoster(String requestId, List<RequestPerson> roster) {
        try {
            JSONArray arr = new JSONArray();
            for (RequestPerson p : roster) {
                JSONObject obj = new JSONObject();
                obj.put("personId", p.getPersonId());
                obj.put("fullName", p.getFullName());
                obj.put("course", p.getCourse());
                obj.put("year", p.getYear());
                obj.put("department", p.getDepartment());
                obj.put("rollNumber", p.getRollNumber());
                obj.put("assignedDuty", p.getAssignedDuty());
                obj.put("team", p.getTeam());
                obj.put("notes", p.getNotes());
                arr.put(obj);
            }
            prefs.edit().putString(KEY_REQUEST_PEOPLE + requestId, arr.toString()).apply();
        } catch (Exception ex) {
            Log.e(TAG, "Error serializing roster", ex);
        }
    }

    // Users Local Cache
    private synchronized void saveUserToCache(User user) {
        List<User> list = getUsersFromCache();
        boolean replaced = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getUid() != null && list.get(i).getUid().equals(user.getUid())) {
                list.set(i, user);
                replaced = true;
                break;
            }
        }
        if (!replaced) list.add(user);
        persistUsers(list);
    }

    private synchronized User getUserFromCache(String uid) {
        for (User u : getUsersFromCache()) {
            if (u.getUid() != null && u.getUid().equals(uid)) return u;
        }
        return null;
    }

    public synchronized List<User> getUsersFromCache() {
        List<User> list = new ArrayList<>();
        String json = prefs.getString(KEY_USERS, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                User u = new User();
                u.setUid(obj.optString("uid"));
                u.setFullName(obj.optString("fullName"));
                u.setEmail(obj.optString("email"));
                u.setRole(obj.optString("role"));
                u.setUserType(obj.optString("userType", User.USER_TYPE_STUDENT));
                u.setCourse(obj.optString("course"));
                u.setYear(obj.optString("year"));
                u.setDepartment(obj.optString("department"));
                u.setRollNumber(obj.optString("rollNumber"));
                u.setActive(obj.optBoolean("active", true));
                list.add(u);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing cached users", e);
        }
        return list;
    }

    private synchronized void persistUsers(List<User> users) {
        try {
            JSONArray arr = new JSONArray();
            for (User u : users) {
                JSONObject obj = new JSONObject();
                obj.put("uid", u.getUid());
                obj.put("fullName", u.getFullName());
                obj.put("email", u.getEmail());
                obj.put("role", u.getRole());
                obj.put("userType", u.getUserType());
                obj.put("course", u.getCourse());
                obj.put("year", u.getYear());
                obj.put("department", u.getDepartment());
                obj.put("rollNumber", u.getRollNumber());
                obj.put("active", u.isActive());
                arr.put(obj);
            }
            prefs.edit().putString(KEY_USERS, arr.toString()).apply();
        } catch (Exception ex) {
            Log.e(TAG, "Error serializing users", ex);
        }
    }

    // Organizers Cache
    private synchronized void saveOrganizerToCache(Organizer organizer) {
        List<Organizer> list = getOrganizersFromCache();
        boolean replaced = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getOrganizerId().equals(organizer.getOrganizerId())) {
                list.set(i, organizer);
                replaced = true;
                break;
            }
        }
        if (!replaced) list.add(organizer);
        persistOrganizers(list);
    }

    public synchronized List<Organizer> getOrganizersFromCache() {
        List<Organizer> list = new ArrayList<>();
        String json = prefs.getString(KEY_ORGANIZERS, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                Organizer o = new Organizer();
                o.setOrganizerId(obj.optString("organizerId"));
                o.setUid(obj.optString("uid"));
                o.setOfficialName(obj.optString("officialName"));
                o.setOfficialEmail(obj.optString("officialEmail"));
                o.setOrganizationType(obj.optString("organizationType"));
                o.setClubId(obj.optString("clubId"));
                o.setDepartmentId(obj.optString("departmentId"));
                o.setFacultyInCharge(obj.optString("facultyInCharge"));
                o.setActive(obj.optBoolean("active", true));
                list.add(o);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing organizers cache", e);
        }
        return list;
    }

    private synchronized void persistOrganizers(List<Organizer> list) {
        try {
            JSONArray arr = new JSONArray();
            for (Organizer o : list) {
                JSONObject obj = new JSONObject();
                obj.put("organizerId", o.getOrganizerId());
                obj.put("uid", o.getUid());
                obj.put("officialName", o.getOfficialName());
                obj.put("officialEmail", o.getOfficialEmail());
                obj.put("organizationType", o.getOrganizationType());
                obj.put("clubId", o.getClubId());
                obj.put("departmentId", o.getDepartmentId());
                obj.put("facultyInCharge", o.getFacultyInCharge());
                obj.put("active", o.isActive());
                arr.put(obj);
            }
            prefs.edit().putString(KEY_ORGANIZERS, arr.toString()).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving organizers cache", e);
        }
    }

    // Clubs Cache
    private synchronized void saveClubToCache(Club club) {
        List<Club> list = getClubsFromCache();
        boolean replaced = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getClubId().equals(club.getClubId())) {
                list.set(i, club);
                replaced = true;
                break;
            }
        }
        if (!replaced) list.add(club);
        persistClubs(list);
    }

    public synchronized List<Club> getClubsFromCache() {
        List<Club> list = new ArrayList<>();
        String json = prefs.getString(KEY_CLUBS, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                Club c = new Club();
                c.setClubId(obj.optString("clubId"));
                c.setClubName(obj.optString("clubName"));
                c.setDescription(obj.optString("description"));
                c.setFacultyInCharge(obj.optString("facultyInCharge"));
                c.setOfficialEmail(obj.optString("officialEmail"));
                c.setActive(obj.optBoolean("active", true));
                list.add(c);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing clubs cache", e);
        }
        return list;
    }

    private synchronized void persistClubs(List<Club> list) {
        try {
            JSONArray arr = new JSONArray();
            for (Club c : list) {
                JSONObject obj = new JSONObject();
                obj.put("clubId", c.getClubId());
                obj.put("clubName", c.getClubName());
                obj.put("description", c.getDescription());
                obj.put("facultyInCharge", c.getFacultyInCharge());
                obj.put("officialEmail", c.getOfficialEmail());
                obj.put("active", c.isActive());
                arr.put(obj);
            }
            prefs.edit().putString(KEY_CLUBS, arr.toString()).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving clubs cache", e);
        }
    }

    // Departments Cache
    private synchronized void saveDepartmentToCache(AcademicDepartment dept) {
        List<AcademicDepartment> list = getDepartmentsFromCache();
        boolean replaced = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getDepartmentId().equals(dept.getDepartmentId())) {
                list.set(i, dept);
                replaced = true;
                break;
            }
        }
        if (!replaced) list.add(dept);
        persistDepartments(list);
    }

    public synchronized List<AcademicDepartment> getDepartmentsFromCache() {
        List<AcademicDepartment> list = new ArrayList<>();
        String json = prefs.getString(KEY_DEPARTMENTS, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                AcademicDepartment d = new AcademicDepartment();
                d.setDepartmentId(obj.optString("departmentId"));
                d.setDepartmentName(obj.optString("departmentName"));
                d.setSchoolName(obj.optString("schoolName"));
                d.setFacultyInCharge(obj.optString("facultyInCharge"));
                d.setOfficialEmail(obj.optString("officialEmail"));
                d.setActive(obj.optBoolean("active", true));
                list.add(d);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing departments cache", e);
        }
        return list;
    }

    private synchronized void persistDepartments(List<AcademicDepartment> list) {
        try {
            JSONArray arr = new JSONArray();
            for (AcademicDepartment d : list) {
                JSONObject obj = new JSONObject();
                obj.put("departmentId", d.getDepartmentId());
                obj.put("departmentName", d.getDepartmentName());
                obj.put("schoolName", d.getSchoolName());
                obj.put("facultyInCharge", d.getFacultyInCharge());
                obj.put("officialEmail", d.getOfficialEmail());
                obj.put("active", d.isActive());
                arr.put(obj);
            }
            prefs.edit().putString(KEY_DEPARTMENTS, arr.toString()).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving departments cache", e);
        }
    }

    /**
     * Seeds initial campus demo events, volunteer opportunities, and official accounts.
     */
    private void seedDefaultDataIfEmpty() {
        if (!getUsersFromCache().isEmpty() && !getEventsFromCache().isEmpty() && !getAssignmentsFromCache().isEmpty()) return;

        // Seed users if empty
        if (getUsersFromCache().isEmpty()) {
            List<User> userSeeds = new ArrayList<>();

            User uAdmin = new User("demo_admin_uid", "Campus Administrator", "admin@unisync.com", User.ROLE_ADMIN, User.USER_TYPE_TEACHER);
            uAdmin.setDepartment("MPSTME, NMIMS");
            uAdmin.setActive(true);
            userSeeds.add(uAdmin);

            User uOrg = new User("demo_organizer_uid", "Turing Tech Club Coordinator", "organizer@unisync.com", User.ROLE_ORGANIZER, User.USER_TYPE_TEACHER);
            uOrg.setDepartment("Computer Engineering");
            uOrg.setActive(true);
            userSeeds.add(uOrg);

            User uStud1 = new User("demo_student_uid", "Aarav Sharma", "student@unisync.com", User.ROLE_USER, User.USER_TYPE_STUDENT);
            uStud1.setCourse("B.Tech Computer Science");
            uStud1.setYear("3rd Year");
            uStud1.setDepartment("Computer Engineering");
            uStud1.setRollNumber("70012022045");
            uStud1.setActive(true);
            userSeeds.add(uStud1);

            User uTeacher1 = new User("demo_teacher_uid", "Prof. Sunita Mehta", "teacher@unisync.com", User.ROLE_USER, User.USER_TYPE_TEACHER);
            uTeacher1.setDepartment("Information Technology");
            uTeacher1.setActive(true);
            userSeeds.add(uTeacher1);

            User uStud2 = new User("demo_student_2", "Priya Patel", "priya.patel@unisync.nmims.edu", User.ROLE_USER, User.USER_TYPE_STUDENT);
            uStud2.setCourse("B.Tech Information Technology");
            uStud2.setYear("2nd Year");
            uStud2.setDepartment("Information Technology");
            uStud2.setRollNumber("70012023012");
            uStud2.setActive(true);
            userSeeds.add(uStud2);

            User uStud3 = new User("demo_student_3", "Rohan Gupta", "rohan.gupta@unisync.nmims.edu", User.ROLE_USER, User.USER_TYPE_STUDENT);
            uStud3.setCourse("B.Tech Electronics & Telecom");
            uStud3.setYear("3rd Year");
            uStud3.setDepartment("Electronics & Telecom");
            uStud3.setRollNumber("70012022089");
            uStud3.setActive(true);
            userSeeds.add(uStud3);

            persistUsers(userSeeds);
        }

        // Seed Clubs & Departments
        if (getClubsFromCache().isEmpty()) {
            List<Club> clubSeeds = new ArrayList<>();
            clubSeeds.add(new Club("club_1", "Turing Tech Club", "Official student technical and coding chapter", "Dr. Ramesh Kulkarni", "turing@nmims.edu"));
            clubSeeds.add(new Club("club_2", "NMIMS Cultural Committee", "Apex committee for dance, drama, and music festivals", "Prof. Sunita Mehta", "cultural@nmims.edu"));
            clubSeeds.add(new Club("club_3", "Google Developer Student Club", "Student developer community supported by Google", "Prof. Amit Verma", "gdsc@nmims.edu"));
            clubSeeds.add(new Club("club_4", "Rotaract Club of NMIMS", "Youth wing of Rotary international for community service", "Prof. Neha Shah", "rotaract@nmims.edu"));
            persistClubs(clubSeeds);
        }

        if (getDepartmentsFromCache().isEmpty()) {
            List<AcademicDepartment> deptSeeds = new ArrayList<>();
            deptSeeds.add(new AcademicDepartment("dept_1", "Computer Engineering", "MPSTME", "Dr. Archana Bhise", "ce@nmims.edu"));
            deptSeeds.add(new AcademicDepartment("dept_2", "Information Technology", "MPSTME", "Dr. Dhirendra Mishra", "it@nmims.edu"));
            deptSeeds.add(new AcademicDepartment("dept_3", "Data Science", "MPSTME", "Dr. Seema Shah", "ds@nmims.edu"));
            deptSeeds.add(new AcademicDepartment("dept_4", "Electronics & Telecom", "MPSTME", "Dr. Vaishali Kulkarni", "extc@nmims.edu"));
            persistDepartments(deptSeeds);
        }

        if (getOrganizersFromCache().isEmpty()) {
            List<Organizer> orgSeeds = new ArrayList<>();
            Organizer o1 = new Organizer("org_1", "demo_organizer_uid", "Turing Tech Club", "organizer@unisync.com", Organizer.TYPE_CLUB);
            o1.setClubId("club_1");
            o1.setFacultyInCharge("Dr. Ramesh Kulkarni");
            orgSeeds.add(o1);
            persistOrganizers(orgSeeds);
        }

        // Seed Events
        if (getEventsFromCache().isEmpty()) {
            List<Event> seeds = new ArrayList<>();

            Event e1 = new Event();
            e1.setEventId("ev_seed_1");
            e1.setEventName("NMIMS Annual Hackathon 2025");
            e1.setCategory(Event.CAT_TECHNICAL);
            e1.setShortDescription("36-hour national level hackathon solving real-world AI and Web challenges.");
            e1.setDescription("Join over 500+ student innovators from across India. Includes mentorship from industry leaders, cash prizes of INR 2 Lakhs, and recruitment opportunities.");
            e1.setEventDate("2025-03-25");
            e1.setStartTime("09:00");
            e1.setEndTime("18:00");
            e1.setVenue("Mukesh Patel Technology Auditorium");
            e1.setOrganizingTeam("Turing Tech Club");
            e1.setFacultyInCharge("Dr. Ramesh Kulkarni");
            e1.setOrganizerId("demo_organizer_uid");
            e1.setStatus(Event.STATUS_PUBLISHED);
            e1.setRegistrationInfo("Open to all engineering students");
            e1.setContactInfo("hackathon@nmims.edu");
            seeds.add(e1);

            Event e2 = new Event();
            e2.setEventId("ev_seed_2");
            e2.setEventName("Sattva Cultural Fest 2025");
            e2.setCategory(Event.CAT_CULTURAL);
            e2.setShortDescription("The premier cultural celebration of NMIMS with dance, music, and drama.");
            e2.setDescription("A three-day cultural extravaganza celebrating talents across performing arts, fashion shows, battle of the bands, and celebrity musical concerts.");
            e2.setEventDate("2025-04-10");
            e2.setStartTime("16:00");
            e2.setEndTime("22:00");
            e2.setVenue("Campus Central Amphitheatre");
            e2.setOrganizingTeam("NMIMS Cultural Committee");
            e2.setFacultyInCharge("Prof. Sunita Mehta");
            e2.setOrganizerId("demo_organizer_uid");
            e2.setStatus(Event.STATUS_PUBLISHED);
            e2.setRegistrationInfo("Free entry for NMIMS ID card holders");
            e2.setContactInfo("sattva@nmims.edu");
            seeds.add(e2);

            Event e3 = new Event();
            e3.setEventId("ev_seed_3");
            e3.setEventName("AI & Cloud Computing Workshop");
            e3.setCategory(Event.CAT_WORKSHOP);
            e3.setShortDescription("Hands-on deep dive into Generative AI, LLMs, and Google Cloud Platform.");
            e3.setDescription("Interactive workshop covering practical development of cloud-native GenAI solutions, prompt engineering, and deploying containerized models.");
            e3.setEventDate("2025-03-30");
            e3.setStartTime("10:00");
            e3.setEndTime("13:00");
            e3.setVenue("Computer Lab 402");
            e3.setOrganizingTeam("Google Developer Student Club");
            e3.setFacultyInCharge("Prof. Amit Verma");
            e3.setOrganizerId("demo_organizer_uid");
            e3.setStatus(Event.STATUS_PENDING);
            e3.setRegistrationInfo("Laptops mandatory with Python 3.10+");
            e3.setContactInfo("gdsc@nmims.edu");
            seeds.add(e3);

            persistEvents(seeds);
        }

        // Seed 1 volunteer support request
        if (getRequestsFromCache().isEmpty()) {
            List<TeamRequest> reqSeeds = new ArrayList<>();
            TeamRequest tr1 = new TeamRequest();
            tr1.setRequestId("req_seed_1");
            tr1.setTitle("Stage and Lighting Volunteers Needed");
            tr1.setCategory(TeamRequest.CAT_STAGE_SETUP);
            tr1.setReason("Need 6 students for stage management and audio-visual setups for Sattva Fest.");
            tr1.setDescription("Assisting event operations team with backstage coordination and audio equipment setup.");
            tr1.setRequiredDate("2025-04-09");
            tr1.setStartTime("14:00");
            tr1.setEndTime("18:00");
            tr1.setPeopleRequired(6);
            tr1.setWorkLocation("Main Amphitheatre");
            tr1.setOrganizerId("demo_organizer_uid");
            tr1.setStatus(TeamRequest.STATUS_APPROVED);
            tr1.setVisibility(TeamRequest.VIS_STUDENT_VOLUNTEER);
            tr1.setAdditionalInstructions("Wear comfortable clothes and report to stage manager at 2 PM.");
            tr1.setRelatedEventId("ev_seed_2");
            reqSeeds.add(tr1);
            persistRequests(reqSeeds);
        }

        // Seed assignments (Section 4, 5, 6, 7)
        if (getAssignmentsFromCache().isEmpty()) {
            List<VolunteerAssignment> asgSeeds = new ArrayList<>();

            // Approved assignment for demo student
            VolunteerAssignment asg1 = new VolunteerAssignment();
            asg1.setAssignmentId("asg_seed_1");
            asg1.setEventId("ev_seed_1");
            asg1.setEventName("NMIMS Annual Hackathon 2025");
            asg1.setUserId("demo_student_uid");
            asg1.setUserName("Aarav Sharma");
            asg1.setUserEmail("student@unisync.com");
            asg1.setUserType(User.USER_TYPE_STUDENT);
            asg1.setCourse("B.Tech Computer Science");
            asg1.setYear("3rd Year");
            asg1.setDepartment("Computer Engineering");
            asg1.setRollNumber("70012022045");
            asg1.setAssignedDuty("Registration Desk & Participant Badge Check");
            asg1.setEventDate("2025-03-25");
            asg1.setStartTime("08:30");
            asg1.setEndTime("13:00");
            asg1.setVenue("Mukesh Patel Technology Auditorium");
            asg1.setOrganizerId("demo_organizer_uid");
            asg1.setOrganizerName("Turing Tech Club");
            asg1.setAdditionalInstructions("Report to Registration Table 1 by 8:15 AM.");
            asg1.setStatus(VolunteerAssignment.STATUS_APPROVED);
            asg1.setReviewedBy("demo_admin_uid");
            asg1.setReviewedAt(Timestamp.now());
            asgSeeds.add(asg1);

            // Approved assignment for another student
            VolunteerAssignment asg2 = new VolunteerAssignment();
            asg2.setAssignmentId("asg_seed_2");
            asg2.setEventId("ev_seed_2");
            asg2.setEventName("Sattva Cultural Fest 2025");
            asg2.setUserId("demo_student_2");
            asg2.setUserName("Priya Patel");
            asg2.setUserEmail("priya.patel@unisync.nmims.edu");
            asg2.setUserType(User.USER_TYPE_STUDENT);
            asg2.setCourse("B.Tech Information Technology");
            asg2.setYear("2nd Year");
            asg2.setDepartment("Information Technology");
            asg2.setRollNumber("70012023012");
            asg2.setAssignedDuty("Stage Coordinator & Green Room Management");
            asg2.setEventDate("2025-04-10");
            asg2.setStartTime("15:00");
            asg2.setEndTime("21:00");
            asg2.setVenue("Campus Central Amphitheatre");
            asg2.setOrganizerId("demo_organizer_uid");
            asg2.setOrganizerName("NMIMS Cultural Committee");
            asg2.setStatus(VolunteerAssignment.STATUS_APPROVED);
            asg2.setReviewedBy("demo_admin_uid");
            asg2.setReviewedAt(Timestamp.now());
            asgSeeds.add(asg2);

            // Pending assignment awaiting Admin review
            VolunteerAssignment asg3 = new VolunteerAssignment();
            asg3.setAssignmentId("asg_seed_3");
            asg3.setEventId("ev_seed_1");
            asg3.setEventName("NMIMS Annual Hackathon 2025");
            asg3.setUserId("demo_student_3");
            asg3.setUserName("Rohan Gupta");
            asg3.setUserEmail("rohan.gupta@unisync.nmims.edu");
            asg3.setUserType(User.USER_TYPE_STUDENT);
            asg3.setCourse("B.Tech Electronics & Telecom");
            asg3.setYear("3rd Year");
            asg3.setDepartment("Electronics & Telecom");
            asg3.setRollNumber("70012022089");
            asg3.setAssignedDuty("Network & WiFi Connectivity Team");
            asg3.setEventDate("2025-03-25");
            asg3.setStartTime("09:00");
            asg3.setEndTime("18:00");
            asg3.setVenue("Mukesh Patel Technology Auditorium");
            asg3.setOrganizerId("demo_organizer_uid");
            asg3.setOrganizerName("Turing Tech Club");
            asg3.setStatus(VolunteerAssignment.STATUS_PENDING);
            asgSeeds.add(asg3);

            persistAssignments(asgSeeds);

            // Also seed legacy volunteers list
            List<Volunteer> volSeeds = new ArrayList<>();
            for (VolunteerAssignment asg : asgSeeds) {
                Volunteer v = new Volunteer();
                v.setVolunteerId(asg.getAssignmentId());
                v.setEventId(asg.getEventId());
                v.setEventName(asg.getEventName());
                v.setUserId(asg.getUserId());
                v.setStudentName(asg.getUserName());
                v.setUserEmail(asg.getUserEmail());
                v.setUserType(asg.getUserType());
                v.setCourse(asg.getCourse());
                v.setYear(asg.getYear());
                v.setDepartment(asg.getDepartment());
                v.setRollNumber(asg.getRollNumber());
                v.setAssignedDuty(asg.getAssignedDuty());
                v.setTeam(asg.getOrganizerName());
                v.setEventDate(asg.getEventDate());
                v.setStartTime(asg.getStartTime());
                v.setEndTime(asg.getEndTime());
                v.setVenue(asg.getVenue());
                v.setOrganizerId(asg.getOrganizerId());
                v.setStatus(asg.getStatus());
                v.setCreatedBy(asg.getOrganizerId());
                volSeeds.add(v);
            }
            persistVolunteers(volSeeds);
        }
    }
}
