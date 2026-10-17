package com.example.unisync.activities.student;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unisync.R;
import com.example.unisync.adapters.ApprovedVolunteerAdapter;
import com.example.unisync.models.Event;
import com.example.unisync.models.VolunteerAssignment;
import com.example.unisync.utils.DataManager;
import com.example.unisync.utils.SessionManager;
import com.example.unisync.utils.UIHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * EventViewActivity — student/teacher view of an event with official details
 * and Admin-approved volunteer assignments. Self-service registration is strictly disabled.
 */
public class EventViewActivity extends AppCompatActivity {

    private TextView tvCategory, tvStatus, tvEventName, tvShortDescription;
    private TextView tvDate, tvTime, tvVenue, tvOrganizingTeam;
    private TextView tvDescription, tvFaculty, tvRegInfo, tvContactInfo;
    private TextView tvVolunteerCount, tvNoApprovedVolunteers;
    private android.widget.ImageView ivEventPoster;
    private View layoutPosterPlaceholder;
    private RecyclerView rvApprovedVolunteers;
    private ProgressBar progressBar;

    private ApprovedVolunteerAdapter volunteerAdapter;
    private SessionManager sessionManager;
    private String eventId;
    private Event currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_view);

        sessionManager = new SessionManager(this);
        eventId = getIntent().getStringExtra("eventId");

        if (eventId == null) {
            UIHelper.showToast(this, "Event not found");
            finish();
            return;
        }

        initViews();
        setupToolbar();
        loadEventDetails();
        loadApprovedVolunteers();
    }

    private void initViews() {
        tvCategory = findViewById(R.id.tvCategory);
        tvStatus = findViewById(R.id.tvStatus);
        ivEventPoster = findViewById(R.id.ivEventPoster);
        layoutPosterPlaceholder = findViewById(R.id.layoutPosterPlaceholder);
        tvEventName = findViewById(R.id.tvEventName);
        tvShortDescription = findViewById(R.id.tvShortDescription);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        tvVenue = findViewById(R.id.tvVenue);
        tvOrganizingTeam = findViewById(R.id.tvOrganizingTeam);
        tvDescription = findViewById(R.id.tvDescription);
        tvFaculty = findViewById(R.id.tvFaculty);
        tvRegInfo = findViewById(R.id.tvRegInfo);
        tvContactInfo = findViewById(R.id.tvContactInfo);
        tvVolunteerCount = findViewById(R.id.tvVolunteerCount);
        tvNoApprovedVolunteers = findViewById(R.id.tvNoApprovedVolunteers);
        rvApprovedVolunteers = findViewById(R.id.rvApprovedVolunteers);
        progressBar = findViewById(R.id.progressBar);

        rvApprovedVolunteers.setLayoutManager(new LinearLayoutManager(this));
        volunteerAdapter = new ApprovedVolunteerAdapter(new ArrayList<>());
        rvApprovedVolunteers.setAdapter(volunteerAdapter);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadEventDetails() {
        progressBar.setVisibility(View.VISIBLE);
        DataManager.getInstance().getEventById(eventId, new DataManager.OnObjectListener<Event>() {
            @Override
            public void onSuccess(Event event) {
                progressBar.setVisibility(View.GONE);
                currentEvent = event;
                if (currentEvent != null) {
                    displayData();
                } else {
                    UIHelper.showToast(EventViewActivity.this, "Event not found");
                    finish();
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                UIHelper.showToast(EventViewActivity.this, "Event not found");
                finish();
            }
        });
    }

    private void loadApprovedVolunteers() {
        DataManager.getInstance().getApprovedAssignmentsByEvent(eventId, new DataManager.OnListListener<VolunteerAssignment>() {
            @Override
            public void onSuccess(List<VolunteerAssignment> list) {
                if (list == null || list.isEmpty()) {
                    rvApprovedVolunteers.setVisibility(View.GONE);
                    tvNoApprovedVolunteers.setVisibility(View.VISIBLE);
                    tvVolunteerCount.setText("0 Assigned");
                } else {
                    rvApprovedVolunteers.setVisibility(View.VISIBLE);
                    tvNoApprovedVolunteers.setVisibility(View.GONE);
                    tvVolunteerCount.setText(list.size() + (list.size() == 1 ? " Volunteer" : " Volunteers"));
                    volunteerAdapter.updateList(list);
                }
            }

            @Override
            public void onError(String error) {
                rvApprovedVolunteers.setVisibility(View.GONE);
                tvNoApprovedVolunteers.setVisibility(View.VISIBLE);
            }
        });
    }

    private void displayData() {
        tvCategory.setText(currentEvent.getCategory());
        UIHelper.applyEventStatusBadge(tvStatus, currentEvent.getStatus());

        if (currentEvent.getPosterUrl() != null && !currentEvent.getPosterUrl().trim().isEmpty()) {
            ivEventPoster.setVisibility(View.VISIBLE);
            layoutPosterPlaceholder.setVisibility(View.GONE);
            com.bumptech.glide.Glide.with(this)
                    .load(currentEvent.getPosterUrl().trim())
                    .centerCrop()
                    .into(ivEventPoster);
        } else {
            ivEventPoster.setVisibility(View.GONE);
            layoutPosterPlaceholder.setVisibility(View.VISIBLE);
        }

        tvEventName.setText(currentEvent.getEventName());
        tvShortDescription.setText(currentEvent.getShortDescription());

        String displayDate = UIHelper.formatDateForDisplay(currentEvent.getEventDate());
        tvDate.setText(!displayDate.isEmpty() ? displayDate : "Date TBA");

        String startTime = UIHelper.formatTimeForDisplay(currentEvent.getStartTime());
        String endTime = UIHelper.formatTimeForDisplay(currentEvent.getEndTime());
        tvTime.setText(startTime + (endTime.isEmpty() ? "" : " - " + endTime));

        tvVenue.setText(currentEvent.getVenue() != null ? currentEvent.getVenue() : "Venue TBA");
        tvOrganizingTeam.setText(currentEvent.getOrganizingTeam() != null
                ? "Organized by: " + currentEvent.getOrganizingTeam() : "Campus Event");

        tvDescription.setText(currentEvent.getDescription() != null && !currentEvent.getDescription().isEmpty()
                ? currentEvent.getDescription() : currentEvent.getShortDescription());

        tvFaculty.setText("Faculty In Charge: " + (currentEvent.getFacultyInCharge() != null ? currentEvent.getFacultyInCharge() : "TBA"));
        tvRegInfo.setText("Registration: " + (currentEvent.getRegistrationInfo() != null ? currentEvent.getRegistrationInfo() : "Open to NMIMS students"));
        tvContactInfo.setText("Contact: " + (currentEvent.getContactInfo() != null ? currentEvent.getContactInfo() : "N/A"));
    }
}
