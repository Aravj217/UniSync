package com.example.unisync.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.unisync.R;
import com.example.unisync.activities.student.StudentDashboardActivity;
import com.example.unisync.models.User;
import com.example.unisync.utils.Constants;
import com.example.unisync.utils.SessionManager;
import com.example.unisync.utils.UIHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * RegisterActivity — allows students to create a new account.
 *
 * IMPORTANT: Only STUDENT role is assigned during public registration.
 * Admin and Organizer accounts must be provisioned through the Admin dashboard
 * or directly in Firebase Console.
 */
public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private TextInputLayout tilFullName, tilEmail, tilCourse, tilYear,
            tilDepartment, tilRollNumber, tilPassword, tilConfirmPassword, tilUserType;
    private TextInputEditText etFullName, etEmail, etCourse, etRollNumber,
            etPassword, etConfirmPassword;
    private MaterialAutoCompleteTextView actvYear, actvDepartment, actvUserType;
    private MaterialButton btnRegister;
    private ProgressBar progressBar;
    private TextView tvError, tvLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    private boolean isRegistering = false;

    private static final String[] USER_TYPE_OPTIONS = {"Student", "Teacher"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        initViews();
        setupDropdowns();
        setupListeners();
    }

    private void initViews() {
        tilUserType = findViewById(R.id.tilUserType);
        tilFullName = findViewById(R.id.tilFullName);
        tilEmail = findViewById(R.id.tilEmail);
        tilCourse = findViewById(R.id.tilCourse);
        tilYear = findViewById(R.id.tilYear);
        tilDepartment = findViewById(R.id.tilDepartment);
        tilRollNumber = findViewById(R.id.tilRollNumber);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        actvUserType = findViewById(R.id.actvUserType);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etCourse = findViewById(R.id.etCourse);
        etRollNumber = findViewById(R.id.etRollNumber);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        actvYear = findViewById(R.id.actvYear);
        actvDepartment = findViewById(R.id.actvDepartment);

        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void setupDropdowns() {
        // User type dropdown
        ArrayAdapter<String> userTypeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, USER_TYPE_OPTIONS);
        actvUserType.setAdapter(userTypeAdapter);

        // Year dropdown
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, Constants.YEAR_OPTIONS);
        actvYear.setAdapter(yearAdapter);

        // Department dropdown
        ArrayAdapter<String> deptAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, Constants.DEPARTMENT_OPTIONS);
        actvDepartment.setAdapter(deptAdapter);

        actvUserType.setOnItemClickListener((parent, view, position, id) -> {
            boolean isTeacher = "Teacher".equalsIgnoreCase(actvUserType.getText().toString());
            if (isTeacher) {
                tilYear.setVisibility(View.GONE);
                tilRollNumber.setHint("Employee / Faculty ID (Optional)");
                tilCourse.setHint("Specialization / Designation");
            } else {
                tilYear.setVisibility(View.VISIBLE);
                tilRollNumber.setHint(getString(R.string.roll_number));
                tilCourse.setHint(getString(R.string.course));
            }
        });
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> attemptRegister());
        tvLogin.setOnClickListener(v -> finish());
    }

    /**
     * Validate all fields and attempt registration.
     */
    private void attemptRegister() {
        if (isRegistering) return;

        // Clear errors
        clearErrors();

        String userTypeStr = actvUserType.getText().toString().trim();
        boolean isTeacher = "Teacher".equalsIgnoreCase(userTypeStr);
        String userType = isTeacher ? User.TYPE_TEACHER : User.TYPE_STUDENT;

        String fullName = UIHelper.getText(etFullName);
        String email = UIHelper.getText(etEmail);
        String course = UIHelper.getText(etCourse);
        String year = actvYear.getText().toString().trim();
        String department = actvDepartment.getText().toString().trim();
        String rollNumber = UIHelper.getText(etRollNumber);
        String password = UIHelper.getText(etPassword);
        String confirmPassword = UIHelper.getText(etConfirmPassword);

        if (isTeacher) {
            if (TextUtils.isEmpty(year)) year = "Faculty";
            if (TextUtils.isEmpty(rollNumber)) rollNumber = "FACULTY";
        }

        // Validate all fields
        boolean isValid = true;

        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError("Full name is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!UIHelper.isValidEmail(email)) {
            tilEmail.setError("Please enter a valid email");
            isValid = false;
        }
        if (TextUtils.isEmpty(course)) {
            tilCourse.setError(isTeacher ? "Specialization / Designation is required" : "Course is required");
            isValid = false;
        }
        if (!isTeacher && TextUtils.isEmpty(year)) {
            tilYear.setError("Year is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(department)) {
            tilDepartment.setError("Department is required");
            isValid = false;
        }
        if (!isTeacher && TextUtils.isEmpty(rollNumber)) {
            tilRollNumber.setError("Roll number is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            isValid = false;
        } else if (!UIHelper.isValidPassword(password)) {
            tilPassword.setError(getString(R.string.password_too_short));
            isValid = false;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError("Please confirm your password");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.passwords_dont_match));
            isValid = false;
        }

        if (!isValid) return;

        if (!UIHelper.isNetworkAvailable(this)) {
            showError(getString(R.string.no_internet));
            return;
        }

        setLoading(true);

        // Create account with Firebase Auth
        final String finalYear = year;
        final String finalRollNumber = rollNumber;
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            createUserProfile(firebaseUser.getUid(), fullName, email, userType,
                                    course, finalYear, department, finalRollNumber);
                        } else {
                            setLoading(false);
                            showError("Registration failed. Please try again.");
                        }
                    } else {
                        setLoading(false);
                        String errorMsg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Registration failed.";
                        showError(errorMsg);
                        Log.e(TAG, "Registration failed", task.getException());
                    }
                });
    }

    /**
     * Create the user profile document in Firestore.
     * Assigns USER role with userType STUDENT or TEACHER.
     * Admin and Organizer accounts must be provisioned by Admin.
     */
    private void createUserProfile(String uid, String fullName, String email, String userType,
                                    String course, String year, String department,
                                    String rollNumber) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("fullName", fullName);
        userData.put("email", email);
        userData.put("role", User.ROLE_USER); // Public registration assigns USER role
        userData.put("userType", userType);
        userData.put("course", course);
        userData.put("year", year);
        userData.put("department", department);
        userData.put("rollNumber", rollNumber);
        userData.put("active", true);
        userData.put("createdAt", com.google.firebase.Timestamp.now());

        User user = new User(uid, fullName, email, User.ROLE_USER);
        user.setUserType(userType);
        user.setCourse(course);
        user.setYear(year);
        user.setDepartment(department);
        user.setRollNumber(rollNumber);

        db.collection(User.COLLECTION_NAME)
                .document(uid)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    sessionManager.createSession(user);

                    // Navigate to User Dashboard
                    UIHelper.showToast(this, "Welcome to UniSync, " + fullName + "!");
                    Intent intent = new Intent(this, StudentDashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Fallback to DataManager local store if Firestore write fails
                    com.example.unisync.utils.DataManager.getInstance().saveUser(user, null);
                    sessionManager.createSession(user);
                    setLoading(false);
                    UIHelper.showToast(this, "Welcome to UniSync, " + fullName + "!");
                    Intent intent = new Intent(this, StudentDashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
    }

    private void clearErrors() {
        tilFullName.setError(null);
        tilEmail.setError(null);
        tilCourse.setError(null);
        tilYear.setError(null);
        tilDepartment.setError(null);
        tilRollNumber.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
        tvError.setVisibility(View.GONE);
    }

    private void setLoading(boolean loading) {
        isRegistering = loading;
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!loading);
        btnRegister.setText(loading ? getString(R.string.registering) : getString(R.string.register));
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
