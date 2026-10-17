package com.example.unisync.activities.admin;

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
import com.example.unisync.utils.UIHelper;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * RequestApprovalListActivity — displays team support requests awaiting Admin review.
 */
public class RequestApprovalListActivity extends AppCompatActivity {

    private RecyclerView rvPendingRequests;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;

    private FirebaseFirestore db;
    private TeamRequestAdapter adapter;
    private final List<TeamRequest> pendingRequests = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_approval_list);

        db = FirestoreHelper.getDb();

        initViews();
        setupToolbar();
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPendingRequests();
    }

    private void initViews() {
        rvPendingRequests = findViewById(R.id.rvPendingRequests);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        swipeRefresh.setColorSchemeResources(R.color.accent, R.color.primary);
        swipeRefresh.setOnRefreshListener(this::loadPendingRequests);
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
        adapter = new TeamRequestAdapter(pendingRequests, request -> {
            Intent intent = new Intent(this, RequestReviewActivity.class);
            intent.putExtra("requestId", request.getRequestId());
            startActivity(intent);
        });
        rvPendingRequests.setLayoutManager(new LinearLayoutManager(this));
        rvPendingRequests.setAdapter(adapter);
    }

    private void loadPendingRequests() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        com.example.unisync.utils.DataManager.getInstance().getPendingRequests(requests -> {
            pendingRequests.clear();
            if (requests != null) {
                pendingRequests.addAll(requests);
            }
            adapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);

            boolean empty = pendingRequests.isEmpty();
            layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            rvPendingRequests.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
    }
}
