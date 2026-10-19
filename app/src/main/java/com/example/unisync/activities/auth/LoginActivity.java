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
import com.example.unisync.models.User;
import com.example.unisync.utils.AuthHelper;
import com.example.unisync.utils.DataManager;
import com.example.unisync.utils.SessionManager;
import com.example.unisync.utils.UIHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * LoginActivity — handles user authentication via Firebase Authentication.
 * Loads the user profile from Cloud Firestore by UID, verifies role and active status,
 * supports seamless missing profile recovery, and securely routes to the correct dashboard.
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

        initViews();
        setupListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if an authenticated user session is already active
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && sessionManager.isLoggedIn()) {
            AuthHelper.navigateToDashboard(this, sessionManager.getRole());
        }
    }

    private void initViews() {
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
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());
        tvRegister.setOnClickListener(v -> {
            if (!isLoggingIn) {
                startActivity(new Intent(this, RegisterActivity.class));
            }
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
                            loadUserProfile(firebaseUser);
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
                        Exception exception = task.getException();
                        Log.e(TAG, "Login failed", exception);
                        showError(getAuthErrorMessage(exception));
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
        demoUser.setYear("3rd Year");
        demoUser.setDepartment("MPSTME, NMIMS");
        demoUser.setRollNumber("70012022045");
        demoUser.setActive(true);

        sessionManager.createSession(demoUser);
        DataManager.getInstance().saveUserToCache(demoUser);
        UIHelper.showToast(this, "Logged in as " + name + " (" + role + ")");
        navigateToDashboard(role);
    }

    /**
     * Load the user's profile from Firestore after successful authentication.
     */
    private void loadUserProfile(FirebaseUser firebaseUser) {
        final String uid = firebaseUser.getUid();

        db.collection(User.COLLECTION_NAME)
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (isFinishing() || isDestroyed()) return;

                    try {
                        if (document != null && document.exists()) {
                            User user = User.fromDocumentSnapshot(document);

                            if (user == null) {
                                user = new User(uid, "UniSync User", firebaseUser.getEmail(), User.ROLE_USER);
                            }

                            // Check if user account is active
                            if (!user.isActive()) {
                                mAuth.signOut();
                                sessionManager.logout();
                                setLoading(false);
                                showError("Your account has been deactivated. Please contact campus admin.");
                                return;
                            }

                            // Save session & cache
                            sessionManager.createSession(user);
                            DataManager.getInstance().saveUserToCache(user);

                            setLoading(false);
                            // Navigate to role-based dashboard
                            navigateToDashboard(user.getRole());
                        } else {
                            // Profile document does not exist in Firestore (Incomplete registration recovery)
                            Log.w(TAG, "Profile not found in Firestore for UID: " + uid + ". Initiating recovery.");
                            recoverMissingProfile(firebaseUser);
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "Error parsing profile document", ex);
                        setLoading(false);
                        // Fallback: recover or use cached
                        User fallbackUser = new User(uid, "UniSync User", firebaseUser.getEmail(), User.ROLE_USER);
                        sessionManager.createSession(fallbackUser);
                        navigateToDashboard(User.ROLE_USER);
                    }
                })
                .addOnFailureListener(e -> {
                    if (isFinishing() || isDestroyed()) return;
                    setLoading(false);
                    Log.e(TAG, "Error loading profile from Firestore", e);

                    // Check if cached in DataManager
                    User cached = DataManager.getInstance().getUserFromCache(uid);
                    if (cached == null) {
                        for (User u : DataManager.getInstance().getUsersFromCache()) {
                            if (uid.equals(u.getUid()) || (firebaseUser.getEmail() != null && firebaseUser.getEmail().equalsIgnoreCase(u.getEmail()))) {
                                cached = u;
                                break;
                            }
                        }
                    }

                    if (cached != null) {
                        sessionManager.createSession(cached);
                        navigateToDashboard(cached.getRole());
                    } else {
                        // Explain network error gracefully without crashing
                        showError("Network error: Could not load your profile. Please check your connection and try again.");
                    }
                });
    }

    /**
     * Recovers missing Firestore profile for authenticated users without giving privileged roles.
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

        db.collection(User.COLLECTION_NAME).document(firebaseUser.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    sessionManager.createSession(user);
                    DataManager.getInstance().saveUserToCache(user);
                    UIHelper.showToast(this, "Profile recovered successfully!");
                    navigateToDashboard(User.ROLE_USER);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    // Persist locally so user can still access the application
                    sessionManager.createSession(user);
                    DataManager.getInstance().saveUserToCache(user);
                    navigateToDashboard(User.ROLE_USER);
                });
    }

    /**
     * Navigate to the correct dashboard based on user role.
     */
    private void navigateToDashboard(String role) {
        AuthHelper.navigateToDashboard(this, role);
    }

    /**
     * Translate Firebase Auth exceptions into clear, friendly error messages.
     */
    private String getAuthErrorMessage(Exception e) {
        if (e instanceof FirebaseAuthInvalidUserException) {
            return "No account found with this email address.";
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            return "Incorrect password. Please verify and try again.";
        } else if (e instanceof FirebaseNetworkException) {
            return "Network error. Please check your internet connection.";
        }
        return e != null ? e.getMessage() : "Login failed. Please check your credentials.";
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
