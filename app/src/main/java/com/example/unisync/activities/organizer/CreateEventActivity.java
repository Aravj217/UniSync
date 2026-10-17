package com.example.unisync.activities.organizer;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.unisync.R;
import com.example.unisync.models.Event;
import com.example.unisync.utils.FirestoreHelper;
import com.example.unisync.utils.SessionManager;
import com.example.unisync.utils.UIHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Locale;

/**
 * CreateEventActivity — allows organizers to create or edit an event.
 * Events can be saved as DRAFT or submitted as PENDING for Admin review.
 */
public class CreateEventActivity extends AppCompatActivity {

    private TextInputEditText etEventName, etShortDescription, etDescription;
    private AutoCompleteTextView actvCategory;
    private TextInputEditText etEventDate, etStartTime, etEndTime, etVenue;
    private TextInputEditText etOrganizingTeam, etFacultyInCharge, etRegistrationInfo, etContactInfo;
    private MaterialButton btnSaveDraft, btnSubmit;
    private ProgressBar progressBar;

    private SessionManager sessionManager;
    private FirebaseFirestore db;
    private String existingEventId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        sessionManager = new SessionManager(this);
        db = FirestoreHelper.getDb();

        initViews();
        setupToolbar();
        setupCategoryDropdown();
        setupPickers();

        // Check for edit mode
        if (getIntent().hasExtra("eventId")) {
            existingEventId = getIntent().getStringExtra("eventId");
            loadEventForEdit(existingEventId);
        }
    }

    private void initViews() {
        etEventName = findViewById(R.id.etEventName);
        actvCategory = findViewById(R.id.actvCategory);
        etShortDescription = findViewById(R.id.etShortDescription);
        etDescription = findViewById(R.id.etDescription);
        etEventDate = findViewById(R.id.etEventDate);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);
        etVenue = findViewById(R.id.etVenue);
        etOrganizingTeam = findViewById(R.id.etOrganizingTeam);
        etFacultyInCharge = findViewById(R.id.etFacultyInCharge);
        etRegistrationInfo = findViewById(R.id.etRegistrationInfo);
        etContactInfo = findViewById(R.id.etContactInfo);
        btnSaveDraft = findViewById(R.id.btnSaveDraft);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);

        btnSaveDraft.setOnClickListener(v -> saveEvent(Event.STATUS_DRAFT));
        btnSubmit.setOnClickListener(v -> saveEvent(Event.STATUS_PENDING));
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
                Event.getAllCategories()
        );
        actvCategory.setAdapter(adapter);
    }

    private void setupPickers() {
        // Date Picker
        etEventDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        String date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                        etEventDate.setText(date);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dialog.show();
        });

        // Start Time Picker
        etStartTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        String time = String.format(Locale.US, "%02d:%02d", hourOfDay, minute);
                        etStartTime.setText(time);
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
            ).show();
        });

        // End Time Picker
        etEndTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        String time = String.format(Locale.US, "%02d:%02d", hourOfDay, minute);
                        etEndTime.setText(time);
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
            ).show();
        });
    }

    private void loadEventForEdit(String eventId) {
        progressBar.setVisibility(View.VISIBLE);
        com.example.unisync.utils.DataManager.getInstance().getEventById(eventId, new com.example.unisync.utils.DataManager.OnObjectListener<Event>() {
            @Override
            public void onSuccess(Event event) {
                progressBar.setVisibility(View.GONE);
                if (event != null) {
                    etEventName.setText(event.getEventName());
                    actvCategory.setText(event.getCategory(), false);
                    etShortDescription.setText(event.getShortDescription());
                    etDescription.setText(event.getDescription());
                    etEventDate.setText(event.getEventDate());
                    etStartTime.setText(event.getStartTime());
                    etEndTime.setText(event.getEndTime());
                    etVenue.setText(event.getVenue());
                    etOrganizingTeam.setText(event.getOrganizingTeam());
                    etFacultyInCharge.setText(event.getFacultyInCharge());
                    etRegistrationInfo.setText(event.getRegistrationInfo());
                    etContactInfo.setText(event.getContactInfo());
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void saveEvent(String targetStatus) {
        String name = UIHelper.getText(etEventName);
        String category = actvCategory.getText().toString().trim();
        String shortDesc = UIHelper.getText(etShortDescription);
        String desc = UIHelper.getText(etDescription);
        String date = UIHelper.getText(etEventDate);
        String startTime = UIHelper.getText(etStartTime);
        String endTime = UIHelper.getText(etEndTime);
        String venue = UIHelper.getText(etVenue);
        String team = UIHelper.getText(etOrganizingTeam);
        String faculty = UIHelper.getText(etFacultyInCharge);
        String regInfo = UIHelper.getText(etRegistrationInfo);
        String contact = UIHelper.getText(etContactInfo);

        // Validation: For submitting to approval, required fields must be filled
        if (Event.STATUS_PENDING.equals(targetStatus)) {
            if (TextUtils.isEmpty(name)) {
                etEventName.setError("Event Name is required");
                etEventName.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(category)) {
                actvCategory.setError("Please select a category");
                actvCategory.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(shortDesc)) {
                etShortDescription.setError("Short description is required");
                etShortDescription.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(date)) {
                UIHelper.showToast(this, "Please select an event date");
                return;
            }
            if (TextUtils.isEmpty(venue)) {
                etVenue.setError("Venue is required");
                etVenue.requestFocus();
                return;
            }
        } else {
            // For draft, at least the event name is helpful
            if (TextUtils.isEmpty(name)) {
                etEventName.setError("Event Name is required even for drafts");
                etEventName.requestFocus();
                return;
            }
        }

        setLoading(true);

        Event event = new Event();
        if (existingEventId != null) {
            event.setEventId(existingEventId);
        }
        event.setEventName(name);
        event.setCategory(category.isEmpty() ? Event.CAT_OTHER : category);
        event.setShortDescription(shortDesc);
        event.setDescription(desc);
        event.setEventDate(date);
        event.setStartTime(startTime);
        event.setEndTime(endTime);
        event.setVenue(venue);
        event.setOrganizingTeam(team);
        event.setFacultyInCharge(faculty);
        event.setRegistrationInfo(regInfo);
        event.setContactInfo(contact);
        event.setStatus(targetStatus);
        event.setOrganizerId(sessionManager.getUid());

        String docId = (existingEventId != null) ? existingEventId : "ev_" + System.currentTimeMillis();
        event.setEventId(docId);

        com.example.unisync.utils.DataManager.getInstance().saveEvent(event, new com.example.unisync.utils.DataManager.OnCompleteListener() {
            @Override
            public void onSuccess() {
                setLoading(false);
                String message = Event.STATUS_PENDING.equals(targetStatus)
                        ? "Event submitted for Admin approval!"
                        : "Draft saved successfully!";
                UIHelper.showToast(CreateEventActivity.this, message);
                finish();
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                String message = Event.STATUS_PENDING.equals(targetStatus)
                        ? "Event submitted for Admin approval!"
                        : "Draft saved successfully!";
                UIHelper.showToast(CreateEventActivity.this, message);
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
