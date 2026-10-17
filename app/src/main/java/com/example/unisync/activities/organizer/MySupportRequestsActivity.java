package com.example.unisync.activities.organizer;

import android.content.Intent;
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
import com.example.unisync.adapters.TeamRequestAdapter;
import com.example.unisync.models.TeamRequest;
import com.example.unisync.utils.FirestoreHelper;
import com.example.unisync.utils.SessionManager;
import com.example.unisync.utils.UIHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * MySupportRequestsActivity — displays all team support requests created by the organizer.
 */
public class MySupportRequestsActivity extends AppCompatActivity {

    private RecyclerView rvMyRequests;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;
    private ChipGroup chipGroupStatus;
    private FloatingActionButton fabCreateRequest;

    private SessionManager sessionManager;
    private FirebaseFirestore db;
    private TeamRequestAdapter adapter;

    private final List<TeamRequest> allMyRequests = new ArrayList<>();
    private final List<TeamRequest> displayedRequests = new ArrayList<>();
    private String selectedStatus = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_support_requests);

        sessionManager = new SessionManager(this);
        db = FirestoreHelper.getDb();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFilterChips();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyRequests();
    }

    private void initViews() {
        rvMyRequests = findViewById(R.id.rvMyRequests);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        chipGroupStatus = findViewById(R.id.chipGroupStatus);
        fabCreateRequest = findViewById(R.id.fabCreateRequest);

        swipeRefresh.setColorSchemeResources(R.color.accent, R.color.primary);
        swipeRefresh.setOnRefreshListener(this::loadMyRequests);

        fabCreateRequest.setOnClickListener(v ->
                startActivity(new Intent(this, CreateSupportRequestActivity.class)));
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

    private void setupRecyclerView() {
        adapter = new TeamRequestAdapter(displayedRequests, request -> {
            Intent intent = new Intent(this, SupportRequestDetailActivity.class);
            intent.putExtra("requestId", request.getRequestId());
            startActivity(intent);
        });
        rvMyRequests.setLayoutManager(new LinearLayoutManager(this));
        rvMyRequests.setAdapter(adapter);
    }

    private void setupFilterChips() {
        chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null) {
                    selectedStatus = chip.getText().toString();
                    filterRequests();
                }
            }
        });
    }

    private void loadMyRequests() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        com.example.unisync.utils.DataManager.getInstance().getTeamRequestsByOrganizer(sessionManager.getUid(), requests -> {
            allMyRequests.clear();
            if (requests != null) {
                allMyRequests.addAll(requests);
            }
            filterRequests();
            progressBar.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);
        });
    }

    private void filterRequests() {
        displayedRequests.clear();
        for (TeamRequest req : allMyRequests) {
            if ("All".equalsIgnoreCase(selectedStatus) || selectedStatus.equalsIgnoreCase(req.getStatus())) {
                displayedRequests.add(req);
            }
        }
        adapter.notifyDataSetChanged();
        boolean isEmpty = displayedRequests.isEmpty();
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvMyRequests.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}
