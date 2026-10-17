package com.example.unisync.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.unisync.R;
import com.example.unisync.activities.admin.AdminDashboardActivity;
import com.example.unisync.activities.organizer.OrganizerDashboardActivity;
import com.example.unisync.activities.student.StudentDashboardActivity;
import com.example.unisync.models.User;
import com.example.unisync.utils.SessionManager;
import com.example.unisync.utils.UIHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * LoginActivity — handles user authentication via Firebase.
 * After successful login, loads the user profile from Firestore
 * and navigates to the appropriate role-based dashboard.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private ProgressBar progressBar;
    private TextView tvError, tvRegister;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    // Flag to prevent double submission
    private boolean isLoggingIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        // Find views
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);
        tvRegister = findViewById(R.id.tvRegister);

        // Demo Buttons
        MaterialButton btnDemoAdmin = findViewById(R.id.btnDemoAdmin);
        MaterialButton btnDemoOrganizer = findViewById(R.id.btnDemoOrganizer);
        MaterialButton btnDemoStudent = findViewById(R.id.btnDemoStudent);

        if (btnDemoAdmin != null) {
            btnDemoAdmin.setOnClickListener(v -> fillCredentials("admin@unisync.com", "admin123"));
        }
        if (btnDemoOrganizer != null) {
            btnDemoOrganizer.setOnClickListener(v -> fillCredentials("organizer@unisync.com", "organizer123"));
        }
        if (btnDemoStudent != null) {
            btnDemoStudent.setOnClickListener(v -> fillCredentials("student@unisync.com", "student123"));
        }

        // Set click listeners
        btnLogin.setOnClickListener(v -> attemptLogin());
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void fillCredentials(String email, String password) {
        etEmail.setText(email);
        etPassword.setText(password);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tvError.setVisibility(View.GONE);
    }

    /**
     * Validate inputs and attempt login via Firebase Auth with demo account fallback.
     */
    private void attemptLogin() {
        if (isLoggingIn) return; // Prevent double-tap

        // Clear previous errors
        tilEmail.setError(null);
        tilPassword.setError(null);
        tvError.setVisibility(View.GONE);

        String email = UIHelper.getText(etEmail);
        String password = UIHelper.getText(etPassword);

        // Validate email
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        if (!UIHelper.isValidEmail(email)) {
            tilEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        // Start login
        setLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            loadUserProfile(firebaseUser.getUid());
                        } else {
                            setLoading(false);
                            showError("Authentication failed. Please try again.");
                        }
                    } else {
                        // Check if demo credentials match
                        if (checkAndLoginDemo(email, password)) {
                            return;
                        }

                        setLoading(false);
                        String errorMsg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Login failed. Please check your credentials.";
                        showError(errorMsg);
                        Log.e(TAG, "Login failed", task.getException());
                    }
                });
    }

    /**
     * Quick demo authentication handler.
     */
    private boolean checkAndLoginDemo(String email, String password) {
        String lowerEmail = email.toLowerCase().trim();

        if (("admin@unisync.com".equals(lowerEmail) || "admin@unisync.nmims.edu".equals(lowerEmail))
                && "admin123".equals(password)) {
            setLoading(false);
            loginWithDemoAccount("demo_admin_uid", "Campus Administrator", lowerEmail, User.ROLE_ADMIN);
            return true;
        }

        if (("organizer@unisync.com".equals(lowerEmail) || "organizer@unisync.nmims.edu".equals(lowerEmail))
                && "organizer123".equals(password)) {
            setLoading(false);
            loginWithDemoAccount("demo_organizer_uid", "Event Coordinator", lowerEmail, User.ROLE_ORGANIZER);
            return true;
        }

        if (("student@unisync.com".equals(lowerEmail) || "student@unisync.nmims.edu".equals(lowerEmail))
                && "student123".equals(password)) {
            setLoading(false);
            loginWithDemoAccount("demo_student_uid", "Aarav Sharma", lowerEmail, User.ROLE_STUDENT);
            return true;
        }

        return false;
    }

    private void loginWithDemoAccount(String uid, String name, String email, String role) {
        User demoUser = new User(uid, name, email, role);
        demoUser.setCourse("B.Tech Computer Science");
        demoUser.setYear("3");
        demoUser.setDepartment("MPSTME, NMIMS");
        demoUser.setRollNumber("70012022045");
        demoUser.setActive(true);

        sessionManager.createSession(demoUser);
        UIHelper.showToast(this, "Logged in as " + name + " (" + role + ")");
        navigateToDashboard(role);
    }

    /**
     * Load the user's profile from Firestore after successful authentication.
     */
    private void loadUserProfile(String uid) {
        db.collection(User.COLLECTION_NAME)
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    setLoading(false);

                    if (document.exists()) {
                        User user = document.toObject(User.class);
                        if (user != null) {
                            user.setUid(uid);

                            // Check if user account is active
                            if (!user.isActive()) {
                                mAuth.signOut();
                                showError("Your account has been deactivated. Contact admin.");
                                return;
                            }

                            // Save session
                            sessionManager.createSession(user);

                            // Navigate to role-based dashboard
                            navigateToDashboard(user.getRole());
                        } else {
                            showError("Failed to load profile. Please try again.");
                        }
                    } else {
                        // Profile document doesn't exist
                        showError("User profile not found. Contact admin.");
                        mAuth.signOut();
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    User cached = null;
                    for (User u : com.example.unisync.utils.DataManager.getInstance().getUsersFromCache()) {
                        if (uid.equals(u.getUid()) || (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getEmail() != null && mAuth.getCurrentUser().getEmail().equalsIgnoreCase(u.getEmail()))) {
                            cached = u;
                            break;
                        }
                    }
                    if (cached != null) {
                        sessionManager.createSession(cached);
                        navigateToDashboard(cached.getRole());
                    } else {
                        showError("Failed to load profile. Please check your credentials.");
                        Log.e(TAG, "Error loading profile", e);
                    }
                });
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

        // Clear the back stack
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Show/hide loading state.
     */
    private void setLoading(boolean loading) {
        isLoggingIn = loading;
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? getString(R.string.logging_in) : getString(R.string.login));
        etEmail.setEnabled(!loading);
        etPassword.setEnabled(!loading);
    }

    /**
     * Display an error message.
     */
    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
