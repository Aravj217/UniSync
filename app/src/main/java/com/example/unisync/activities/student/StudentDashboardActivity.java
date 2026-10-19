package com.example.unisync.activities.student;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.unisync.R;
import com.example.unisync.activities.auth.ProfileActivity;
import com.example.unisync.adapters.EventCardAdapter;
import com.example.unisync.models.Event;
import com.example.unisync.utils.AuthHelper;
import com.example.unisync.utils.FirestoreHelper;
import com.example.unisync.utils.SessionManager;
import com.example.unisync.utils.UIHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * StudentDashboardActivity — the student home screen showing
 * published events with search and category filtering.
 */
public class StudentDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private EditText etSearch;
    private ChipGroup chipGroupCategories;
    private RecyclerView rvEvents;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;

    private SessionManager sessionManager;
    private FirebaseFirestore db;

    private EventCardAdapter adapter;
    private List<Event> allEvents = new ArrayList<>();
    private List<Event> filteredEvents = new ArrayList<>();
    private String selectedCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        sessionManager = new SessionManager(this);
        db = FirestoreHelper.getDb();

        initViews();
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
        tvWelcome = findViewById(R.id.tvWelcome);
        etSearch = findViewById(R.id.etSearch);
        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        rvEvents = findViewById(R.id.rvEvents);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        tvWelcome.setText(String.format("Welcome, %s", sessionManager.getFullName()));

        // Profile
        findViewById(R.id.ivProfile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        // My Assignments
        View cardMyAssignments = findViewById(R.id.cardMyAssignments);
        if (cardMyAssignments != null) {
            cardMyAssignments.setOnClickListener(v ->
                    startActivity(new Intent(this, MyAssignmentsActivity.class)));
        }

        // Pull to refresh
        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.accent);
        swipeRefresh.setOnRefreshListener(this::loadPublishedEvents);
    }

    private void setupRecyclerView() {
        adapter = new EventCardAdapter(filteredEvents, event -> {
            // Open event detail
            Intent intent = new Intent(this, EventViewActivity.class);
            intent.putExtra("eventId", event.getEventId());
            startActivity(intent);
        });
        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        rvEvents.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEvents(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupCategoryFilter() {
        chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null) {
                    selectedCategory = chip.getText().toString();
                    filterEvents(etSearch.getText().toString());
                }
            }
        });
    }

    /**
     * Load only PUBLISHED events from DataManager.
     */
    private void loadPublishedEvents() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        com.example.unisync.utils.DataManager.getInstance().getPublishedEvents(events -> {
            allEvents.clear();
            if (events != null) {
                allEvents.addAll(events);
            }
            filterEvents(etSearch.getText().toString());
            progressBar.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);
        });
    }

    /**
     * Filter events by search text and selected category.
     */
    private void filterEvents(String searchText) {
        filteredEvents.clear();
        String query = searchText.toLowerCase().trim();

        for (Event event : allEvents) {
            // Category filter
            if (!"All".equals(selectedCategory)
                    && !selectedCategory.equalsIgnoreCase(event.getCategory())) {
                continue;
            }

            // Search filter
            if (!query.isEmpty()) {
                boolean matches = false;
                if (event.getEventName() != null
                        && event.getEventName().toLowerCase().contains(query)) {
                    matches = true;
                }
                if (event.getShortDescription() != null
                        && event.getShortDescription().toLowerCase().contains(query)) {
                    matches = true;
                }
                if (event.getVenue() != null
                        && event.getVenue().toLowerCase().contains(query)) {
                    matches = true;
                }
                if (event.getOrganizingTeam() != null
                        && event.getOrganizingTeam().toLowerCase().contains(query)) {
                    matches = true;
                }
                if (!matches) continue;
            }

            filteredEvents.add(event);
        }

        adapter.notifyDataSetChanged();
        layoutEmpty.setVisibility(filteredEvents.isEmpty() ? View.VISIBLE : View.GONE);
        rvEvents.setVisibility(filteredEvents.isEmpty() ? View.GONE : View.VISIBLE);
    }
}
