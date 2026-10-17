package com.example.unisync.utils;

import com.example.unisync.models.AdminLog;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * FirestoreHelper — centralized Firestore operations and logging.
 * Provides convenience methods for common database operations.
 */
public final class FirestoreHelper {

    private FirestoreHelper() {} // Prevent instantiation

    private static FirebaseFirestore db;

    /**
     * Get the Firestore database instance (singleton).
     */
    public static FirebaseFirestore getDb() {
        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }
        return db;
    }

    /**
     * Log an administrative action to the administrativeLogs collection.
     *
     * @param actorUid       The UID of the admin performing the action
     * @param action         The action type (e.g., APPROVE_EVENT, REJECT_REQUEST)
     * @param targetType     The type of target (EVENT, TEAM_REQUEST, USER)
     * @param targetId       The ID of the target document
     * @param previousStatus The status before the action
     * @param newStatus      The status after the action
     * @param reason         Optional reason (e.g., rejection reason)
     */
    public static void logAction(String actorUid, String action, String targetType,
                                  String targetId, String previousStatus,
                                  String newStatus, String reason) {
        AdminLog log = new AdminLog(actorUid, action, targetType, targetId,
                previousStatus, newStatus, reason);
        getDb().collection(AdminLog.COLLECTION_NAME)
                .add(log)
                .addOnFailureListener(e -> {
                    // Logging should never block the main operation
                    // Silently handle the error
                });
    }

    /**
     * Generate a new document ID for a collection.
     */
    public static String generateId(String collectionName) {
        return getDb().collection(collectionName).document().getId();
    }
}
