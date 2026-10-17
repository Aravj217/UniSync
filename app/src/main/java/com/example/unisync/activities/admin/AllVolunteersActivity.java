package com.example.unisync.activities.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
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

import java.util.ArrayList;
import java.util.List;

/**
 * AllVolunteersActivity — campus-wide directory of all volunteer assignments for Admins.
 */
public class AllVolunteersActivity extends AppCompatActivity {

    private EditText etSearchAllVolunteers;
    private RecyclerView rvAllVolunteers;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;

    private DataManager dataManager;
    private SessionManager sessionManager;
    private VolunteerAssignmentAdapter adapter;

    private final List<VolunteerAssignment> allAssignments = new ArrayList<>();
    private final List<VolunteerAssignment> displayedAssignments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_volunteers);

        dataManager = DataManager.getInstance();
        sessionManager = new SessionManager(this);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSearch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAssignments();
    }

    private void initViews() {
        etSearchAllVolunteers = findViewById(R.id.etSearchAllVolunteers);
        rvAllVolunteers = findViewById(R.id.rvAllVolunteers);
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
            getSupportActionBar().setTitle("All Volunteer Assignments");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new VolunteerAssignmentAdapter(displayedAssignments,
                VolunteerAssignmentAdapter.ViewMode.ADMIN,
                new VolunteerAssignmentAdapter.OnAssignmentActionListener() {
                    @Override
                    public void onCancel(VolunteerAssignment assignment) {
                        UIHelper.showConfirmDialog(AllVolunteersActivity.this, "Remove Assignment",
                                "Are you sure you want to remove the assignment for " + assignment.getUserName() + " (" + assignment.getAssignedDuty() + ")?",
                                () -> updateStatus(assignment, VolunteerAssignment.STATUS_CANCELLED, "Removed by Administrator"));
                    }
                });
        rvAllVolunteers.setLayoutManager(new LinearLayoutManager(this));
        rvAllVolunteers.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearchAllVolunteers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadAssignments() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        dataManager.getVolunteerAssignments(assignments -> {
            allAssignments.clear();
            if (assignments != null) {
                allAssignments.addAll(assignments);
            }
            filter(etSearchAllVolunteers.getText().toString());
            progressBar.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);
        });
    }

    private void filter(String query) {
        displayedAssignments.clear();
        String q = query.toLowerCase().trim();

        for (VolunteerAssignment a : allAssignments) {
            if (q.isEmpty()) {
                displayedAssignments.add(a);
            } else {
                boolean matches = false;
                if (a.getUserName() != null && a.getUserName().toLowerCase().contains(q)) matches = true;
                if (a.getUserEmail() != null && a.getUserEmail().toLowerCase().contains(q)) matches = true;
                if (a.getRollNumber() != null && a.getRollNumber().toLowerCase().contains(q)) matches = true;
                if (a.getCourse() != null && a.getCourse().toLowerCase().contains(q)) matches = true;
                if (a.getDepartment() != null && a.getDepartment().toLowerCase().contains(q)) matches = true;
                if (a.getAssignedDuty() != null && a.getAssignedDuty().toLowerCase().contains(q)) matches = true;
                if (a.getEventName() != null && a.getEventName().toLowerCase().contains(q)) matches = true;
                if (matches) displayedAssignments.add(a);
            }
        }

        adapter.notifyDataSetChanged();
        boolean empty = displayedAssignments.isEmpty();
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvAllVolunteers.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void updateStatus(VolunteerAssignment assignment, String newStatus, String reason) {
        progressBar.setVisibility(View.VISIBLE);
        dataManager.updateVolunteerAssignmentStatus(assignment.getAssignmentId(), newStatus, reason,
                sessionManager.getUid(), new DataManager.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                        UIHelper.showToast(AllVolunteersActivity.this, "Status updated.");
                        loadAssignments();
                    }

                    @Override
                    public void onError(String error) {
                        progressBar.setVisibility(View.GONE);
                        UIHelper.showToast(AllVolunteersActivity.this, "Failed: " + error);
                    }
                });
    }
}
