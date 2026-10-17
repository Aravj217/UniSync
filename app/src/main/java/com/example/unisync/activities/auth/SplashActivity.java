package com.example.unisync.activities.auth;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.unisync.R;
import com.example.unisync.activities.admin.AdminDashboardActivity;
import com.example.unisync.activities.organizer.OrganizerDashboardActivity;
import com.example.unisync.activities.student.StudentDashboardActivity;
import com.example.unisync.models.User;
import com.example.unisync.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * SplashActivity — the first screen shown when the app opens.
 * Displays the UniSync branding with smooth animations,
 * then routes to the correct dashboard based on login state and role.
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 2500; // 2.5 seconds

    private ImageView ivLogo;
    private TextView tvAppName;
    private TextView tvTagline;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Find views
        ivLogo = findViewById(R.id.ivLogo);
        tvAppName = findViewById(R.id.tvAppName);
        tvTagline = findViewById(R.id.tvTagline);
        progressBar = findViewById(R.id.progressBar);

        // Start entrance animations
        playEntranceAnimations();

        // Navigate after delay
        new Handler(Looper.getMainLooper()).postDelayed(this::navigateToNext, SPLASH_DELAY);
    }

    /**
     * Plays smooth entrance animations for logo, title and tagline.
     */
    private void playEntranceAnimations() {
        // Initially invisible
        ivLogo.setAlpha(0f);
        ivLogo.setScaleX(0.5f);
        ivLogo.setScaleY(0.5f);
        tvAppName.setAlpha(0f);
        tvAppName.setTranslationY(30f);
        tvTagline.setAlpha(0f);
        tvTagline.setTranslationY(20f);

        // Logo animation — scale + fade
        ObjectAnimator logoAlpha = ObjectAnimator.ofFloat(ivLogo, "alpha", 0f, 1f);
        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(ivLogo, "scaleX", 0.5f, 1f);
        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(ivLogo, "scaleY", 0.5f, 1f);
        AnimatorSet logoAnim = new AnimatorSet();
        logoAnim.playTogether(logoAlpha, logoScaleX, logoScaleY);
        logoAnim.setDuration(800);
        logoAnim.setInterpolator(new OvershootInterpolator(1.2f));

        // App name animation — slide up + fade
        ObjectAnimator nameAlpha = ObjectAnimator.ofFloat(tvAppName, "alpha", 0f, 1f);
        ObjectAnimator nameSlide = ObjectAnimator.ofFloat(tvAppName, "translationY", 30f, 0f);
        AnimatorSet nameAnim = new AnimatorSet();
        nameAnim.playTogether(nameAlpha, nameSlide);
        nameAnim.setDuration(600);
        nameAnim.setStartDelay(400);

        // Tagline animation — slide up + fade
        ObjectAnimator tagAlpha = ObjectAnimator.ofFloat(tvTagline, "alpha", 0f, 1f);
        ObjectAnimator tagSlide = ObjectAnimator.ofFloat(tvTagline, "translationY", 20f, 0f);
        AnimatorSet tagAnim = new AnimatorSet();
        tagAnim.playTogether(tagAlpha, tagSlide);
        tagAnim.setDuration(600);
        tagAnim.setStartDelay(700);

        // Play all animations
        logoAnim.start();
        nameAnim.start();
        tagAnim.start();
    }

    /**
     * Determine where to navigate based on authentication state.
     */
    private void navigateToNext() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        SessionManager sessionManager = new SessionManager(this);

        if (firebaseUser != null && sessionManager.isLoggedIn()) {
            // User is logged in — navigate based on role
            navigateToDashboard(sessionManager.getRole());
        } else {
            // Not logged in — go to Login
            startActivity(new Intent(this, LoginActivity.class));
        }

        finish(); // Close splash so user can't go back to it
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Navigate to the correct dashboard based on user role.
     */
    private void navigateToDashboard(String role) {
        Intent intent;
        switch (role) {
            case User.ROLE_ADMIN:
                intent = new Intent(this, AdminDashboardActivity.class);
                break;
            case User.ROLE_ORGANIZER:
                intent = new Intent(this, OrganizerDashboardActivity.class);
                break;
            default:
                intent = new Intent(this, StudentDashboardActivity.class);
                break;
        }
        startActivity(intent);
    }
}
