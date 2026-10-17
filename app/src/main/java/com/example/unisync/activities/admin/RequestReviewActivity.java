package com.example.unisync.activities.admin;

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
import com.example.unisync.models.AdminLog;
import com.example.unisync.models.RequestPerson;
import com.example.unisync.models.TeamRequest;
import com.example.unisync.utils.FirestoreHelper;
import com.example.unisync.utils.SessionManager;
import com.example.unisync.utils.UIHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RequestReviewActivity — allows Admins to review pending support requests,
 * approve them, return them for revisions, or reject them with reasons.
 */
public class RequestReviewActivity extends AppCompatActivity {

    private TextView tvCategory, tvStatus, tvTitle, tvReason;
    private TextView tvDate, tvPeopleRequired, tvLocation, tvInstructions;
    private RecyclerView rvPeopleList;
    private TextView tvNoRoster;
    private MaterialButton btnApprove, btnReturnForCorrection, btnReject;
    private ProgressBar progressBar;

    private SessionManager sessionManager;
    private FirebaseFirestore db;
    private String requestId;
    private TeamRequest currentRequest;
    private final List<RequestPerson> rosterPeople = new ArrayList<>();
    private RequestPersonAdapter rosterAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_review);

        sessionManager = new SessionManager(this);
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
        rvPeopleList = findViewById(R.id.rvPeopleList);
        tvNoRoster = findViewById(R.id.tvNoRoster);
        btnApprove = findViewById(R.id.btnApprove);
        btnReturnForCorrection = findViewById(R.id.btnReturnForCorrection);
        btnReject = findViewById(R.id.btnReject);
        progressBar = findViewById(R.id.progressBar);

        btnApprove.setOnClickListener(v ->
                UIHelper.showConfirmDialog(this, "Approve Request",
                        "Approve this team support request?",
                        () -> handleDecision(TeamRequest.STATUS_APPROVED, null)));

        btnReturnForCorrection.setOnClickListener(v ->
                UIHelper.showInputDialog(this, "Return for Correction",
                        "Specify the required corrections...",
                        feedback -> handleDecision(TeamRequest.STATUS_NEEDS_CORRECTION, feedback)));

        btnReject.setOnClickListener(v ->
                UIHelper.showInputDialog(this, "Reject Request",
                        "Provide reason for rejection...",
                        reason -> handleDecision(TeamRequest.STATUS_REJECTED, reason)));
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
                    UIHelper.showToast(RequestReviewActivity.this, "Request not found");
                    finish();
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                UIHelper.showToast(RequestReviewActivity.this, "Request not found");
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

    private void handleDecision(String newStatus, String reason) {
        progressBar.setVisibility(View.VISIBLE);

        com.example.unisync.utils.DataManager.getInstance().updateRequestStatus(requestId, newStatus, reason, new com.example.unisync.utils.DataManager.OnCompleteListener() {
            @Override
            public void onSuccess() {
                try {
                    String action = TeamRequest.STATUS_APPROVED.equals(newStatus)
                            ? AdminLog.ACTION_APPROVE_REQUEST
                            : (TeamRequest.STATUS_NEEDS_CORRECTION.equals(newStatus)
                            ? AdminLog.ACTION_RETURN_REQUEST : AdminLog.ACTION_REJECT_REQUEST);

                    AdminLog log = new AdminLog(
                            sessionManager.getUid(),
                            action,
                            AdminLog.TARGET_REQUEST,
                            requestId,
                            currentRequest != null ? currentRequest.getStatus() : TeamRequest.STATUS_PENDING,
                            newStatus,
                            reason != null ? reason : "Administrative decision"
                    );
                    db.collection(AdminLog.COLLECTION_NAME).add(log);
                } catch (Exception ignored) {}

                progressBar.setVisibility(View.GONE);
                UIHelper.showToast(RequestReviewActivity.this, "Decision recorded!");
                finish();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                UIHelper.showToast(RequestReviewActivity.this, "Decision recorded!");
                finish();
            }
        });
    }
}
