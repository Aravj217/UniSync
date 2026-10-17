package com.example.unisync.activities.organizer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * ManageVolunteersActivity — Organizer screen to view and manage volunteer assignments.
 * Displays approval status (Pending, Approved, Rejected, Needs Correction) and allows editing/resubmitting.
 */
public class ManageVolunteersActivity extends AppCompatActivity {

    private EditText etSearchVolunteer;
    private ChipGroup chipGroupStatus;
    private RecyclerView rvVolunteers;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;
    private FloatingActionButton fabAddVolunteer;

    private SessionManager sessionManager;
    private DataManager dataManager;
    private VolunteerAssignmentAdapter adapter;

    private final List<VolunteerAssignment> allAssignments = new ArrayList<>();
    private final List<VolunteerAssignment> filteredAssignments = new ArrayList<>();
    private String eventIdFilter = null;
    private String selectedStatus = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_volunteers);

        sessionManager = new SessionManager(this);
        dataManager = DataManager.getInstance();

        if (getIntent().hasExtra("eventId")) {
            eventIdFilter = getIntent().getStringExtra("eventId");
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSearchAndFilter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAssignments();
    }

    private void initViews() {
        etSearchVolunteer = findViewById(R.id.etSearchVolunteer);
        chipGroupStatus = findViewById(R.id.chipGroupStatus);
        rvVolunteers = findViewById(R.id.rvVolunteers);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        fabAddVolunteer = findViewById(R.id.fabAddVolunteer);

        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.accent);
        swipeRefresh.setOnRefreshListener(this::loadAssignments);

        fabAddVolunteer.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddVolunteerActivity.class);
            if (eventIdFilter != null) {
                intent.putExtra("eventId", eventIdFilter);
            }
            startActivity(intent);
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(eventIdFilter != null ? "Event Assignments" : "Volunteer Assignments");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new VolunteerAssignmentAdapter(filteredAssignments, VolunteerAssignmentAdapter.ViewMode.ORGANIZER,
                new VolunteerAssignmentAdapter.OnAssignmentActionListener() {
                    @Override
                    public void onEdit(VolunteerAssignment assignment) {
                        Intent intent = new Intent(ManageVolunteersActivity.this, AddVolunteerActivity.class);
                        intent.putExtra("assignmentId", assignment.getAssignmentId());
                        intent.putExtra("eventId", assignment.getEventId());
                        startActivity(intent);
                    }

                    @Override
                    public void onCancel(VolunteerAssignment assignment) {
                        UIHelper.showConfirmDialog(ManageVolunteersActivity.this, "Cancel Assignment",
                                "Are you sure you want to cancel the assignment for " + assignment.getUserName() + "?",
                                () -> cancelAssignment(assignment));
                    }
                });
        rvVolunteers.setLayoutManager(new LinearLayoutManager(this));
        rvVolunteers.setAdapter(adapter);
    }

    private void setupSearchAndFilter() {
        etSearchVolunteer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAssignments();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null) {
                    selectedStatus = chip.getText().toString();
                    filterAssignments();
                }
            }
        });
    }

    private void loadAssignments() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        String organizerId = sessionManager.getUid();

        dataManager.getVolunteerAssignmentsByOrganizer(organizerId, list -> {
            allAssignments.clear();
            if (list != null) {
                for (VolunteerAssignment a : list) {
                    if (eventIdFilter == null || eventIdFilter.equals(a.getEventId())) {
                        allAssignments.add(a);
                    }
                }
            }
            filterAssignments();
            progressBar.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);
        });
    }

    private void filterAssignments() {
        filteredAssignments.clear();
        String query = etSearchVolunteer.getText().toString().toLowerCase().trim();

        for (VolunteerAssignment a : allAssignments) {
            // Status filter
            if (!"All".equalsIgnoreCase(selectedStatus)) {
                if ("Active".equalsIgnoreCase(selectedStatus)) {
                    if (!a.isActive()) continue;
                } else if ("Cancelled".equalsIgnoreCase(selectedStatus)) {
                    if (!VolunteerAssignment.STATUS_CANCELLED.equalsIgnoreCase(a.getStatus())) continue;
                } else {
                    String matchStatus = a.getStatus() != null ? a.getStatus().replace("_", " ") : "";
                    if (!matchStatus.equalsIgnoreCase(selectedStatus)) {
                        continue;
                    }
                }
            }

            // Search query filter
            if (!query.isEmpty()) {
                boolean matches = false;
                if (a.getUserName() != null && a.getUserName().toLowerCase().contains(query)) matches = true;
                if (a.getUserEmail() != null && a.getUserEmail().toLowerCase().contains(query)) matches = true;
                if (a.getAssignedDuty() != null && a.getAssignedDuty().toLowerCase().contains(query)) matches = true;
                if (a.getEventName() != null && a.getEventName().toLowerCase().contains(query)) matches = true;
                if (a.getDepartment() != null && a.getDepartment().toLowerCase().contains(query)) matches = true;
                if (!matches) continue;
            }

            filteredAssignments.add(a);
        }

        adapter.notifyDataSetChanged();
        boolean empty = filteredAssignments.isEmpty();
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvVolunteers.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void cancelAssignment(VolunteerAssignment assignment) {
        progressBar.setVisibility(View.VISIBLE);
        dataManager.updateVolunteerAssignmentStatus(assignment.getAssignmentId(),
                VolunteerAssignment.STATUS_CANCELLED, "Cancelled by Organizer", sessionManager.getUid(),
                new DataManager.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                        UIHelper.showToast(ManageVolunteersActivity.this, "Assignment cancelled.");
                        loadAssignments();
                    }

                    @Override
                    public void onError(String error) {
                        progressBar.setVisibility(View.GONE);
                        UIHelper.showToast(ManageVolunteersActivity.this, "Failed to cancel: " + error);
                    }
                });
    }
}
