package com.example.unisync.activities.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.unisync.R;
import com.example.unisync.adapters.VolunteerAssignmentAdapter;
import com.example.unisync.models.VolunteerAssignment;
import com.example.unisync.utils.DataManager;
import com.example.unisync.utils.SessionManager;
import com.example.unisync.utils.UIHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * AssignmentApprovalListActivity — allows Admins to review every volunteer assignment.
 * Workflow:
 * Organizer assigns user -> PENDING -> Admin reviews -> APPROVED, REJECTED, or NEEDS_CORRECTION.
 */
public class AssignmentApprovalListActivity extends AppCompatActivity {

    private ChipGroup chipGroupStatus;
    private RecyclerView rvAssignments;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;

    private SessionManager sessionManager;
    private DataManager dataManager;
    private VolunteerAssignmentAdapter adapter;

    private final List<VolunteerAssignment> allAssignments = new ArrayList<>();
    private final List<VolunteerAssignment> displayedAssignments = new ArrayList<>();
    private String selectedFilter = "Pending Approvals";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_approval_list);

        sessionManager = new SessionManager(this);
        dataManager = DataManager.getInstance();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFilters();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAssignments();
    }

    private void initViews() {
        chipGroupStatus = findViewById(R.id.chipGroupStatus);
        rvAssignments = findViewById(R.id.rvAssignments);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.accent);
        swipeRefresh.setOnRefreshListener(this::loadAssignments);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Volunteer Assignment Approvals");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new VolunteerAssignmentAdapter(displayedAssignments,
                VolunteerAssignmentAdapter.ViewMode.ADMIN,
                new VolunteerAssignmentAdapter.OnAssignmentActionListener() {
                    @Override
                    public void onApprove(VolunteerAssignment assignment) {
                        UIHelper.showConfirmDialog(AssignmentApprovalListActivity.this,
                                "Approve Volunteer Assignment",
                                "Approve " + assignment.getUserName() + " for duty: '" + assignment.getAssignedDuty() + "'?",
                                () -> updateStatus(assignment, VolunteerAssignment.STATUS_APPROVED, null));
                    }

                    @Override
                    public void onReject(VolunteerAssignment assignment) {
                        boolean isApproved = VolunteerAssignment.STATUS_APPROVED.equals(assignment.getStatus());
                        String title = isApproved ? "Withdraw Assignment" : "Reject Assignment";
                        String prompt = isApproved ? "Reason for withdrawing this assignment:" : "Please provide a reason for rejection:";

                        UIHelper.showInputDialog(AssignmentApprovalListActivity.this, title, prompt, reason -> {
                            if (TextUtils.isEmpty(reason)) {
                                UIHelper.showToast(AssignmentApprovalListActivity.this, "A reason is required to reject/withdraw.");
                                return;
                            }
                            String targetStatus = isApproved ? VolunteerAssignment.STATUS_CANCELLED : VolunteerAssignment.STATUS_REJECTED;
                            updateStatus(assignment, targetStatus, reason);
                        });
                    }

                    @Override
                    public void onReturn(VolunteerAssignment assignment) {
                        UIHelper.showInputDialog(AssignmentApprovalListActivity.this,
                                "Return for Correction",
                                "Specify the corrections needed by the organizer:",
                                reason -> {
                                    if (TextUtils.isEmpty(reason)) {
                                        UIHelper.showToast(AssignmentApprovalListActivity.this, "Correction details required.");
                                        return;
                                    }
                                    updateStatus(assignment, VolunteerAssignment.STATUS_NEEDS_CORRECTION, reason);
                                });
                    }
                });

        rvAssignments.setLayoutManager(new LinearLayoutManager(this));
        rvAssignments.setAdapter(adapter);
    }

    private void setupFilters() {
        chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null) {
                    selectedFilter = chip.getText().toString();
                    applyFilter();
                }
            }
        });
    }

    private void loadAssignments() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        dataManager.getVolunteerAssignments(list -> {
            allAssignments.clear();
            if (list != null) {
                allAssignments.addAll(list);
            }
            applyFilter();
            progressBar.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);
        });
    }

    private void applyFilter() {
        displayedAssignments.clear();

        for (VolunteerAssignment a : allAssignments) {
            String status = a.getStatus();
            if ("Pending Approvals".equalsIgnoreCase(selectedFilter)) {
                if (VolunteerAssignment.STATUS_PENDING.equals(status)) {
                    displayedAssignments.add(a);
                }
            } else if ("Approved".equalsIgnoreCase(selectedFilter)) {
                if (VolunteerAssignment.STATUS_APPROVED.equals(status)) {
                    displayedAssignments.add(a);
                }
            } else if ("Needs Correction".equalsIgnoreCase(selectedFilter)) {
                if (VolunteerAssignment.STATUS_NEEDS_CORRECTION.equals(status)) {
                    displayedAssignments.add(a);
                }
            } else if ("Rejected".equalsIgnoreCase(selectedFilter)) {
                if (VolunteerAssignment.STATUS_REJECTED.equals(status)) {
                    displayedAssignments.add(a);
                }
            } else {
                // All
                displayedAssignments.add(a);
            }
        }

        adapter.notifyDataSetChanged();
        boolean empty = displayedAssignments.isEmpty();
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvAssignments.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void updateStatus(VolunteerAssignment assignment, String newStatus, String reason) {
        progressBar.setVisibility(View.VISIBLE);
        dataManager.updateVolunteerAssignmentStatus(assignment.getAssignmentId(), newStatus, reason,
                sessionManager.getUid(), new DataManager.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                        String msg = VolunteerAssignment.STATUS_APPROVED.equals(newStatus)
                                ? "Volunteer assignment approved!"
                                : (VolunteerAssignment.STATUS_NEEDS_CORRECTION.equals(newStatus)
                                ? "Returned to organizer for correction."
                                : "Assignment rejected.");
                        UIHelper.showToast(AssignmentApprovalListActivity.this, msg);
                        loadAssignments();
                    }

                    @Override
                    public void onError(String error) {
                        progressBar.setVisibility(View.GONE);
                        UIHelper.showToast(AssignmentApprovalListActivity.this, "Operation failed: " + error);
                    }
                });
    }
}
