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
import com.example.unisync.adapters.EventCardAdapter;
import com.example.unisync.models.Event;
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
 * MyEventsActivity — displays all events created by the logged-in organizer.
 * Features status filtering chips and direct navigation to create/view event details.
 */
public class MyEventsActivity extends AppCompatActivity {

    private RecyclerView rvMyEvents;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;
    private ChipGroup chipGroupStatus;
    private FloatingActionButton fabCreateEvent;

    private SessionManager sessionManager;
    private FirebaseFirestore db;
    private EventCardAdapter adapter;

    private final List<Event> allMyEvents = new ArrayList<>();
    private final List<Event> displayedEvents = new ArrayList<>();
    private String selectedStatus = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

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
        loadMyEvents();
    }

    private void initViews() {
        rvMyEvents = findViewById(R.id.rvMyEvents);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        chipGroupStatus = findViewById(R.id.chipGroupStatus);
        fabCreateEvent = findViewById(R.id.fabCreateEvent);

        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.accent);
        swipeRefresh.setOnRefreshListener(this::loadMyEvents);

        fabCreateEvent.setOnClickListener(v ->
                startActivity(new Intent(this, CreateEventActivity.class)));
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
        adapter = new EventCardAdapter(displayedEvents, event -> {
            Intent intent = new Intent(this, EventDetailActivity.class);
            intent.putExtra("eventId", event.getEventId());
            startActivity(intent);
        });
        rvMyEvents.setLayoutManager(new LinearLayoutManager(this));
        rvMyEvents.setAdapter(adapter);
    }

    private void setupFilterChips() {
        chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null) {
                    selectedStatus = chip.getText().toString();
                    filterEvents();
                }
            }
        });
    }

    private void loadMyEvents() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        com.example.unisync.utils.DataManager.getInstance().getEventsByOrganizer(sessionManager.getUid(), events -> {
            allMyEvents.clear();
            if (events != null) {
                allMyEvents.addAll(events);
            }
            filterEvents();
            progressBar.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);
        });
    }

    private void filterEvents() {
        displayedEvents.clear();
        for (Event event : allMyEvents) {
            if ("All".equalsIgnoreCase(selectedStatus) || selectedStatus.equalsIgnoreCase(event.getStatus())) {
                displayedEvents.add(event);
            }
        }
        adapter.notifyDataSetChanged();
        boolean isEmpty = displayedEvents.isEmpty();
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvMyEvents.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}
