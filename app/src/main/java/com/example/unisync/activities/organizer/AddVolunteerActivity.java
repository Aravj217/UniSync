package com.example.unisync.activities.organizer;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.unisync.R;
import com.example.unisync.models.Event;
import com.example.unisync.models.User;
import com.example.unisync.models.VolunteerAssignment;
import com.example.unisync.utils.DataManager;
import com.example.unisync.utils.SessionManager;
import com.example.unisync.utils.UIHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * AddVolunteerActivity — assign registered student or teacher to event duties by registered email.
 * Every assignment is created in PENDING status for mandatory Admin approval.
 */
public class AddVolunteerActivity extends AppCompatActivity {

    private AutoCompleteTextView actvEvent;
    private TextView tvEventInfo;

    private TextInputLayout tilEmailSearch;
    private TextInputEditText etEmailSearch;
    private MaterialButton btnSearchUser;
    private TextView tvSearchError;

    private LinearLayout layoutUserMatch;
    private TextView tvFoundName, tvFoundUserType, tvFoundEmail, tvFoundAcademic, tvFoundDepartment;

    private TextInputLayout tilDuty, tilDate, tilStartTime, tilEndTime, tilVenue, tilInstructions;
    private TextInputEditText etDuty, etDate, etStartTime, etEndTime, etVenue, etInstructions;
    private TextView tvOverlapWarning;

    private MaterialButton btnSaveVolunteer;
    private ProgressBar progressBar;

    private SessionManager sessionManager;
    private DataManager dataManager;

