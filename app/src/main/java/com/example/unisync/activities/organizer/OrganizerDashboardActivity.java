package com.example.unisync.activities.organizer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.unisync.R;
import com.example.unisync.activities.auth.ProfileActivity;
import com.example.unisync.models.Event;
import com.example.unisync.models.TeamRequest;
import com.example.unisync.models.VolunteerAssignment;
import com.example.unisync.utils.DataManager;
import com.example.unisync.utils.SessionManager;

import java.util.List;

/**
 * OrganizerDashboardActivity — the main hub for official event organizers.
 *
 * Provides the 5 main sections:
 * 1. Create Event
 * 2. My Events
 * 3. Assign Volunteers
 * 4. My Volunteer Assignments
 * 5. Profile
 */
public class OrganizerDashboardActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private DataManager dataManager;

    private TextView tvTotalEventsCount;
    private TextView tvPendingEventsCount;
    private TextView tvPublishedEventsCount;
    private TextView tvActiveRequestsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_dashboard);

        sessionManager = new SessionManager(this);
        dataManager = DataManager.getInstance();

        initViews();
    }

    private void initViews() {
        TextView tvWelcome = findViewById(R.id.tvWelcome);
        tvWelcome.setText(String.format("Welcome, %s", sessionManager.getFullName()));

        tvTotalEventsCount = findViewById(R.id.tvTotalEventsCount);
        tvPendingEventsCount = findViewById(R.id.tvPendingEventsCount);
        tvPublishedEventsCount = findViewById(R.id.tvPublishedEventsCount);
        tvActiveRequestsCount = findViewById(R.id.tvActiveRequestsCount);

        // 5. Profile
        findViewById(R.id.ivProfile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        // 1. Create Event
        CardView cardCreateEvent = findViewById(R.id.cardCreateEvent);
        if (cardCreateEvent != null) {
            cardCreateEvent.setOnClickListener(v ->
                    startActivity(new Intent(this, CreateEventActivity.class)));
        }

        // 3. Assign Volunteers
        CardView cardAssignVolunteersBanner = findViewById(R.id.cardAssignVolunteersBanner);
        if (cardAssignVolunteersBanner != null) {
            cardAssignVolunteersBanner.setOnClickListener(v ->
                    startActivity(new Intent(this, AddVolunteerActivity.class)));
        }

        // 2. My Events
        CardView cardMyEvents = findViewById(R.id.cardMyEvents);
        if (cardMyEvents != null) {
            cardMyEvents.setOnClickListener(v ->
                    startActivity(new Intent(this, MyEventsActivity.class)));
        }

        // 4. My Volunteer Assignments
        CardView cardVolunteers = findViewById(R.id.cardVolunteers);
        if (cardVolunteers != null) {
            cardVolunteers.setOnClickListener(v ->
                    startActivity(new Intent(this, ManageVolunteersActivity.class)));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrganizerCounts();
    }

    private void loadOrganizerCounts() {
        String uid = sessionManager.getUid();

        dataManager.getEventsByOrganizer(uid, events -> {
            int total = 0;
            int pending = 0;
            int published = 0;
            if (events != null) {
                total = events.size();
                for (Event e : events) {
                    if (Event.STATUS_PENDING.equals(e.getStatus())) pending++;
                    if (Event.STATUS_PUBLISHED.equals(e.getStatus())) published++;
                }
            }
            if (tvTotalEventsCount != null) tvTotalEventsCount.setText(String.valueOf(total));
            if (tvPendingEventsCount != null) tvPendingEventsCount.setText(String.valueOf(pending));
            if (tvPublishedEventsCount != null) tvPublishedEventsCount.setText(String.valueOf(published));
        });

        // Count volunteer assignments created by this organizer
        dataManager.getVolunteerAssignmentsByOrganizer(uid, assignments -> {
            if (tvActiveRequestsCount != null) {
                int count = 0;
                if (assignments != null) {
                    for (VolunteerAssignment va : assignments) {
                        if (va.isActive()) count++;
                    }
                }
                tvActiveRequestsCount.setText(String.valueOf(count));
            }
        });
    }
}
