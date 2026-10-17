package com.example.unisync.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.unisync.R;
import com.example.unisync.models.User;
import com.example.unisync.utils.SessionManager;
import com.example.unisync.utils.UIHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

/**
 * ProfileActivity — displays the logged-in user's profile information
 * and provides a logout option.
 */
public class ProfileActivity extends AppCompatActivity {

    private ImageView ivBack;
    private TextView tvProfileName, tvProfileRole;
    private MaterialButton btnLogout;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);

        ivBack = findViewById(R.id.ivBack);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileRole = findViewById(R.id.tvProfileRole);
        btnLogout = findViewById(R.id.btnLogout);

        // Load profile data
        loadProfile();

        // Back button
        ivBack.setOnClickListener(v -> finish());

        // Logout
        btnLogout.setOnClickListener(v -> {
            UIHelper.showConfirmDialog(this, "Logout",
                    "Are you sure you want to logout?", this::performLogout);
        });
    }

    private void loadProfile() {
        User user = sessionManager.getUser();
        if (user == null) {
            finish();
            return;
        }

        tvProfileName.setText(user.getFullName());
        tvProfileRole.setText(user.getRole());

        // Set profile fields
        setFieldValue(R.id.fieldEmail, "Email", user.getEmail());
        setFieldValue(R.id.fieldCourse, "Course", user.getCourse());
        setFieldValue(R.id.fieldYear, "Academic Year", user.getYear());
        setFieldValue(R.id.fieldDepartment, "Department", user.getDepartment());
        setFieldValue(R.id.fieldRollNumber, "Roll Number", user.getRollNumber());
    }

    /**
     * Set label and value for a profile field include.
     */
    private void setFieldValue(int containerId, String label, String value) {
        View container = findViewById(containerId);
        if (container != null) {
            TextView tvLabel = container.findViewById(R.id.tvFieldLabel);
            TextView tvValue = container.findViewById(R.id.tvFieldValue);
            if (tvLabel != null) tvLabel.setText(label);
            if (tvValue != null) tvValue.setText(value != null && !value.isEmpty() ? value : "—");
        }
    }

    /**
     * Sign out from Firebase and clear local session.
     */
    private void performLogout() {
        FirebaseAuth.getInstance().signOut();
        sessionManager.logout();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        UIHelper.showToast(this, "Logged out successfully");
    }
}
