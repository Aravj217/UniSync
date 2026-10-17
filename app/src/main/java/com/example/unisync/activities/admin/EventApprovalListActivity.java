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
import com.example.unisync.adapters.EventCardAdapter;
import com.example.unisync.models.Event;
import com.example.unisync.utils.FirestoreHelper;
import com.example.unisync.utils.UIHelper;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * EventApprovalListActivity — displays events pending Admin approval.
 */
public class EventApprovalListActivity extends AppCompatActivity {

    private RecyclerView rvPendingEvents;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;

    private FirebaseFirestore db;
    private EventCardAdapter adapter;
    private final List<Event> pendingEvents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_approval_list);

        db = FirestoreHelper.getDb();

        initViews();
        setupToolbar();
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPendingEvents();
    }

    private void initViews() {
        rvPendingEvents = findViewById(R.id.rvPendingEvents);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.accent);
        swipeRefresh.setOnRefreshListener(this::loadPendingEvents);
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
        adapter = new EventCardAdapter(pendingEvents, event -> {
            Intent intent = new Intent(this, EventReviewActivity.class);
            intent.putExtra("eventId", event.getEventId());
            startActivity(intent);
        });
        rvPendingEvents.setLayoutManager(new LinearLayoutManager(this));
        rvPendingEvents.setAdapter(adapter);
    }

    private void loadPendingEvents() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        com.example.unisync.utils.DataManager.getInstance().getPendingEvents(events -> {
            pendingEvents.clear();
            if (events != null) {
                pendingEvents.addAll(events);
            }
            adapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);

            boolean empty = pendingEvents.isEmpty();
            layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            rvPendingEvents.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
    }
}
