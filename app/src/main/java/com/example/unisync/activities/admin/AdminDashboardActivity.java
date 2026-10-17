package com.example.unisync.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.unisync.R;
import com.example.unisync.activities.auth.ProfileActivity;
import com.example.unisync.utils.DataManager;
import com.example.unisync.utils.SessionManager;

/**
 * AdminDashboardActivity — central administrative hub with metrics and navigation
 * for event approvals, volunteer assignment reviews, request approvals, and user/organizer management.
 */
public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private TextView tvTotalEvents, tvPendingEvents, tvPublishedEvents, tvTotalUsers;

    private SessionManager sessionManager;
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        sessionManager = new SessionManager(this);
        dataManager = DataManager.getInstance();

        initViews();
        setupNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardCounts();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvTotalEvents = findViewById(R.id.tvTotalEvents);
        tvPendingEvents = findViewById(R.id.tvPendingEvents);
        tvPublishedEvents = findViewById(R.id.tvPublishedEvents);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);

        tvWelcome.setText(String.format("Welcome, %s", sessionManager.getFullName()));
    }

    private void setupNavigation() {
        // Profile
        findViewById(R.id.ivProfile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        // Event Approvals (Card and Pending Events Card)
        findViewById(R.id.cardEventApprovals).setOnClickListener(v ->
                startActivity(new Intent(this, EventApprovalListActivity.class)));
        findViewById(R.id.cardPendingEvents).setOnClickListener(v ->
                startActivity(new Intent(this, EventApprovalListActivity.class)));

        // All Events
        findViewById(R.id.cardAllEvents).setOnClickListener(v ->
                startActivity(new Intent(this, AllEventsActivity.class)));
        findViewById(R.id.cardTotalEvents).setOnClickListener(v ->
                startActivity(new Intent(this, AllEventsActivity.class)));
        findViewById(R.id.cardPublishedEvents).setOnClickListener(v ->
                startActivity(new Intent(this, AllEventsActivity.class)));

        // Manage Users & Organizers
        findViewById(R.id.cardManageUsers).setOnClickListener(v ->
                startActivity(new Intent(this, ManageUsersActivity.class)));
        findViewById(R.id.cardTotalUsers).setOnClickListener(v ->
                startActivity(new Intent(this, ManageUsersActivity.class)));
    }

    private void loadDashboardCounts() {
        dataManager.getAdminDashboardSummary((totalEvents, pendingEvents, publishedEvents,
                                              totalOrganizers, totalUsers, totalAssignments) -> {
            if (tvTotalEvents != null) tvTotalEvents.setText(String.valueOf(totalEvents));
            if (tvPendingEvents != null) tvPendingEvents.setText(String.valueOf(pendingEvents));
            if (tvPublishedEvents != null) tvPublishedEvents.setText(String.valueOf(publishedEvents));
            if (tvTotalUsers != null) tvTotalUsers.setText(String.valueOf(totalUsers));
        });
    }
}
