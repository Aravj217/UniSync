package com.example.unisync.activities.admin;

import android.app.AlertDialog;
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
import com.example.unisync.adapters.UserAdapter;
import com.example.unisync.models.AdminLog;
import com.example.unisync.models.User;
import com.example.unisync.utils.FirestoreHelper;
import com.example.unisync.utils.SessionManager;
import com.example.unisync.utils.UIHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * ManageUsersActivity — allows Admins to view all users,
 * change roles (Admin, Organizer, Student), and toggle activation status.
 */
public class ManageUsersActivity extends AppCompatActivity {

    private EditText etSearchUsers;
    private ChipGroup chipGroupRoles;
    private RecyclerView rvUsers;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;

    private SessionManager sessionManager;
    private FirebaseFirestore db;
    private UserAdapter adapter;

    private final List<User> allUsers = new ArrayList<>();
    private final List<User> displayedUsers = new ArrayList<>();
    private String selectedRole = "All Roles";

    private static final String[] ROLES = { User.ROLE_STUDENT, User.ROLE_ORGANIZER, User.ROLE_ADMIN };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        sessionManager = new SessionManager(this);
        db = FirestoreHelper.getDb();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSearch();
        setupRoleFilter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }

    private void initViews() {
        etSearchUsers = findViewById(R.id.etSearchUsers);
        chipGroupRoles = findViewById(R.id.chipGroupRoles);
        rvUsers = findViewById(R.id.rvUsers);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.accent);
        swipeRefresh.setOnRefreshListener(this::loadUsers);
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
        adapter = new UserAdapter(displayedUsers, new UserAdapter.OnUserActionListener() {
            @Override
            public void onChangeRole(User user) {
                showChangeRoleDialog(user);
            }

            @Override
            public void onToggleStatus(User user) {
                toggleUserStatus(user);
            }
        });
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearchUsers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRoleFilter() {
        chipGroupRoles.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null) {
                    selectedRole = chip.getText().toString();
                    filterUsers();
                }
            }
        });
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        com.example.unisync.utils.DataManager.getInstance().getUsers(users -> {
            allUsers.clear();
            if (users != null) {
                allUsers.addAll(users);
            }
            filterUsers();
            progressBar.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);
        });
    }

    private void filterUsers() {
        displayedUsers.clear();
        String query = etSearchUsers.getText().toString().toLowerCase().trim();

        for (User u : allUsers) {
            // Role filter
            if (!"All Roles".equalsIgnoreCase(selectedRole)
                    && !selectedRole.equalsIgnoreCase(u.getRole())) {
                continue;
            }

            // Search filter
            if (!query.isEmpty()) {
                boolean matches = false;
                if (u.getFullName() != null && u.getFullName().toLowerCase().contains(query)) matches = true;
                if (u.getEmail() != null && u.getEmail().toLowerCase().contains(query)) matches = true;
                if (u.getRollNumber() != null && u.getRollNumber().toLowerCase().contains(query)) matches = true;
                if (u.getDepartment() != null && u.getDepartment().toLowerCase().contains(query)) matches = true;
                if (!matches) continue;
            }

            displayedUsers.add(u);
        }

        adapter.notifyDataSetChanged();
        boolean empty = displayedUsers.isEmpty();
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvUsers.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void showChangeRoleDialog(User user) {
        int currentIndex = 0;
        for (int i = 0; i < ROLES.length; i++) {
            if (ROLES[i].equalsIgnoreCase(user.getRole())) {
                currentIndex = i;
                break;
            }
        }

        final int[] selectedIndex = {currentIndex};

        new AlertDialog.Builder(this)
                .setTitle("Change Role for " + user.getFullName())
                .setSingleChoiceItems(ROLES, currentIndex, (dialog, which) -> selectedIndex[0] = which)
                .setPositiveButton("Update", (dialog, which) -> {
                    String newRole = ROLES[selectedIndex[0]];
                    if (!newRole.equalsIgnoreCase(user.getRole())) {
                        updateUserRole(user, newRole);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateUserRole(User user, String newRole) {
        progressBar.setVisibility(View.VISIBLE);
        String oldRole = user.getRole();

        com.example.unisync.utils.DataManager.getInstance().updateUserRole(user.getUid(), newRole, new com.example.unisync.utils.DataManager.OnCompleteListener() {
            @Override
            public void onSuccess() {
                try {
                    AdminLog log = new AdminLog(
                            sessionManager.getUid(),
                            AdminLog.ACTION_UPDATE_USER,
                            AdminLog.TARGET_USER,
                            user.getUid(),
                            oldRole,
                            newRole,
                            "Role updated by Admin"
                    );
                    db.collection(AdminLog.COLLECTION_NAME).add(log);
                } catch (Exception ignored) {}

                progressBar.setVisibility(View.GONE);
                UIHelper.showToast(ManageUsersActivity.this, "Role updated to " + newRole);
                loadUsers();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                UIHelper.showToast(ManageUsersActivity.this, "Role updated to " + newRole);
                loadUsers();
            }
        });
    }

    private void toggleUserStatus(User user) {
        boolean newActive = !user.isActive();
        progressBar.setVisibility(View.VISIBLE);

        com.example.unisync.utils.DataManager.getInstance().toggleUserStatus(user.getUid(), newActive, new com.example.unisync.utils.DataManager.OnCompleteListener() {
            @Override
            public void onSuccess() {
                try {
                    AdminLog log = new AdminLog(
                            sessionManager.getUid(),
                            newActive ? AdminLog.ACTION_UPDATE_USER : AdminLog.ACTION_DEACTIVATE_USER,
                            AdminLog.TARGET_USER,
                            user.getUid(),
                            user.isActive() ? "ACTIVE" : "INACTIVE",
                            newActive ? "ACTIVE" : "INACTIVE",
                            "User status toggled by Admin"
                    );
                    db.collection(AdminLog.COLLECTION_NAME).add(log);
                } catch (Exception ignored) {}

                progressBar.setVisibility(View.GONE);
                UIHelper.showToast(ManageUsersActivity.this, newActive ? "User activated" : "User deactivated");
                loadUsers();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                UIHelper.showToast(ManageUsersActivity.this, newActive ? "User activated" : "User deactivated");
                loadUsers();
            }
        });
    }
}
