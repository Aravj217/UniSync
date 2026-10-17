package com.example.unisync.activities.student;

import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

/**
 * MyAssignmentsActivity — displays all official approved volunteer duties for the logged-in student or teacher.
 * Only APPROVED assignments appear as confirmed responsibilities.
 */
public class MyAssignmentsActivity extends AppCompatActivity {

    private RecyclerView rvAssignments;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;

    private SessionManager sessionManager;
    private DataManager dataManager;
    private VolunteerAssignmentAdapter adapter;
    private final List<VolunteerAssignment> myApprovedAssignments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_assignments);

        sessionManager = new SessionManager(this);
        dataManager = DataManager.getInstance();

        initViews();
        setupToolbar();
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyAssignments();
    }

    private void initViews() {
        rvAssignments = findViewById(R.id.rvAssignments);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.accent);
        swipeRefresh.setOnRefreshListener(this::loadMyAssignments);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("My Assignments");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new VolunteerAssignmentAdapter(myApprovedAssignments,
                VolunteerAssignmentAdapter.ViewMode.USER_ASSIGNMENTS, null);
        rvAssignments.setLayoutManager(new LinearLayoutManager(this));
        rvAssignments.setAdapter(adapter);
    }

    private void loadMyAssignments() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        String currentUid = sessionManager.getUid();

        dataManager.getApprovedAssignmentsByUser(currentUid, assignments -> {
            myApprovedAssignments.clear();
            if (assignments != null) {
                for (VolunteerAssignment a : assignments) {
                    // Strictly verify user UID and approved status
                    if (currentUid.equals(a.getUserId()) && VolunteerAssignment.STATUS_APPROVED.equals(a.getStatus())) {
                        myApprovedAssignments.add(a);
                    }
                }
            }

            adapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);

            boolean empty = myApprovedAssignments.isEmpty();
            layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            rvAssignments.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
    }
}
