package com.example.unisync.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.unisync.activities.admin.AdminDashboardActivity;
import com.example.unisync.activities.auth.LoginActivity;
import com.example.unisync.activities.organizer.OrganizerDashboardActivity;
import com.example.unisync.activities.student.StudentDashboardActivity;
import com.example.unisync.models.User;
import com.google.firebase.auth.FirebaseAuth;

/**
 * AuthHelper — centralized helper for role-based navigation, authentication session verification,
 * and reliable, backstack-clearing explicit logout.
 */
public final class AuthHelper {

    private static final String TAG = "AuthHelper";

    private AuthHelper() {} // Prevent instantiation

    /**
     * Get the explicit target Dashboard Intent for a given role.
     */
    public static Intent getDashboardIntent(Context context, String role) {
        String normalizedRole = (role != null && !role.trim().isEmpty()) ? role.trim().toUpperCase() : User.ROLE_USER;
        Intent intent;
        switch (normalizedRole) {
            case User.ROLE_ADMIN:
                intent = new Intent(context, AdminDashboardActivity.class);
                break;
            case User.ROLE_ORGANIZER:
                intent = new Intent(context, OrganizerDashboardActivity.class);
                break;
            case User.ROLE_USER:
            case User.ROLE_STUDENT:
            default:
                intent = new Intent(context, StudentDashboardActivity.class);
                break;
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    /**
     * Navigate to the appropriate role-based dashboard, ensuring backstack is cleared
     * so user cannot navigate back to Auth/Splash screens.
     */
    public static void navigateToDashboard(Activity activity, String role) {
        if (activity == null || activity.isFinishing()) return;
        Intent intent = getDashboardIntent(activity, role);
        activity.startActivity(intent);
        activity.finish();
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Prompts the user with a confirmation dialog and performs explicit logout on confirm.
     * Clears Firebase Auth, local SessionManager, and navigates back to LoginActivity
     * while completely clearing the Activity backstack.
     */
    public static void promptLogout(Activity activity) {
        if (activity == null || activity.isFinishing()) return;
        UIHelper.showConfirmDialog(activity, "Logout", "Are you sure you want to log out of UniSync?", () -> {
            executeLogout(activity);
        });
    }

    /**
     * Performs immediate explicit logout, signs out from Firebase Authentication,
     * clears SharedPreferences session, and routes to LoginActivity with cleared backstack.
     */
    public static void executeLogout(Activity activity) {
        if (activity == null) return;
        try {
            FirebaseAuth.getInstance().signOut();
        } catch (Exception e) {
            Log.e(TAG, "Error signing out from FirebaseAuth", e);
        }

        SessionManager sessionManager = new SessionManager(activity);
        sessionManager.logout();

        Intent intent = new Intent(activity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        if (!activity.isFinishing()) {
            activity.finish();
        }
        UIHelper.showToast(activity, "Logged out successfully");
    }
}
