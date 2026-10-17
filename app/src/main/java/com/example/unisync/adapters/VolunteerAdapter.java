package com.example.unisync.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unisync.R;
import com.example.unisync.models.Volunteer;

import java.util.List;

/**
 * VolunteerAdapter — RecyclerView adapter for displaying Volunteer records.
 */
public class VolunteerAdapter extends RecyclerView.Adapter<VolunteerAdapter.VolunteerViewHolder> {

    public interface OnVolunteerActionListener {
        void onEdit(Volunteer volunteer);
        void onDelete(Volunteer volunteer);
    }

    private List<Volunteer> volunteers;
    private final boolean showActions;
    private final OnVolunteerActionListener listener;

    public VolunteerAdapter(List<Volunteer> volunteers, boolean showActions, OnVolunteerActionListener listener) {
        this.volunteers = volunteers;
        this.showActions = showActions;
        this.listener = listener;
    }

    public void setVolunteers(List<Volunteer> volunteers) {
        this.volunteers = volunteers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VolunteerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_volunteer, parent, false);
        return new VolunteerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VolunteerViewHolder holder, int position) {
        Volunteer volunteer = volunteers.get(position);
        holder.bind(volunteer, showActions, listener);
    }

    @Override
    public int getItemCount() {
        return volunteers != null ? volunteers.size() : 0;
    }

    static class VolunteerViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvAvatar;
        private final TextView tvStudentName;
        private final TextView tvStudentDetails;
        private final TextView tvRollNumber;
        private final TextView tvDuty;
        private final TextView tvTeam;
        private final LinearLayout layoutActions;
        private final ImageView btnEdit;
        private final ImageView btnDelete;

        public VolunteerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentDetails = itemView.findViewById(R.id.tvStudentDetails);
            tvRollNumber = itemView.findViewById(R.id.tvRollNumber);
            tvDuty = itemView.findViewById(R.id.tvDuty);
            tvTeam = itemView.findViewById(R.id.tvTeam);
            layoutActions = itemView.findViewById(R.id.layoutActions);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Volunteer volunteer, boolean showActions, OnVolunteerActionListener listener) {
            String name = volunteer.getStudentName();
            tvStudentName.setText(name != null && !name.isEmpty() ? name : "Volunteer");

            if (name != null && !name.trim().isEmpty()) {
                tvAvatar.setText(name.substring(0, 1).toUpperCase());
            } else {
                tvAvatar.setText("V");
            }

            StringBuilder details = new StringBuilder();
            if (volunteer.getCourse() != null && !volunteer.getCourse().isEmpty()) {
                details.append(volunteer.getCourse());
            }
            if (volunteer.getYear() != null && !volunteer.getYear().isEmpty()) {
                if (details.length() > 0) details.append(" · ");
                details.append("Year ").append(volunteer.getYear());
            }
            if (volunteer.getDepartment() != null && !volunteer.getDepartment().isEmpty()) {
                if (details.length() > 0) details.append(" · ");
                details.append(volunteer.getDepartment());
            }
            tvStudentDetails.setText(details.length() > 0 ? details.toString() : "Student");

            tvRollNumber.setText(volunteer.getRollNumber() != null && !volunteer.getRollNumber().isEmpty()
                    ? "Roll: " + volunteer.getRollNumber() : "No Roll Number");

            tvDuty.setText(volunteer.getAssignedDuty() != null && !volunteer.getAssignedDuty().isEmpty()
                    ? "Duty: " + volunteer.getAssignedDuty() : "General Duty");

            tvTeam.setText(volunteer.getTeam() != null && !volunteer.getTeam().isEmpty()
                    ? "Team: " + volunteer.getTeam() : "Main Team");

            layoutActions.setVisibility(showActions ? View.VISIBLE : View.GONE);
            if (showActions && listener != null) {
                btnEdit.setOnClickListener(v -> listener.onEdit(volunteer));
                btnDelete.setOnClickListener(v -> listener.onDelete(volunteer));
            }
        }
    }
}
