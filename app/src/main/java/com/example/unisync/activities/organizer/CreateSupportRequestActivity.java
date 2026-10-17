package com.example.unisync.activities.organizer;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unisync.R;
import com.example.unisync.adapters.RequestPersonAdapter;
import com.example.unisync.models.Event;
import com.example.unisync.models.RequestPerson;
import com.example.unisync.models.TeamRequest;
import com.example.unisync.utils.FirestoreHelper;
import com.example.unisync.utils.SessionManager;
import com.example.unisync.utils.UIHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * CreateSupportRequestActivity — enables organizers to request people
 * for event preparation, logistics, technical work, and stage management.
 */
public class CreateSupportRequestActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etReason, etRequiredDate, etPeopleCount;
    private TextInputEditText etWorkLocation, etInstructions;
    private AutoCompleteTextView actvCategory, actvRelatedEvent;
    private RecyclerView rvPeopleList;
    private TextView tvNoPeopleHint;
    private MaterialButton btnAddPerson, btnSaveDraft, btnSubmit;
    private ProgressBar progressBar;

    private SessionManager sessionManager;
    private FirebaseFirestore db;
    private RequestPersonAdapter peopleAdapter;
    private final List<RequestPerson> rosterList = new ArrayList<>();
    private final List<Event> organizerEvents = new ArrayList<>();

    private String relatedEventId = null;

    private static final String[] CATEGORIES = {
            TeamRequest.CAT_EVENT_PREP,
            TeamRequest.CAT_DECORATION,
            TeamRequest.CAT_STAGE_SETUP,
            TeamRequest.CAT_TECH_SUPPORT,
            TeamRequest.CAT_REGISTRATION,
            TeamRequest.CAT_CROWD,
            TeamRequest.CAT_LOGISTICS,
            TeamRequest.CAT_PHOTOGRAPHY,
            TeamRequest.CAT_HOSPITALITY,
            TeamRequest.CAT_EQUIPMENT,
            TeamRequest.CAT_CLEANING,
            TeamRequest.CAT_OTHER
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_support_request);

        sessionManager = new SessionManager(this);
        db = FirestoreHelper.getDb();

        if (getIntent().hasExtra("relatedEventId")) {
            relatedEventId = getIntent().getStringExtra("relatedEventId");
        }

        initViews();
        setupToolbar();
        setupCategoryDropdown();
        setupDatePicker();
        setupRosterRecyclerView();
        loadOrganizerEvents();
    }

    private void initViews() {
        etTitle = findViewById(R.id.etTitle);
        actvCategory = findViewById(R.id.actvCategory);
        etReason = findViewById(R.id.etReason);
        actvRelatedEvent = findViewById(R.id.actvRelatedEvent);
        etRequiredDate = findViewById(R.id.etRequiredDate);
        etPeopleCount = findViewById(R.id.etPeopleCount);
        etWorkLocation = findViewById(R.id.etWorkLocation);
        etInstructions = findViewById(R.id.etInstructions);
        rvPeopleList = findViewById(R.id.rvPeopleList);
        tvNoPeopleHint = findViewById(R.id.tvNoPeopleHint);
        btnAddPerson = findViewById(R.id.btnAddPerson);
        btnSaveDraft = findViewById(R.id.btnSaveDraft);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);

        btnAddPerson.setOnClickListener(v -> showAddPersonDialog());
        btnSaveDraft.setOnClickListener(v -> saveRequest(TeamRequest.STATUS_DRAFT));
        btnSubmit.setOnClickListener(v -> saveRequest(TeamRequest.STATUS_PENDING));
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

    private void setupCategoryDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                CATEGORIES
        );
        actvCategory.setAdapter(adapter);
    }

    private void setupDatePicker() {
        etRequiredDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        String date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                        etRequiredDate.setText(date);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dialog.show();
        });
    }

    private void setupRosterRecyclerView() {
        peopleAdapter = new RequestPersonAdapter(rosterList, true, position -> {
            rosterList.remove(position);
            peopleAdapter.notifyItemRemoved(position);
            updateRosterVisibility();
        });
        rvPeopleList.setLayoutManager(new LinearLayoutManager(this));
        rvPeopleList.setAdapter(peopleAdapter);
        updateRosterVisibility();
    }

    private void updateRosterVisibility() {
        boolean hasPeople = !rosterList.isEmpty();
        tvNoPeopleHint.setVisibility(hasPeople ? View.GONE : View.VISIBLE);
        rvPeopleList.setVisibility(hasPeople ? View.VISIBLE : View.GONE);
    }

    private void loadOrganizerEvents() {
        com.example.unisync.utils.DataManager.getInstance().getEventsByOrganizer(sessionManager.getUid(), events -> {
            organizerEvents.clear();
            List<String> eventTitles = new ArrayList<>();
            eventTitles.add("None / General Request");

            int selectedIndex = 0;
            int i = 1;
            if (events != null) {
                for (Event event : events) {
                    organizerEvents.add(event);
                    eventTitles.add(event.getEventName());
                    if (relatedEventId != null && event.getEventId().equals(relatedEventId)) {
                        selectedIndex = i;
                    }
                    i++;
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    eventTitles
            );
            actvRelatedEvent.setAdapter(adapter);

            if (selectedIndex > 0 && selectedIndex < eventTitles.size()) {
                actvRelatedEvent.setText(eventTitles.get(selectedIndex), false);
            }
        });
    }

    private void showAddPersonDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_roster_person, null);
        TextInputEditText etPersonName = dialogView.findViewById(R.id.etDialogPersonName);
        TextInputEditText etDuty = dialogView.findViewById(R.id.etDialogDuty);
        TextInputEditText etCourse = dialogView.findViewById(R.id.etDialogCourse);
        TextInputEditText etYear = dialogView.findViewById(R.id.etDialogYear);
        TextInputEditText etDept = dialogView.findViewById(R.id.etDialogDepartment);
        TextInputEditText etRoll = dialogView.findViewById(R.id.etDialogRoll);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = UIHelper.getText(etPersonName);
                    String duty = UIHelper.getText(etDuty);
                    if (TextUtils.isEmpty(name)) {
                        UIHelper.showToast(this, "Please enter a name");
                        return;
                    }
                    RequestPerson person = new RequestPerson();
                    person.setFullName(name);
                    person.setAssignedDuty(duty.isEmpty() ? "General Support" : duty);
                    person.setCourse(UIHelper.getText(etCourse));
                    person.setYear(UIHelper.getText(etYear));
                    person.setDepartment(UIHelper.getText(etDept));
                    person.setRollNumber(UIHelper.getText(etRoll));

                    rosterList.add(person);
                    peopleAdapter.notifyItemInserted(rosterList.size() - 1);
                    updateRosterVisibility();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveRequest(String status) {
        String title = UIHelper.getText(etTitle);
        String category = actvCategory.getText().toString().trim();
        String reason = UIHelper.getText(etReason);
        String date = UIHelper.getText(etRequiredDate);
        String countStr = UIHelper.getText(etPeopleCount);
        String location = UIHelper.getText(etWorkLocation);
        String instructions = UIHelper.getText(etInstructions);

        if (TeamRequest.STATUS_PENDING.equals(status)) {
            if (TextUtils.isEmpty(title)) {
                etTitle.setError("Title is required");
                etTitle.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(category)) {
                actvCategory.setError("Category is required");
                actvCategory.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(reason)) {
                etReason.setError("Reason is required");
                etReason.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(date)) {
                UIHelper.showToast(this, "Please select required date");
                return;
            }
        } else {
            if (TextUtils.isEmpty(title)) {
                etTitle.setError("Title is required for drafts");
                etTitle.requestFocus();
                return;
            }
        }

        int peopleCount = 0;
        try {
            if (!TextUtils.isEmpty(countStr)) {
                peopleCount = Integer.parseInt(countStr);
            }
        } catch (NumberFormatException ignored) {}

        if (peopleCount == 0 && !rosterList.isEmpty()) {
            peopleCount = rosterList.size();
        }

        setLoading(true);

        TeamRequest request = new TeamRequest();
        String requestId = db.collection(TeamRequest.COLLECTION_NAME).document().getId();
        request.setRequestId(requestId);
        request.setTitle(title);
        request.setCategory(category.isEmpty() ? TeamRequest.CAT_OTHER : category);
        request.setReason(reason);
        request.setDescription(reason);
        request.setRequiredDate(date);
        request.setPeopleRequired(peopleCount);
        request.setWorkLocation(location);
        request.setAdditionalInstructions(instructions);
        request.setStatus(status);
        request.setOrganizerId(sessionManager.getUid());

        // Check if an event was selected
        String selectedEventName = actvRelatedEvent.getText().toString();
        for (Event event : organizerEvents) {
            if (event.getEventName().equalsIgnoreCase(selectedEventName)) {
                request.setRelatedEventId(event.getEventId());
                break;
            }
        }

        // Determine mode based on roster
        if (rosterList.isEmpty()) {
            request.setRequestMode(TeamRequest.MODE_ROLE_BASED);
        } else if (peopleCount > rosterList.size()) {
            request.setRequestMode(TeamRequest.MODE_MIXED);
        } else {
            request.setRequestMode(TeamRequest.MODE_PRESELECTED);
        }

        com.example.unisync.utils.DataManager.getInstance().saveTeamRequest(request, rosterList, new com.example.unisync.utils.DataManager.OnCompleteListener() {
            @Override
            public void onSuccess() {
                setLoading(false);
                String msg = TeamRequest.STATUS_PENDING.equals(status)
                        ? "Support request submitted for Admin review!"
                        : "Support request draft saved!";
                UIHelper.showToast(CreateSupportRequestActivity.this, msg);
                finish();
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                String msg = TeamRequest.STATUS_PENDING.equals(status)
                        ? "Support request submitted for Admin review!"
                        : "Support request draft saved!";
                UIHelper.showToast(CreateSupportRequestActivity.this, msg);
                finish();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSaveDraft.setEnabled(!loading);
        btnSubmit.setEnabled(!loading);
    }
}
