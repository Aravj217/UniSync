package com.example.unisync.activities.admin;

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
import com.example.unisync.adapters.EventCardAdapter;
import com.example.unisync.models.Event;
import com.example.unisync.utils.FirestoreHelper;
import com.example.unisync.utils.UIHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * AllEventsActivity — provides Admins a central dashboard view of all events across campus.
 */
public class AllEventsActivity extends AppCompatActivity {

    private EditText etSearchAllEvents;
    private ChipGroup chipGroupStatus;
    private RecyclerView rvAllEvents;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;

    private FirebaseFirestore db;
    private EventCardAdapter adapter;

    private final List<Event> allEventsList = new ArrayList<>();
    private final List<Event> displayedEvents = new ArrayList<>();
    private String selectedStatus = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_events);

        db = FirestoreHelper.getDb();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSearch();
        setupStatusFilter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllEvents();
    }

    private void initViews() {
        etSearchAllEvents = findViewById(R.id.etSearchAllEvents);
        chipGroupStatus = findViewById(R.id.chipGroupStatus);
        rvAllEvents = findViewById(R.id.rvAllEvents);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.accent);
        swipeRefresh.setOnRefreshListener(this::loadAllEvents);
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
            Intent intent = new Intent(this, EventReviewActivity.class);
            intent.putExtra("eventId", event.getEventId());
            startActivity(intent);
        });
        rvAllEvents.setLayoutManager(new LinearLayoutManager(this));
        rvAllEvents.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearchAllEvents.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEvents();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupStatusFilter() {
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

    private void loadAllEvents() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        com.example.unisync.utils.DataManager.getInstance().getEvents(events -> {
            allEventsList.clear();
            if (events != null) {
                allEventsList.addAll(events);
            }
            filterEvents();
            progressBar.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);
        });
    }

    private void filterEvents() {
        displayedEvents.clear();
        String query = etSearchAllEvents.getText().toString().toLowerCase().trim();

        for (Event ev : allEventsList) {
            // Status check
            if (!"All".equalsIgnoreCase(selectedStatus) && !selectedStatus.equalsIgnoreCase(ev.getStatus())) {
                continue;
            }

            // Search query check
            if (!query.isEmpty()) {
                boolean matches = false;
                if (ev.getEventName() != null && ev.getEventName().toLowerCase().contains(query)) matches = true;
                if (ev.getCategory() != null && ev.getCategory().toLowerCase().contains(query)) matches = true;
                if (ev.getVenue() != null && ev.getVenue().toLowerCase().contains(query)) matches = true;
                if (ev.getOrganizingTeam() != null && ev.getOrganizingTeam().toLowerCase().contains(query)) matches = true;
                if (!matches) continue;
            }

            displayedEvents.add(ev);
        }

        adapter.notifyDataSetChanged();
        boolean empty = displayedEvents.isEmpty();
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvAllEvents.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}
