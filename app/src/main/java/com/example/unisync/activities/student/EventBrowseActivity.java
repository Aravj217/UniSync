package com.example.unisync.activities.student;

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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * EventBrowseActivity — dedicated screen for students to browse and search published campus events.
 */
public class EventBrowseActivity extends AppCompatActivity {

    private EditText etSearchBrowse;
    private ChipGroup chipGroupBrowseCategories;
    private RecyclerView rvBrowseEvents;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;

    private FirebaseFirestore db;
    private EventCardAdapter adapter;

    private final List<Event> allPublishedEvents = new ArrayList<>();
    private final List<Event> displayedEvents = new ArrayList<>();
    private String selectedCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_browse);

        db = FirestoreHelper.getDb();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSearch();
        setupCategoryFilter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPublishedEvents();
    }

    private void initViews() {
        etSearchBrowse = findViewById(R.id.etSearchBrowse);
        chipGroupBrowseCategories = findViewById(R.id.chipGroupBrowseCategories);
        rvBrowseEvents = findViewById(R.id.rvBrowseEvents);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.accent);
        swipeRefresh.setOnRefreshListener(this::loadPublishedEvents);
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
            Intent intent = new Intent(this, EventViewActivity.class);
            intent.putExtra("eventId", event.getEventId());
            startActivity(intent);
        });
        rvBrowseEvents.setLayoutManager(new LinearLayoutManager(this));
        rvBrowseEvents.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearchBrowse.addTextChangedListener(new TextWatcher() {
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

    private void setupCategoryFilter() {
        chipGroupBrowseCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null) {
                    selectedCategory = chip.getText().toString();
                    filterEvents();
                }
            }
        });
    }

    private void loadPublishedEvents() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        com.example.unisync.utils.DataManager.getInstance().getPublishedEvents(events -> {
            allPublishedEvents.clear();
            if (events != null) {
                allPublishedEvents.addAll(events);
            }
            filterEvents();
            progressBar.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);
        });
    }

    private void filterEvents() {
        displayedEvents.clear();
        String query = etSearchBrowse.getText().toString().toLowerCase().trim();

        for (Event event : allPublishedEvents) {
            // Category check
            if (!"All".equalsIgnoreCase(selectedCategory)
                    && !selectedCategory.equalsIgnoreCase(event.getCategory())) {
                continue;
            }

            // Search query check
            if (!query.isEmpty()) {
                boolean matches = false;
                if (event.getEventName() != null && event.getEventName().toLowerCase().contains(query)) matches = true;
                if (event.getShortDescription() != null && event.getShortDescription().toLowerCase().contains(query)) matches = true;
                if (event.getVenue() != null && event.getVenue().toLowerCase().contains(query)) matches = true;
                if (event.getOrganizingTeam() != null && event.getOrganizingTeam().toLowerCase().contains(query)) matches = true;
                if (!matches) continue;
            }

            displayedEvents.add(event);
        }

        adapter.notifyDataSetChanged();
        boolean empty = displayedEvents.isEmpty();
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvBrowseEvents.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}
