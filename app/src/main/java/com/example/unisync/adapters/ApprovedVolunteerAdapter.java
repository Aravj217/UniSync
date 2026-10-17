package com.example.unisync.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unisync.R;
import com.example.unisync.models.VolunteerAssignment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ApprovedVolunteerAdapter — displays public approved volunteers for an event.
 * Displays full name, designation, course/department, duty, and status.
 */
public class ApprovedVolunteerAdapter extends RecyclerView.Adapter<ApprovedVolunteerAdapter.ViewHolder> {

    private List<VolunteerAssignment> volunteers;

    public ApprovedVolunteerAdapter(List<VolunteerAssignment> volunteers) {
        setVolunteers(volunteers);
    }

    public void setVolunteers(List<VolunteerAssignment> list) {
        if (list == null) {
            this.volunteers = new ArrayList<>();
        } else {
            // Deduplicate assignments by assignmentId or userId+duty
            List<VolunteerAssignment> deduped = new ArrayList<>();
            Set<String> seenKeys = new HashSet<>();
            for (VolunteerAssignment v : list) {
                if (v == null) continue;
                String key = v.getAssignmentId() != null ? v.getAssignmentId()
                        : (v.getUserId() + "_" + v.getAssignedDuty());
                if (!seenKeys.contains(key)) {
                    seenKeys.add(key);
                    deduped.add(v);
                }
            }
            this.volunteers = deduped;
        }
        notifyDataSetChanged();
    }

    public void updateList(List<VolunteerAssignment> list) {
        setVolunteers(list);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_approved_volunteer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VolunteerAssignment v = volunteers.get(position);
        holder.bind(v);
    }

    @Override
    public int getItemCount() {
        return volunteers != null ? volunteers.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvAvatar;
        private final TextView tvName;
        private final TextView tvDepartment;
        private final TextView tvDuty;
        private final TextView tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
            tvDuty = itemView.findViewById(R.id.tvDuty);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        public void bind(VolunteerAssignment v) {
            // 1. Full Volunteer Name with graceful fallback
            String name = (v.getUserName() != null && !v.getUserName().trim().isEmpty())
                    ? v.getUserName().trim() : "Name unavailable";
            tvName.setText(name);

            // 2. Avatar initial
            String initial = "V";
            if (!"Name unavailable".equals(name) && !name.isEmpty()) {
                initial = name.substring(0, 1).toUpperCase();
            }
            tvAvatar.setText(initial);

            // 3. User Designation & Department / Course
            StringBuilder details = new StringBuilder();
            if (v.getUserType() != null && !v.getUserType().trim().isEmpty()) {
                String type = v.getUserType().equalsIgnoreCase("TEACHER") ? "Teacher" : "Student";
                details.append(type);
            }
            if (v.getCourse() != null && !v.getCourse().trim().isEmpty()) {
                if (details.length() > 0) details.append(" · ");
                details.append(v.getCourse().trim());
            }
            if (v.getDepartment() != null && !v.getDepartment().trim().isEmpty()) {
                if (details.length() > 0) details.append(" · ");
                details.append(v.getDepartment().trim());
            }
            tvDepartment.setText(details.length() > 0 ? details.toString() : "University Member");

            // 4. Assigned Duty
            String duty = (v.getAssignedDuty() != null && !v.getAssignedDuty().trim().isEmpty())
                    ? v.getAssignedDuty().trim() : "Event Volunteer";
            tvDuty.setText("Duty: " + duty);

            // 5. Assignment Status
            if (tvStatus != null) {
                if (v.isActive()) {
                    tvStatus.setText("Active");
                    tvStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.status_published));
                } else if (v.getStatus() != null) {
                    tvStatus.setText(v.getStatus());
                } else {
                    tvStatus.setText("Assigned");
                }
            }
        }
    }
}
