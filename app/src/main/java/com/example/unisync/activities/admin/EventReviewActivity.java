package com.example.unisync.activities.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.unisync.R;
import com.example.unisync.models.AdminLog;
import com.example.unisync.models.Event;
import com.example.unisync.utils.FirestoreHelper;
import com.example.unisync.utils.SessionManager;
import com.example.unisync.utils.UIHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * EventReviewActivity — allows Admins to review pending events,
 * approve them, publish them directly, or reject them with feedback.
 */
public class EventReviewActivity extends AppCompatActivity {

    private TextView tvCategory, tvStatus, tvEventName, tvShortDescription;
    private TextView tvDate, tvTime, tvVenue, tvOrganizingTeam;
    private TextView tvDescription, tvFaculty, tvRegInfo, tvContactInfo;
    private MaterialButton btnApproveAndPublish, btnApprove, btnReject;
    private MaterialButton btnUnpublishEvent, btnRestoreEvent;
    private ProgressBar progressBar;

    private SessionManager sessionManager;
    private FirebaseFirestore db;
    private String eventId;
    private Event currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_review);

        sessionManager = new SessionManager(this);
        db = FirestoreHelper.getDb();
        eventId = getIntent().getStringExtra("eventId");

        if (eventId == null) {
            UIHelper.showToast(this, "Event not found");
            finish();
            return;
        }

        initViews();
        setupToolbar();
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
        btnApproveAndPublish = findViewById(R.id.btnApproveAndPublish);
        btnApprove = findViewById(R.id.btnApprove);
        btnReject = findViewById(R.id.btnReject);
        btnUnpublishEvent = findViewById(R.id.btnUnpublishEvent);
        btnRestoreEvent = findViewById(R.id.btnRestoreEvent);
        progressBar = findViewById(R.id.progressBar);

        btnApprove.setOnClickListener(v ->
                UIHelper.showConfirmDialog(this, "Approve Event",
                        "Approve this event? The organizer can publish it when ready.",
                        () -> handleDecision(Event.STATUS_APPROVED, null, false)));

        btnApproveAndPublish.setOnClickListener(v ->
                UIHelper.showConfirmDialog(this, "Approve & Publish",
                        "Approve and make this event visible immediately to students?",
                        () -> handleDecision(Event.STATUS_PUBLISHED, null, true)));

        btnReject.setOnClickListener(v ->
                UIHelper.showInputDialog(this, "Reject / Return Event",
                        "Provide a reason for rejection / corrections needed",
                        reason -> handleDecision(Event.STATUS_REJECTED, reason, false)));

        btnUnpublishEvent.setOnClickListener(v ->
                UIHelper.showConfirmDialog(this, "Remove Event from Directory",
                        "This event will no longer appear in the public All Events directory. Are you sure you want to remove it?",
                        () -> handleDecision(Event.STATUS_CANCELLED, "Removed / Unpublished by Admin", false)));

        btnRestoreEvent.setOnClickListener(v ->
                UIHelper.showConfirmDialog(this, "Restore Event",
                        "Restore this cancelled event back to approved status?",
                        () -> handleDecision(Event.STATUS_APPROVED, "Restored to approved state by Admin", false)));
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
                    displayData();
                } else {
                    UIHelper.showToast(EventReviewActivity.this, "Event document not found");
                    finish();
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                UIHelper.showToast(EventReviewActivity.this, "Event not found");
                finish();
            }
        });
    }

    private void displayData() {
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
        tvDescription.setText(currentEvent.getDescription() != null ? currentEvent.getDescription() : "No detailed description.");
        tvFaculty.setText("Faculty In Charge: " + (currentEvent.getFacultyInCharge() != null ? currentEvent.getFacultyInCharge() : "None"));
        tvRegInfo.setText("Registration: " + (currentEvent.getRegistrationInfo() != null ? currentEvent.getRegistrationInfo() : "None"));
        tvContactInfo.setText("Contact: " + (currentEvent.getContactInfo() != null ? currentEvent.getContactInfo() : "None"));

        // Configure actions according to exact event lifecycle status
        String status = currentEvent.getStatus();
        if (Event.STATUS_PUBLISHED.equals(status)) {
            btnApproveAndPublish.setVisibility(View.GONE);
            btnApprove.setVisibility(View.GONE);
            btnReject.setVisibility(View.GONE);
            btnUnpublishEvent.setVisibility(View.VISIBLE);
            btnRestoreEvent.setVisibility(View.GONE);
        } else if (Event.STATUS_CANCELLED.equals(status)) {
            btnApproveAndPublish.setVisibility(View.GONE);
            btnApprove.setVisibility(View.GONE);
            btnReject.setVisibility(View.GONE);
            btnUnpublishEvent.setVisibility(View.GONE);
            btnRestoreEvent.setVisibility(View.VISIBLE);
        } else if (Event.STATUS_APPROVED.equals(status)) {
            btnApproveAndPublish.setVisibility(View.VISIBLE);
            btnApproveAndPublish.setText("Publish Event Now");
            btnApprove.setVisibility(View.GONE);
            btnReject.setVisibility(View.VISIBLE);
            btnUnpublishEvent.setVisibility(View.GONE);
            btnRestoreEvent.setVisibility(View.GONE);
        } else if (Event.STATUS_REJECTED.equals(status)) {
            btnApproveAndPublish.setVisibility(View.GONE);
            btnApprove.setVisibility(View.VISIBLE);
            btnApprove.setText("Reconsider & Approve Event");
            btnReject.setVisibility(View.GONE);
            btnUnpublishEvent.setVisibility(View.GONE);
            btnRestoreEvent.setVisibility(View.GONE);
        } else { // PENDING / DRAFT
            btnApproveAndPublish.setVisibility(View.VISIBLE);
            btnApproveAndPublish.setText("Approve & Publish Immediately");
            btnApprove.setVisibility(View.VISIBLE);
            btnApprove.setText("Approve Event");
            btnReject.setVisibility(View.VISIBLE);
            btnUnpublishEvent.setVisibility(View.GONE);
            btnRestoreEvent.setVisibility(View.GONE);
        }
    }

    private void handleDecision(String newStatus, String reason, boolean publishNow) {
        progressBar.setVisibility(View.VISIBLE);

        com.example.unisync.utils.DataManager.getInstance().updateEventStatus(eventId, newStatus, reason, new com.example.unisync.utils.DataManager.OnCompleteListener() {
            @Override
            public void onSuccess() {
                // Create audit log
                try {
                    String action = Event.STATUS_PUBLISHED.equals(newStatus)
                            ? AdminLog.ACTION_PUBLISH_EVENT
                            : (Event.STATUS_APPROVED.equals(newStatus)
                            ? AdminLog.ACTION_APPROVE_EVENT
                            : (Event.STATUS_CANCELLED.equals(newStatus) ? "CANCEL_EVENT" : AdminLog.ACTION_REJECT_EVENT));

                    AdminLog log = new AdminLog(
                            sessionManager.getUid(),
                            action,
                            AdminLog.TARGET_EVENT,
                            eventId,
                            currentEvent != null ? currentEvent.getStatus() : Event.STATUS_PENDING,
                            newStatus,
                            reason != null ? reason : "Administrative decision"
                    );
                    db.collection(AdminLog.COLLECTION_NAME).add(log);
                } catch (Exception ignored) {}

                progressBar.setVisibility(View.GONE);
                if (Event.STATUS_CANCELLED.equals(newStatus)) {
                    UIHelper.showToast(EventReviewActivity.this, "Event removed from public directory.");
                } else if (Event.STATUS_PUBLISHED.equals(newStatus)) {
                    UIHelper.showToast(EventReviewActivity.this, "Event published successfully!");
                } else {
                    UIHelper.showToast(EventReviewActivity.this, "Decision recorded successfully!");
                }
                finish();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                UIHelper.showToast(EventReviewActivity.this, "Operation failed: " + error);
                finish();
            }
        });
    }
}
