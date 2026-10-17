package com.example.unisync.activities.organizer;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unisync.R;
import com.example.unisync.adapters.RequestPersonAdapter;
import com.example.unisync.models.RequestPerson;
import com.example.unisync.models.TeamRequest;
import com.example.unisync.utils.FirestoreHelper;
import com.example.unisync.utils.UIHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * SupportRequestDetailActivity — view team support request details,
 * people roster, feedback from admin, and status actions.
 */
public class SupportRequestDetailActivity extends AppCompatActivity {

    private TextView tvCategory, tvStatus, tvTitle, tvReason;
    private TextView tvDate, tvPeopleRequired, tvLocation, tvInstructions;
    private MaterialCardView cardFeedback;
    private TextView tvFeedbackTitle, tvFeedbackReason;
    private RecyclerView rvPeopleList;
    private TextView tvNoRoster;
    private MaterialButton btnSubmitApproval, btnDeleteRequest;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private String requestId;
    private TeamRequest currentRequest;
    private final List<RequestPerson> rosterPeople = new ArrayList<>();
    private RequestPersonAdapter rosterAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_request_detail);

        db = FirestoreHelper.getDb();
        requestId = getIntent().getStringExtra("requestId");

        if (requestId == null) {
            UIHelper.showToast(this, "Request not found");
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupRosterRecyclerView();
        loadRequestDetails();
    }

    private void initViews() {
        tvCategory = findViewById(R.id.tvCategory);
        tvStatus = findViewById(R.id.tvStatus);
        tvTitle = findViewById(R.id.tvTitle);
        tvReason = findViewById(R.id.tvReason);
        tvDate = findViewById(R.id.tvDate);
        tvPeopleRequired = findViewById(R.id.tvPeopleRequired);
        tvLocation = findViewById(R.id.tvLocation);
        tvInstructions = findViewById(R.id.tvInstructions);
        cardFeedback = findViewById(R.id.cardFeedback);
        tvFeedbackTitle = findViewById(R.id.tvFeedbackTitle);
        tvFeedbackReason = findViewById(R.id.tvFeedbackReason);
        rvPeopleList = findViewById(R.id.rvPeopleList);
        tvNoRoster = findViewById(R.id.tvNoRoster);
        btnSubmitApproval = findViewById(R.id.btnSubmitApproval);
        btnDeleteRequest = findViewById(R.id.btnDeleteRequest);
        progressBar = findViewById(R.id.progressBar);

        btnSubmitApproval.setOnClickListener(v ->
                UIHelper.showConfirmDialog(this, "Submit Request",
                        "Submit this team request for Admin review?",
                        () -> updateStatus(TeamRequest.STATUS_PENDING, "Submitted for review!")));

        btnDeleteRequest.setOnClickListener(v ->
                UIHelper.showConfirmDialog(this, "Delete Request",
                        "Are you sure you want to delete this request?", this::deleteRequest));
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

    private void setupRosterRecyclerView() {
        rosterAdapter = new RequestPersonAdapter(rosterPeople, false, null);
        rvPeopleList.setLayoutManager(new LinearLayoutManager(this));
        rvPeopleList.setAdapter(rosterAdapter);
    }

    private void loadRequestDetails() {
        progressBar.setVisibility(View.VISIBLE);
        com.example.unisync.utils.DataManager.getInstance().getTeamRequestById(requestId, new com.example.unisync.utils.DataManager.OnObjectListener<TeamRequest>() {
            @Override
            public void onSuccess(TeamRequest request) {
                progressBar.setVisibility(View.GONE);
                currentRequest = request;
                if (currentRequest != null) {
                    displayRequestData();
                    loadRosterPeople();
                } else {
                    UIHelper.showToast(SupportRequestDetailActivity.this, "Request not found");
                    finish();
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                UIHelper.showToast(SupportRequestDetailActivity.this, "Request not found");
                finish();
            }
        });
    }

    private void displayRequestData() {
        tvCategory.setText(currentRequest.getCategory());
        UIHelper.applyRequestStatusBadge(tvStatus, currentRequest.getStatus());
        tvTitle.setText(currentRequest.getTitle());
        tvReason.setText(currentRequest.getReason());

        String displayDate = UIHelper.formatDateForDisplay(currentRequest.getRequiredDate());
        tvDate.setText(!displayDate.isEmpty() ? displayDate : "Date TBA");

        int peopleCount = currentRequest.getPeopleRequired();
        tvPeopleRequired.setText(peopleCount > 0 ? (peopleCount + (peopleCount == 1 ? " Person" : " People")) : "Volunteers");

        tvLocation.setText(currentRequest.getWorkLocation() != null ? currentRequest.getWorkLocation() : "Campus");

        if (currentRequest.getAdditionalInstructions() != null && !currentRequest.getAdditionalInstructions().isEmpty()) {
            tvInstructions.setText("Instructions: " + currentRequest.getAdditionalInstructions());
            tvInstructions.setVisibility(View.VISIBLE);
        } else {
            tvInstructions.setVisibility(View.GONE);
        }

        // Feedback card
        String status = currentRequest.getStatus();
        if ((TeamRequest.STATUS_REJECTED.equals(status) || TeamRequest.STATUS_NEEDS_CORRECTION.equals(status))
                && currentRequest.getRejectionReason() != null
                && !currentRequest.getRejectionReason().isEmpty()) {
            cardFeedback.setVisibility(View.VISIBLE);
            tvFeedbackTitle.setText(TeamRequest.STATUS_NEEDS_CORRECTION.equals(status) ? "Correction Requested" : "Rejection Reason");
            tvFeedbackReason.setText(currentRequest.getRejectionReason());
        } else {
            cardFeedback.setVisibility(View.GONE);
        }

        // Button visibility
        if (TeamRequest.STATUS_DRAFT.equals(status)
                || TeamRequest.STATUS_REJECTED.equals(status)
                || TeamRequest.STATUS_NEEDS_CORRECTION.equals(status)) {
            btnSubmitApproval.setVisibility(View.VISIBLE);
            btnDeleteRequest.setVisibility(View.VISIBLE);
        } else {
            btnSubmitApproval.setVisibility(View.GONE);
            btnDeleteRequest.setVisibility(View.GONE);
        }
    }

    private void loadRosterPeople() {
        com.example.unisync.utils.DataManager.getInstance().getRequestRoster(requestId, people -> {
            rosterPeople.clear();
            if (people != null) {
                rosterPeople.addAll(people);
            }
            rosterAdapter.notifyDataSetChanged();
            boolean hasPeople = !rosterPeople.isEmpty();
            tvNoRoster.setVisibility(hasPeople ? View.GONE : View.VISIBLE);
            rvPeopleList.setVisibility(hasPeople ? View.VISIBLE : View.GONE);
        });
    }

    private void updateStatus(String newStatus, String message) {
        progressBar.setVisibility(View.VISIBLE);
        com.example.unisync.utils.DataManager.getInstance().updateRequestStatus(requestId, newStatus, null, new com.example.unisync.utils.DataManager.OnCompleteListener() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                UIHelper.showToast(SupportRequestDetailActivity.this, message);
                loadRequestDetails();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                UIHelper.showToast(SupportRequestDetailActivity.this, message);
                loadRequestDetails();
            }
        });
    }

    private void deleteRequest() {
        progressBar.setVisibility(View.VISIBLE);
        com.example.unisync.utils.DataManager.getInstance().deleteTeamRequest(requestId, new com.example.unisync.utils.DataManager.OnCompleteListener() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                UIHelper.showToast(SupportRequestDetailActivity.this, "Request deleted");
                finish();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                UIHelper.showToast(SupportRequestDetailActivity.this, "Request deleted");
                finish();
            }
        });
    }
}
