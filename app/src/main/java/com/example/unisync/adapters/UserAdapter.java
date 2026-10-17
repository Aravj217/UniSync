package com.example.unisync.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unisync.R;
import com.example.unisync.models.User;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * UserAdapter — RecyclerView adapter for Admin User Management.
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnUserActionListener {
        void onChangeRole(User user);
        void onToggleStatus(User user);
    }

    private List<User> users;
    private final OnUserActionListener listener;

    public UserAdapter(List<User> users, OnUserActionListener listener) {
        this.users = users;
        this.listener = listener;
    }

    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvUserAvatar;
        private final TextView tvUserName;
        private final TextView tvUserEmail;
        private final TextView tvUserDept;
        private final TextView tvUserRole;
        private final MaterialButton btnChangeRole;
        private final MaterialButton btnToggleStatus;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserAvatar = itemView.findViewById(R.id.tvUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserDept = itemView.findViewById(R.id.tvUserDept);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
            btnChangeRole = itemView.findViewById(R.id.btnChangeRole);
            btnToggleStatus = itemView.findViewById(R.id.btnToggleStatus);
        }

        public void bind(User user, OnUserActionListener listener) {
            Context context = itemView.getContext();
            String name = user.getFullName();
            tvUserName.setText(name != null && !name.isEmpty() ? name : "User");

            if (name != null && !name.trim().isEmpty()) {
                tvUserAvatar.setText(name.substring(0, 1).toUpperCase());
            } else {
                tvUserAvatar.setText("U");
            }

            tvUserEmail.setText(user.getEmail());

            StringBuilder deptInfo = new StringBuilder();
            if (user.getDepartment() != null && !user.getDepartment().isEmpty()) {
                deptInfo.append(user.getDepartment());
            }
            if (user.getRollNumber() != null && !user.getRollNumber().isEmpty()) {
                if (deptInfo.length() > 0) deptInfo.append(" · ");
                deptInfo.append("Roll: ").append(user.getRollNumber());
            }
            tvUserDept.setText(deptInfo.length() > 0 ? deptInfo.toString() : "Campus Member");

            String role = user.getRole() != null ? user.getRole() : User.ROLE_STUDENT;
            tvUserRole.setText(role);

            if (User.ROLE_ADMIN.equals(role)) {
                tvUserRole.setTextColor(ContextCompat.getColor(context, R.color.accent_dark));
                tvUserRole.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.accent_container));
            } else if (User.ROLE_ORGANIZER.equals(role)) {
                tvUserRole.setTextColor(ContextCompat.getColor(context, R.color.primary));
                tvUserRole.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.primary_container));
            } else {
                tvUserRole.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
                tvUserRole.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.surface_variant));
            }

            boolean isActive = user.isActive();
            btnToggleStatus.setText(isActive ? "Active" : "Disabled");
            btnToggleStatus.setTextColor(ContextCompat.getColor(context, isActive ? R.color.success : R.color.error));

            btnChangeRole.setOnClickListener(v -> {
                if (listener != null) listener.onChangeRole(user);
            });

            btnToggleStatus.setOnClickListener(v -> {
                if (listener != null) listener.onToggleStatus(user);
            });
        }
    }
}
