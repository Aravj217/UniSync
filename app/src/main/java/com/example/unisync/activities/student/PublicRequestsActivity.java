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
import com.example.unisync.adapters.TeamRequestAdapter;
import com.example.unisync.models.RequestPerson;
import com.example.unisync.models.TeamRequest;
import com.example.unisync.models.Volunteer;
import com.example.unisync.utils.FirestoreHelper;
import com.example.unisync.utils.SessionManager;
import com.example.unisync.utils.UIHelper;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * PublicRequestsActivity — shows students approved team support requests that need volunteers.
 */
public class PublicRequestsActivity extends AppCompatActivity {

    private RecyclerView rvPublicRequests;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;

    private SessionManager sessionManager;
    private FirebaseFirestore db;
    private TeamRequestAdapter adapter;
    private final List<TeamRequest> openRequests = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_requests);

        sessionManager = new SessionManager(this);
        db = FirestoreHelper.getDb();

        initViews();
        setupToolbar();
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOpenRequests();
    }

    private void initViews() {
        rvPublicRequests = findViewById(R.id.rvPublicRequests);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        swipeRefresh.setColorSchemeResources(R.color.accent, R.color.primary);
        swipeRefresh.setOnRefreshListener(this::loadOpenRequests);
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
        adapter = new TeamRequestAdapter(openRequests, request -> {
            UIHelper.showConfirmDialog(this, "Sign Up as Volunteer",
                    "Do you want to volunteer for: " + request.getTitle() + "?\nDuty: " + request.getCategory()
                            + "\nDate: " + UIHelper.formatDateForDisplay(request.getRequiredDate()),
                    () -> volunteerForRequest(request));
        });
        rvPublicRequests.setLayoutManager(new LinearLayoutManager(this));
        rvPublicRequests.setAdapter(adapter);
    }

    private void loadOpenRequests() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        com.example.unisync.utils.DataManager.getInstance().getApprovedRequests(requests -> {
            openRequests.clear();
            if (requests != null) {
                openRequests.addAll(requests);
            }
            adapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);

            boolean empty = openRequests.isEmpty();
            layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            rvPublicRequests.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
    }

    private void volunteerForRequest(TeamRequest request) {
        progressBar.setVisibility(View.VISIBLE);

        RequestPerson person = new RequestPerson();
        person.setFullName(sessionManager.getFullName());
        person.setCourse(sessionManager.getCourse());
        person.setYear(sessionManager.getYear());
        person.setDepartment(sessionManager.getDepartment());
        person.setRollNumber(sessionManager.getRollNumber());
        person.setAssignedDuty(request.getCategory());
        person.setTeam("Support Team");

        com.example.unisync.utils.DataManager.getInstance().volunteerForRequest(request, person, new com.example.unisync.utils.DataManager.OnCompleteListener() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                UIHelper.showToast(PublicRequestsActivity.this, "Signed up successfully! Organizer will be informed.");
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                UIHelper.showToast(PublicRequestsActivity.this, "Signed up successfully! Organizer will be informed.");
            }
        });
    }
}