    private final List<Event> organizerEvents = new ArrayList<>();
    private Event selectedEvent = null;
    private User selectedUser = null;
    private String preselectedEventId = null;
    private String existingAssignmentId = null;
    private VolunteerAssignment existingAssignment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_volunteer);

        sessionManager = new SessionManager(this);
        dataManager = DataManager.getInstance();

        if (getIntent().hasExtra("eventId")) {
            preselectedEventId = getIntent().getStringExtra("eventId");
        }
        if (getIntent().hasExtra("assignmentId")) {
            existingAssignmentId = getIntent().getStringExtra("assignmentId");
        } else if (getIntent().hasExtra("volunteerId")) {
            existingAssignmentId = getIntent().getStringExtra("volunteerId");
        }

        initViews();
        setupToolbar();
        setupDateTimePickers();
        loadEvents();

        if (existingAssignmentId != null) {
            loadAssignmentForEdit();
        }
    }

    private void initViews() {
        actvEvent = findViewById(R.id.actvEvent);
        tvEventInfo = findViewById(R.id.tvEventInfo);

        tilEmailSearch = findViewById(R.id.tilEmailSearch);
        etEmailSearch = findViewById(R.id.etEmailSearch);
        btnSearchUser = findViewById(R.id.btnSearchUser);
        tvSearchError = findViewById(R.id.tvSearchError);

        layoutUserMatch = findViewById(R.id.layoutUserMatch);
        tvFoundName = findViewById(R.id.tvFoundName);
        tvFoundUserType = findViewById(R.id.tvFoundUserType);
        tvFoundEmail = findViewById(R.id.tvFoundEmail);
        tvFoundAcademic = findViewById(R.id.tvFoundAcademic);
        tvFoundDepartment = findViewById(R.id.tvFoundDepartment);

        tilDuty = findViewById(R.id.tilDuty);
        tilDate = findViewById(R.id.tilDate);
        tilStartTime = findViewById(R.id.tilStartTime);
        tilEndTime = findViewById(R.id.tilEndTime);
        tilVenue = findViewById(R.id.tilVenue);
        tilInstructions = findViewById(R.id.tilInstructions);

        etDuty = findViewById(R.id.etDuty);
        etDate = findViewById(R.id.etDate);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);
        etVenue = findViewById(R.id.etVenue);
        etInstructions = findViewById(R.id.etInstructions);
        tvOverlapWarning = findViewById(R.id.tvOverlapWarning);

        btnSaveVolunteer = findViewById(R.id.btnSaveVolunteer);
        progressBar = findViewById(R.id.progressBar);

        btnSearchUser.setOnClickListener(v -> performUserSearch());
        btnSaveVolunteer.setOnClickListener(v -> submitAssignment());

        // Live check for the overlapping assignment times
        TextWatcher overlapWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkOverlap();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };
        etDate.addTextChangedListener(overlapWatcher);
        etStartTime.addTextChangedListener(overlapWatcher);
        etEndTime.addTextChangedListener(overlapWatcher);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(existingAssignmentId != null ? "Edit Volunteer Assignment" : "Assign Volunteers");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupDateTimePickers() {
        etDate.setOnClickListener(v -> UIHelper.showDatePicker(this, etDate));
        etStartTime.setOnClickListener(v -> UIHelper.showTimePicker(this, etStartTime));
        etEndTime.setOnClickListener(v -> UIHelper.showTimePicker(this, etEndTime));
    }

    private void loadEvents() {
        dataManager.getEventsByOrganizer(sessionManager.getUid(), events -> {
            organizerEvents.clear();
            List<String> eventNames = new ArrayList<>();
            int selectedIdx = -1;
            int i = 0;
            if (events != null) {
                for (Event event : events) {
                    organizerEvents.add(event);
                    eventNames.add(event.getEventName());
                    if (preselectedEventId != null && event.getEventId().equals(preselectedEventId)) {
                        selectedIdx = i;
                        selectedEvent = event;
                    }
                    i++;
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    eventNames
            );
            actvEvent.setAdapter(adapter);

            if (selectedIdx >= 0 && selectedIdx < eventNames.size()) {
                actvEvent.setText(eventNames.get(selectedIdx), false);
                populateEventDetails(selectedEvent);
            }

            actvEvent.setOnItemClickListener((parent, view, position, id) -> {
                if (position >= 0 && position < organizerEvents.size()) {
                    selectedEvent = organizerEvents.get(position);
                    populateEventDetails(selectedEvent);
                    checkOverlap();
                }
            });
        });
    }

    private void populateEventDetails(Event event) {
        if (event == null) return;
        tvEventInfo.setText(String.format("Date: %s | Venue: %s",
                UIHelper.formatDateForDisplay(event.getEventDate()),
                event.getVenue() != null ? event.getVenue() : "TBA"));

        // Pre-fill date and venue if empty
        if (TextUtils.isEmpty(UIHelper.getText(etDate)) && event.getEventDate() != null) {
            etDate.setText(event.getEventDate());
        }
        if (TextUtils.isEmpty(UIHelper.getText(etVenue)) && event.getVenue() != null) {
            etVenue.setText(event.getVenue());
        }
        if (TextUtils.isEmpty(UIHelper.getText(etStartTime)) && event.getStartTime() != null) {
            etStartTime.setText(event.getStartTime());
        }
        if (TextUtils.isEmpty(UIHelper.getText(etEndTime)) && event.getEndTime() != null) {
            etEndTime.setText(event.getEndTime());
        }
    }

    private void performUserSearch() {
        String email = UIHelper.getText(etEmailSearch);
        if (TextUtils.isEmpty(email)) {
            tilEmailSearch.setError("Please enter the user's registered email");
            return;
        }
        tilEmailSearch.setError(null);
        tvSearchError.setVisibility(View.GONE);
        layoutUserMatch.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        dataManager.findUserByEmail(email, new DataManager.OnObjectListener<User>() {
            @Override
            public void onSuccess(User user) {
                progressBar.setVisibility(View.GONE);
                if (user == null || !user.isActive()) {
                    tvSearchError.setText("User account is inactive or not found.");
                    tvSearchError.setVisibility(View.VISIBLE);
                    selectedUser = null;
                    return;
                }
                selectedUser = user;
                displayMatchedUser(user);
                checkOverlap();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                selectedUser = null;
                tvSearchError.setText("No registered user found with email: " + email + ".\nStudents and teachers must create an account first.");
                tvSearchError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void displayMatchedUser(User user) {
        layoutUserMatch.setVisibility(View.VISIBLE);
        tvFoundName.setText(user.getFullName());
        tvFoundUserType.setText(user.getUserType() != null ? user.getUserType().toUpperCase() : "STUDENT");
        tvFoundEmail.setText(user.getEmail());

        StringBuilder academic = new StringBuilder();
        if (user.getCourse() != null && !user.getCourse().isEmpty()) {
            academic.append(user.getCourse());
        }
        if (user.getYear() != null && !user.getYear().isEmpty()) {
            if (academic.length() > 0) academic.append(" · ");
            academic.append(user.getYear());
        }
        if (user.getRollNumber() != null && !user.getRollNumber().isEmpty()) {
            if (academic.length() > 0) academic.append(" · Roll: ");
            academic.append(user.getRollNumber());
        }
        tvFoundAcademic.setText(academic.length() > 0 ? academic.toString() : "NMIMS Member");
        tvFoundDepartment.setText(user.getDepartment() != null ? "Department: " + user.getDepartment() : "Department: MPSTME");
    }

    private void checkOverlap() {
        if (selectedUser == null) {
            tvOverlapWarning.setVisibility(View.GONE);
            return;
        }
        String date = UIHelper.getText(etDate);
        String start = UIHelper.getText(etStartTime);
        String end = UIHelper.getText(etEndTime);
        String warning = dataManager.checkTimeOverlapWarning(selectedUser.getUid(), date, start, end, existingAssignmentId);
        if (warning != null) {
            tvOverlapWarning.setText(warning);
            tvOverlapWarning.setVisibility(View.VISIBLE);
        } else {
            tvOverlapWarning.setVisibility(View.GONE);
        }
    }

    private void loadAssignmentForEdit() {
        progressBar.setVisibility(View.VISIBLE);
        dataManager.getVolunteerAssignmentById(existingAssignmentId, new DataManager.OnObjectListener<VolunteerAssignment>() {
            @Override
            public void onSuccess(VolunteerAssignment assignment) {
                progressBar.setVisibility(View.GONE);
                existingAssignment = assignment;
                if (existingAssignment != null) {
                    preselectedEventId = existingAssignment.getEventId();
                    etEmailSearch.setText(existingAssignment.getUserEmail());
                    etDuty.setText(existingAssignment.getAssignedDuty());
                    etDate.setText(existingAssignment.getEventDate());
                    etStartTime.setText(existingAssignment.getStartTime());
                    etEndTime.setText(existingAssignment.getEndTime());
                    etVenue.setText(existingAssignment.getVenue());
                    etInstructions.setText(existingAssignment.getAdditionalInstructions());

                    // Create user proxy from assignment info
                    User user = new User();
                    user.setUid(existingAssignment.getUserId());
                    user.setFullName(existingAssignment.getUserName());
                    user.setEmail(existingAssignment.getUserEmail());
                    user.setUserType(existingAssignment.getUserType());
                    user.setCourse(existingAssignment.getCourse());
                    user.setYear(existingAssignment.getYear());
                    user.setDepartment(existingAssignment.getDepartment());
                    user.setRollNumber(existingAssignment.getRollNumber());
                    selectedUser = user;
                    displayMatchedUser(user);

                    btnSaveVolunteer.setText(VolunteerAssignment.STATUS_NEEDS_CORRECTION.equals(existingAssignment.getStatus())
                            || VolunteerAssignment.STATUS_REJECTED.equals(existingAssignment.getStatus())
                            ? "Correct and Resubmit Assignment" : "Update Assignment");
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                UIHelper.showToast(AddVolunteerActivity.this, "Failed to load assignment details");
            }
        });
    }

    private void submitAssignment() {
        if (selectedEvent == null) {
            actvEvent.setError("Please select an event");
            UIHelper.showToast(this, "Please select an event first");
            return;
        }

        if (selectedUser == null) {
            tilEmailSearch.setError("Please search and select a registered user");
            UIHelper.showToast(this, "Please search and select a registered user by email");
            return;
        }

        String duty = UIHelper.getText(etDuty);
        String date = UIHelper.getText(etDate);
        String startTime = UIHelper.getText(etStartTime);
        String endTime = UIHelper.getText(etEndTime);
        String venue = UIHelper.getText(etVenue);
        String instructions = UIHelper.getText(etInstructions);

        if (TextUtils.isEmpty(duty)) {
            tilDuty.setError("Assigned duty is required");
            etDuty.requestFocus();
            return;
        }
        tilDuty.setError(null);

        if (TextUtils.isEmpty(date)) {
            tilDate.setError("Assignment date is required");
            return;
        }
        tilDate.setError(null);

        if (TextUtils.isEmpty(startTime)) {
            tilStartTime.setError("Start time required");
            return;
        }
        tilStartTime.setError(null);

        if (TextUtils.isEmpty(endTime)) {
            tilEndTime.setError("End time required");
            return;
        }
        tilEndTime.setError(null);

        if (TextUtils.isEmpty(venue)) {
            tilVenue.setError("Venue is required");
            etVenue.requestFocus();
            return;
        }
        tilVenue.setError(null);

        // Prevent accidental duplicate assignment (same user, event, duty)
        if (dataManager.hasDuplicateAssignment(selectedUser.getUid(), selectedEvent.getEventId(), duty, existingAssignmentId)) {
            UIHelper.showToast(this, "This user is already assigned to duty '" + duty + "' for this event.");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSaveVolunteer.setEnabled(false);

        VolunteerAssignment assignment = existingAssignment != null ? existingAssignment : new VolunteerAssignment();
        if (assignment.getAssignmentId() == null) {
            assignment.setAssignmentId("asg_" + System.currentTimeMillis());
        }
        assignment.setEventId(selectedEvent.getEventId());
        assignment.setEventName(selectedEvent.getEventName());
        assignment.setUserId(selectedUser.getUid()); // Permanent authoritative UID
        assignment.setUserName(selectedUser.getFullName());
        assignment.setUserEmail(selectedUser.getEmail());
        assignment.setUserType(selectedUser.getUserType());
        assignment.setCourse(selectedUser.getCourse());
        assignment.setYear(selectedUser.getYear());
        assignment.setDepartment(selectedUser.getDepartment());
        assignment.setRollNumber(selectedUser.getRollNumber());
        assignment.setAssignedDuty(duty);
        assignment.setEventDate(date);
        assignment.setStartTime(startTime);
        assignment.setEndTime(endTime);
        assignment.setVenue(venue);
        assignment.setOrganizerId(sessionManager.getUid());
        assignment.setOrganizerName(sessionManager.getFullName());
        assignment.setAdditionalInstructions(instructions);
        assignment.setStatus(VolunteerAssignment.STATUS_ACTIVE); // Immediately active without Admin approval
        assignment.setRejectionReason(null);

        dataManager.saveVolunteerAssignment(assignment, new DataManager.OnCompleteListener() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                UIHelper.showToast(AddVolunteerActivity.this, "Volunteer assigned successfully! Assignment is now active.");
                finish();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                btnSaveVolunteer.setEnabled(true);
                UIHelper.showToast(AddVolunteerActivity.this, "Error: " + error);
            }
        });
    }
}
