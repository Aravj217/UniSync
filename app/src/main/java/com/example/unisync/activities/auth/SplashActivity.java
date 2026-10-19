package com.example.unisync.activities.auth;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.unisync.R;
import com.example.unisync.models.User;
import com.example.unisync.utils.AuthHelper;
import com.example.unisync.utils.DataManager;
import com.example.unisync.utils.SessionManager;
import com.example.unisync.utils.UIHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * SplashActivity — initial startup activity.
 * Displays UniSync branding, checks persistent Firebase Authentication session,
 * loads/verifies user role and account status from Firestore, and routes directly to the correct dashboard.
 */
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final long MIN_SPLASH_DURATION_MS = 1400; // minimum branding display time

    private ImageView ivLogo;
    private TextView tvAppName;
    private TextView tvTagline;
    private ProgressBar progressBar;
    private LinearLayout layoutSplashError;
    private TextView tvSplashError;
    private MaterialButton btnSplashRetry;
    private MaterialButton btnSplashLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    private long startTime;
    private boolean hasNavigated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        initViews();
        playEntranceAnimations();

        startTime = System.currentTimeMillis();
        checkAuthenticationAndRoute();
    }

    private void initViews() {
        ivLogo = findViewById(R.id.ivLogo);
        tvAppName = findViewById(R.id.tvAppName);
        tvTagline = findViewById(R.id.tvTagline);
        progressBar = findViewById(R.id.progressBar);
        layoutSplashError = findViewById(R.id.layoutSplashError);
        tvSplashError = findViewById(R.id.tvSplashError);
        btnSplashRetry = findViewById(R.id.btnSplashRetry);
        btnSplashLogin = findViewById(R.id.btnSplashLogin);

        if (btnSplashRetry != null) {
            btnSplashRetry.setOnClickListener(v -> {
                if (layoutSplashError != null) layoutSplashError.setVisibility(View.GONE);
                if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
                checkAuthenticationAndRoute();
            });
        }

        if (btnSplashLogin != null) {
            btnSplashLogin.setOnClickListener(v -> {
                try {
                    mAuth.signOut();
                } catch (Exception ignored) {}
                sessionManager.logout();
                navigateToLogin();
            });
        }
    }

    private void playEntranceAnimations() {
        ivLogo.setAlpha(0f);
        ivLogo.setScaleX(0.5f);
        ivLogo.setScaleY(0.5f);
        tvAppName.setAlpha(0f);
        tvAppName.setTranslationY(30f);
        tvTagline.setAlpha(0f);
        tvTagline.setTranslationY(20f);

        ObjectAnimator logoAlpha = ObjectAnimator.ofFloat(ivLogo, "alpha", 0f, 1f);
        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(ivLogo, "scaleX", 0.5f, 1f);
        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(ivLogo, "scaleY", 0.5f, 1f);
        AnimatorSet logoAnim = new AnimatorSet();
        logoAnim.playTogether(logoAlpha, logoScaleX, logoScaleY);
        logoAnim.setDuration(700);
        logoAnim.setInterpolator(new OvershootInterpolator(1.2f));

        ObjectAnimator nameAlpha = ObjectAnimator.ofFloat(tvAppName, "alpha", 0f, 1f);
        ObjectAnimator nameSlide = ObjectAnimator.ofFloat(tvAppName, "translationY", 30f, 0f);
        AnimatorSet nameAnim = new AnimatorSet();
        nameAnim.playTogether(nameAlpha, nameSlide);
        nameAnim.setDuration(500);
        nameAnim.setStartDelay(300);

        ObjectAnimator tagAlpha = ObjectAnimator.ofFloat(tvTagline, "alpha", 0f, 1f);
        ObjectAnimator tagSlide = ObjectAnimator.ofFloat(tvTagline, "translationY", 20f, 0f);
        AnimatorSet tagAnim = new AnimatorSet();
        tagAnim.playTogether(tagAlpha, tagSlide);
        tagAnim.setDuration(500);
        tagAnim.setStartDelay(500);

        logoAnim.start();
        nameAnim.start();
        tagAnim.start();
    }

    /**
     * Checks persistent authentication session and directs to the appropriate screen.
     */
    private void checkAuthenticationAndRoute() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // Check if a local demo session is active
            if (sessionManager.isLoggedIn() && sessionManager.getUid().startsWith("demo_")) {
                postDelayedNavigation(() -> navigateToDashboard(sessionManager.getRole()));
                return;
            }

            // No active session -> Route to Login screen
            postDelayedNavigation(this::navigateToLogin);
            return;
        }

        // Active Firebase user exists: retrieve Firestore profile
        final String uid = currentUser.getUid();
        db.collection(User.COLLECTION_NAME).document(uid).get()
                .addOnSuccessListener(document -> {
                    if (isFinishing() || isDestroyed()) return;

                    if (document != null && document.exists()) {
                        User user = User.fromDocumentSnapshot(document);

                        // Verify account active status
                        if (!user.isActive()) {
                            mAuth.signOut();
                            sessionManager.logout();
                            UIHelper.showToast(this, "Your account has been deactivated. Contact admin.");
                            postDelayedNavigation(this::navigateToLogin);
                            return;
                        }

                        // Save refreshed profile in session and cache
                        sessionManager.createSession(user);
                        DataManager.getInstance().saveUserToCache(user);

                        postDelayedNavigation(() -> navigateToDashboard(user.getRole()));
                    } else {
                        // Firebase Auth user exists but Firestore profile is missing (incomplete registration recovery)
                        Log.w(TAG, "Profile missing in Firestore for UID: " + uid + ". Recovering basic profile.");
                        recoverMissingProfile(currentUser);
                    }
                })
                .addOnFailureListener(e -> {
                    if (isFinishing() || isDestroyed()) return;
                    Log.w(TAG, "Failed to load user profile from network: " + e.getMessage());

                    // If user has a valid cached session, allow offline access
                    if (sessionManager.isLoggedIn() && uid.equals(sessionManager.getUid())) {
                        postDelayedNavigation(() -> navigateToDashboard(sessionManager.getRole()));
                    } else {
                        // Check DataManager cache
                        User cached = DataManager.getInstance().getUserFromCache(uid);
                        if (cached != null) {
                            sessionManager.createSession(cached);
                            postDelayedNavigation(() -> navigateToDashboard(cached.getRole()));
                        } else {
                            // Show connection error and retry option instead of silently crashing or logging out
                            if (progressBar != null) progressBar.setVisibility(View.GONE);
                            if (layoutSplashError != null) layoutSplashError.setVisibility(View.VISIBLE);
                            if (tvSplashError != null) {
                                tvSplashError.setText("Network error: Unable to load your profile. Please check your connection.");
                            }
                        }
                    }
                });
    }

    /**
     * Recovers a missing Firestore profile for an already-authenticated Firebase user
     * without assigning privileged roles.
     */
    private void recoverMissingProfile(FirebaseUser firebaseUser) {
        String email = firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "";
        String fallbackName = email.contains("@") ? email.substring(0, email.indexOf("@")) : "UniSync User";

        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", firebaseUser.getUid());
        userData.put("fullName", fallbackName);
        userData.put("email", email);
        userData.put("role", User.ROLE_USER);
        userData.put("userType", User.USER_TYPE_STUDENT);
        userData.put("course", "General");
        userData.put("year", "1st Year");
        userData.put("department", "Other");
        userData.put("rollNumber", "TBA");
        userData.put("active", true);
        userData.put("createdAt", com.google.firebase.Timestamp.now());

        User user = new User(firebaseUser.getUid(), fallbackName, email, User.ROLE_USER);
        user.setActive(true);

        db.collection(User.COLLECTION_NAME).document(firebaseUser.getUid()).set(userData)
                .addOnSuccessListener(aVoid -> {
                    sessionManager.createSession(user);
                    DataManager.getInstance().saveUserToCache(user);
                    postDelayedNavigation(() -> navigateToDashboard(User.ROLE_USER));
                })
                .addOnFailureListener(e -> {
                    sessionManager.createSession(user);
                    DataManager.getInstance().saveUserToCache(user);
                    postDelayedNavigation(() -> navigateToDashboard(User.ROLE_USER));
                });
    }

    /**
     * Executes the navigation action after ensuring the minimum splash duration has elapsed.
     */
    private void postDelayedNavigation(Runnable action) {
        long elapsed = System.currentTimeMillis() - startTime;
        long remaining = Math.max(0, MIN_SPLASH_DURATION_MS - elapsed);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!hasNavigated && !isFinishing() && !isDestroyed()) {
                hasNavigated = true;
                action.run();
            }
        }, remaining);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void navigateToDashboard(String role) {
        AuthHelper.navigateToDashboard(this, role);
    }
}
