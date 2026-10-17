package com.example.unisync.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

/**
 * AdminLog model — tracks administrative actions for audit purposes.
 * Stored in the "administrativeLogs" Firestore collection.
 */
public class AdminLog {

    public static final String COLLECTION_NAME = "administrativeLogs";

    // Action types
    public static final String ACTION_APPROVE_EVENT = "APPROVE_EVENT";
    public static final String ACTION_REJECT_EVENT = "REJECT_EVENT";
    public static final String ACTION_PUBLISH_EVENT = "PUBLISH_EVENT";
    public static final String ACTION_UNPUBLISH_EVENT = "UNPUBLISH_EVENT";
    public static final String ACTION_CANCEL_EVENT = "CANCEL_EVENT";

    public static final String ACTION_APPROVE_REQUEST = "APPROVE_REQUEST";
    public static final String ACTION_REJECT_REQUEST = "REJECT_REQUEST";
    public static final String ACTION_RETURN_REQUEST = "RETURN_REQUEST";
    public static final String ACTION_CANCEL_REQUEST = "CANCEL_REQUEST";

    public static final String ACTION_APPROVE_ASSIGNMENT = "APPROVE_ASSIGNMENT";
    public static final String ACTION_REJECT_ASSIGNMENT = "REJECT_ASSIGNMENT";
    public static final String ACTION_RETURN_ASSIGNMENT = "RETURN_ASSIGNMENT";
    public static final String ACTION_WITHDRAW_ASSIGNMENT = "WITHDRAW_ASSIGNMENT";

    public static final String ACTION_CREATE_USER = "CREATE_USER";
    public static final String ACTION_UPDATE_USER = "UPDATE_USER";
    public static final String ACTION_DEACTIVATE_USER = "DEACTIVATE_USER";
    public static final String ACTION_CREATE_ORGANIZER = "CREATE_ORGANIZER";
    public static final String ACTION_UPDATE_ORGANIZER = "UPDATE_ORGANIZER";

    // Target types
    public static final String TARGET_EVENT = "EVENT";
    public static final String TARGET_REQUEST = "TEAM_REQUEST";
    public static final String TARGET_USER = "USER";
    public static final String TARGET_VOLUNTEER = "VOLUNTEER";
    public static final String TARGET_ASSIGNMENT = "VOLUNTEER_ASSIGNMENT";
    public static final String TARGET_ORGANIZER = "ORGANIZER";

    @DocumentId
    private String logId;
    private String actorUid;
    private String action;
    private String targetType;
    private String targetId;
    private String previousStatus;
    private String newStatus;
    private String reason;

    @ServerTimestamp
    private Timestamp timestamp;

    // Required empty constructor for Firestore
    public AdminLog() {}

    public AdminLog(String actorUid, String action, String targetType,
                    String targetId, String previousStatus, String newStatus, String reason) {
        this.actorUid = actorUid;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.reason = reason;
    }

    // ── Getters ──

    public String getLogId() { return logId; }
    public String getActorUid() { return actorUid; }
    public String getAction() { return action; }
    public String getTargetType() { return targetType; }
    public String getTargetId() { return targetId; }
    public String getPreviousStatus() { return previousStatus; }
    public String getNewStatus() { return newStatus; }
    public String getReason() { return reason; }
    public Timestamp getTimestamp() { return timestamp; }

    // ── Setters ──

    public void setLogId(String logId) { this.logId = logId; }
    public void setActorUid(String actorUid) { this.actorUid = actorUid; }
    public void setAction(String action) { this.action = action; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    public void setReason(String reason) { this.reason = reason; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
