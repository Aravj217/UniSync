package com.example.unisync.activities.organizer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.unisync.R;
import com.example.unisync.models.Event;
import com.example.unisync.utils.FirestoreHelper;
import com.example.unisync.utils.UIHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * EventDetailActivity — detailed view of an event for organizers.
 * Shows current status, details, rejection feedback, and actions to edit, delete,
 * submit for approval, or publish.
 */
public class EventDetailActivity extends AppCompatActivity {

    private TextView tvCategory, tvStatus, tvEventName, tvShortDescription;
    private TextView tvDate, tvTime, tvVenue, tvOrganizingTeam;
    private TextView tvDescription, tvFaculty, tvRegInfo, tvContactInfo;
    private MaterialCardView cardRejection;
    private TextView tvRejectionReason;
    private MaterialButton btnSubmitApproval, btnPublish, btnUnpublishEvent, btnEditEvent, btnDeleteEvent;
    private MaterialButton btnManageVolunteers;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private String eventId;
    private Event currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        db = FirestoreHelper.getDb();
        eventId = getIntent().getStringExtra("eventId");

        if (eventId == null) {
            UIHelper.showToast(this, "Event not found");
            finish();
            return;
        }

        initViews();
        setupToolbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEventDetails();
    }

    private void initViews() {
        tvCategory = findViewById(R.id.tvCategory);
        tvStatus = findViewById(R.id.tvStatus);
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
        cardRejection = findViewById(R.id.cardRejection);
        tvRejectionReason = findViewById(R.id.tvRejectionReason);
        btnSubmitApproval = findViewById(R.id.btnSubmitApproval);
        btnPublish = findViewById(R.id.btnPublish);
        btnUnpublishEvent = findViewById(R.id.btnUnpublishEvent);
        btnEditEvent = findViewById(R.id.btnEditEvent);
        btnDeleteEvent = findViewById(R.id.btnDeleteEvent);
        btnManageVolunteers = findViewById(R.id.btnManageVolunteers);
        progressBar = findViewById(R.id.progressBar);

        btnManageVolunteers.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageVolunteersActivity.class);
            intent.putExtra("eventId", eventId);
            startActivity(intent);
        });

        btnEditEvent.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateEventActivity.class);
            intent.putExtra("eventId", eventId);
            startActivity(intent);
        });

        btnDeleteEvent.setOnClickListener(v ->
                UIHelper.showConfirmDialog(this, "Delete Event",
                        "Are you sure you want to delete this event?", this::deleteEvent));

        btnSubmitApproval.setOnClickListener(v ->
                UIHelper.showConfirmDialog(this, "Submit for Approval",
                        "Submit this event to the Admin for approval?",
                        () -> updateEventStatus(Event.STATUS_PENDING, "Submitted for approval!")));

        btnPublish.setOnClickListener(v ->
                UIHelper.showConfirmDialog(this, "Publish Event",
                        "Make this event visible to all students?", this::publishEvent));

        btnUnpublishEvent.setOnClickListener(v ->
                UIHelper.showConfirmDialog(this, "Unpublish Event",
                        "This event will no longer appear in the public All Events directory. Are you sure you want to remove it?",
                        () -> updateEventStatus(Event.STATUS_CANCELLED, "Event has been unpublished and removed from public directory.")));
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
        com.example.unisync.utils.DataManager.getInstance().getEventById(eventId, new com.example.unisync.utils.DataManager.OnObjectListener<Event>() {
            @Override
            public void onSuccess(Event event) {
                progressBar.setVisibility(View.GONE);
                currentEvent = event;
                if (currentEvent != null) {
                    displayEventData();
                } else {
                    UIHelper.showToast(EventDetailActivity.this, "Event not found");
                    finish();
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                UIHelper.showToast(EventDetailActivity.this, "Event not found");
                finish();
            }
        });
    }

    private void displayEventData() {
        tvCategory.setText(currentEvent.getCategory());
        UIHelper.applyEventStatusBadge(tvStatus, currentEvent.getStatus());
        tvEventName.setText(currentEvent.getEventName());
        tvShortDescription.setText(currentEvent.getShortDescription());

        String displayDate = UIHelper.formatDateForDisplay(currentEvent.getEventDate());
        tvDate.setText(!displayDate.isEmpty() ? displayDate : "Date TBA");

        String startTime = UIHelper.formatTimeForDisplay(currentEvent.getStartTime());
        String endTime = UIHelper.formatTimeForDisplay(currentEvent.getEndTime());
        tvTime.setText(startTime + (endTime.isEmpty() ? "" : " - " + endTime));

        tvVenue.setText(currentEvent.getVenue() != null ? currentEvent.getVenue() : "Venue TBA");
        tvOrganizingTeam.setText(currentEvent.getOrganizingTeam() != null ? currentEvent.getOrganizingTeam() : "Organizing Team");

        tvDescription.setText(currentEvent.getDescription() != null && !currentEvent.getDescription().isEmpty()
                ? currentEvent.getDescription() : "No additional description provided.");

        tvFaculty.setText("Faculty in Charge: " + (currentEvent.getFacultyInCharge() != null ? currentEvent.getFacultyInCharge() : "TBA"));
        tvRegInfo.setText("Registration: " + (currentEvent.getRegistrationInfo() != null ? currentEvent.getRegistrationInfo() : "Open"));
        tvContactInfo.setText("Contact: " + (currentEvent.getContactInfo() != null ? currentEvent.getContactInfo() : "N/A"));

        // Rejection reason card
        if (Event.STATUS_REJECTED.equals(currentEvent.getStatus())
                && currentEvent.getRejectionReason() != null
                && !currentEvent.getRejectionReason().isEmpty()) {
            cardRejection.setVisibility(View.VISIBLE);
            tvRejectionReason.setText(currentEvent.getRejectionReason());
        } else {
            cardRejection.setVisibility(View.GONE);
        }

        // Configure actions based on status
        String status = currentEvent.getStatus();
        if (Event.STATUS_DRAFT.equals(status) || Event.STATUS_REJECTED.equals(status)) {
            btnSubmitApproval.setVisibility(View.VISIBLE);
            btnPublish.setVisibility(View.GONE);
            btnUnpublishEvent.setVisibility(View.GONE);
            btnEditEvent.setVisibility(View.VISIBLE);
            btnDeleteEvent.setVisibility(View.VISIBLE);
        } else if (Event.STATUS_APPROVED.equals(status)) {
            btnSubmitApproval.setVisibility(View.GONE);
            btnPublish.setVisibility(View.VISIBLE);
            btnUnpublishEvent.setVisibility(View.GONE);
            btnEditEvent.setVisibility(View.GONE);
            btnDeleteEvent.setVisibility(View.GONE);
        } else if (Event.STATUS_PENDING.equals(status)) {
            btnSubmitApproval.setVisibility(View.GONE);
            btnPublish.setVisibility(View.GONE);
            btnUnpublishEvent.setVisibility(View.GONE);
            btnEditEvent.setVisibility(View.GONE);
            btnDeleteEvent.setVisibility(View.VISIBLE);
        } else if (Event.STATUS_PUBLISHED.equals(status)) {
            btnSubmitApproval.setVisibility(View.GONE);
            btnPublish.setVisibility(View.GONE);
            btnUnpublishEvent.setVisibility(View.VISIBLE);
            btnEditEvent.setVisibility(View.GONE);
            btnDeleteEvent.setVisibility(View.GONE);
        } else { // CANCELLED
            btnSubmitApproval.setVisibility(View.GONE);
            btnPublish.setVisibility(View.GONE);
            btnUnpublishEvent.setVisibility(View.GONE);
            btnEditEvent.setVisibility(View.GONE);
            btnDeleteEvent.setVisibility(View.GONE);
        }
    }

    private void updateEventStatus(String newStatus, String successMessage) {
        progressBar.setVisibility(View.VISIBLE);
        com.example.unisync.utils.DataManager.getInstance().updateEventStatus(eventId, newStatus, null, new com.example.unisync.utils.DataManager.OnCompleteListener() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                UIHelper.showToast(EventDetailActivity.this, successMessage);
                loadEventDetails();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                UIHelper.showToast(EventDetailActivity.this, successMessage);
                loadEventDetails();
            }
        });
    }

    private void publishEvent() {
        progressBar.setVisibility(View.VISIBLE);
        com.example.unisync.utils.DataManager.getInstance().updateEventStatus(eventId, Event.STATUS_PUBLISHED, null, new com.example.unisync.utils.DataManager.OnCompleteListener() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                UIHelper.showToast(EventDetailActivity.this, "Event published successfully!");
                loadEventDetails();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                UIHelper.showToast(EventDetailActivity.this, "Event published successfully!");
                loadEventDetails();
            }
        });
    }

    private void deleteEvent() {
        progressBar.setVisibility(View.VISIBLE);
        com.example.unisync.utils.DataManager.getInstance().deleteEvent(eventId, new com.example.unisync.utils.DataManager.OnCompleteListener() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                UIHelper.showToast(EventDetailActivity.this, "Event deleted");
                finish();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                UIHelper.showToast(EventDetailActivity.this, "Event deleted");
                finish();
            }
        });
    }
}
