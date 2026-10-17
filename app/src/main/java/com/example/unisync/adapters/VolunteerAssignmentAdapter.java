package com.example.unisync.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unisync.R;
import com.example.unisync.models.VolunteerAssignment;
import com.example.unisync.utils.UIHelper;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * VolunteerAssignmentAdapter — RecyclerView adapter for displaying VolunteerAssignment records.
 * Supports Organizer, Admin, and Student/Teacher views.
 */
public class VolunteerAssignmentAdapter extends RecyclerView.Adapter<VolunteerAssignmentAdapter.AssignmentViewHolder> {

    public enum ViewMode {
        ORGANIZER,
        ADMIN,
        USER_ASSIGNMENTS
    }

    public interface OnAssignmentActionListener {
        default void onEdit(VolunteerAssignment assignment) {}
        default void onCancel(VolunteerAssignment assignment) {}
        default void onApprove(VolunteerAssignment assignment) {}
        default void onReject(VolunteerAssignment assignment) {}
        default void onReturn(VolunteerAssignment assignment) {}
    }

    private List<VolunteerAssignment> assignments;
    private final ViewMode viewMode;
    private final OnAssignmentActionListener listener;

    public VolunteerAssignmentAdapter(List<VolunteerAssignment> assignments, ViewMode viewMode, OnAssignmentActionListener listener) {
        this.assignments = assignments;
        this.viewMode = viewMode;
        this.listener = listener;
    }

    public void setAssignments(List<VolunteerAssignment> assignments) {
        this.assignments = assignments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_volunteer_assignment, parent, false);
        return new AssignmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssignmentViewHolder holder, int position) {
        VolunteerAssignment assignment = assignments.get(position);
        holder.bind(assignment, viewMode, listener);
    }

    @Override
    public int getItemCount() {
        return assignments != null ? assignments.size() : 0;
    }

    static class AssignmentViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvEventName;
        private final TextView tvStatusBadge;
        private final TextView tvAvatar;
        private final TextView tvUserName;
        private final TextView tvUserTypeBadge;
        private final TextView tvUserEmail;
        private final TextView tvUserDetails;
        private final TextView tvDuty;
        private final TextView tvScheduleAndVenue;
        private final TextView tvInstructions;
        private final LinearLayout layoutRejection;
        private final TextView tvRejectionTitle;
        private final TextView tvRejectionReason;

        private final LinearLayout layoutOrganizerActions;
        private final MaterialButton btnCancelAssignment;
        private final MaterialButton btnEditAssignment;

        private final LinearLayout layoutAdminActions;
        private final MaterialButton btnAdminReject;
        private final MaterialButton btnAdminReturn;
        private final MaterialButton btnAdminApprove;

        public AssignmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserTypeBadge = itemView.findViewById(R.id.tvUserTypeBadge);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserDetails = itemView.findViewById(R.id.tvUserDetails);
            tvDuty = itemView.findViewById(R.id.tvDuty);
            tvScheduleAndVenue = itemView.findViewById(R.id.tvScheduleAndVenue);
            tvInstructions = itemView.findViewById(R.id.tvInstructions);
            layoutRejection = itemView.findViewById(R.id.layoutRejection);
            tvRejectionTitle = itemView.findViewById(R.id.tvRejectionTitle);
            tvRejectionReason = itemView.findViewById(R.id.tvRejectionReason);

            layoutOrganizerActions = itemView.findViewById(R.id.layoutOrganizerActions);
            btnCancelAssignment = itemView.findViewById(R.id.btnCancelAssignment);
            btnEditAssignment = itemView.findViewById(R.id.btnEditAssignment);

            layoutAdminActions = itemView.findViewById(R.id.layoutAdminActions);
            btnAdminReject = itemView.findViewById(R.id.btnAdminReject);
            btnAdminReturn = itemView.findViewById(R.id.btnAdminReturn);
            btnAdminApprove = itemView.findViewById(R.id.btnAdminApprove);
        }

        public void bind(VolunteerAssignment assignment, ViewMode viewMode, OnAssignmentActionListener listener) {
            tvEventName.setText(assignment.getEventName() != null ? assignment.getEventName() : "Event Assignment");

            // Status Badge
            String status = assignment.getStatus() != null ? assignment.getStatus() : VolunteerAssignment.STATUS_PENDING;
            UIHelper.applyAssignmentStatusBadge(tvStatusBadge, status);

            // User info
            String name = (assignment.getUserName() != null && !assignment.getUserName().trim().isEmpty())
                    ? assignment.getUserName().trim() : "Name unavailable";
            tvUserName.setText(name);
            tvAvatar.setText(!name.equals("Name unavailable") && !name.isEmpty() ? name.substring(0, 1).toUpperCase() : "V");

            String userType = assignment.getUserType() != null ? assignment.getUserType() : "STUDENT";
            tvUserTypeBadge.setText(userType.toUpperCase());

            if (assignment.getUserEmail() != null && !assignment.getUserEmail().isEmpty()) {
                tvUserEmail.setText(assignment.getUserEmail());
                tvUserEmail.setVisibility(View.VISIBLE);
            } else {
                tvUserEmail.setVisibility(View.GONE);
            }

            StringBuilder details = new StringBuilder();
            if (assignment.getCourse() != null && !assignment.getCourse().isEmpty()) {
                details.append(assignment.getCourse());
            }
            if (assignment.getYear() != null && !assignment.getYear().isEmpty()) {
                if (details.length() > 0) details.append(" · ");
                details.append(assignment.getYear());
            }
            if (assignment.getDepartment() != null && !assignment.getDepartment().isEmpty()) {
                if (details.length() > 0) details.append(" · ");
                details.append(assignment.getDepartment());
            }
            if (assignment.getRollNumber() != null && !assignment.getRollNumber().isEmpty()) {
                if (details.length() > 0) details.append(" · Roll: ");
                details.append(assignment.getRollNumber());
            }
            tvUserDetails.setText(details.length() > 0 ? details.toString() : "NMIMS Member");

            // Duty
            tvDuty.setText(assignment.getAssignedDuty() != null ? "Duty: " + assignment.getAssignedDuty() : "General Duty");

            // Schedule and Venue
            String dateDisp = UIHelper.formatDateForDisplay(assignment.getEventDate());
            String startDisp = UIHelper.formatTimeForDisplay(assignment.getStartTime());
            String endDisp = UIHelper.formatTimeForDisplay(assignment.getEndTime());
            String timeDisp = startDisp + (!endDisp.isEmpty() ? " - " + endDisp : "");

            StringBuilder sched = new StringBuilder();
            if (!dateDisp.isEmpty()) sched.append("Date: ").append(dateDisp);
            if (!timeDisp.isEmpty()) {
                if (sched.length() > 0) sched.append(" | Time: ");
                sched.append(timeDisp);
            }
            if (assignment.getVenue() != null && !assignment.getVenue().isEmpty()) {
                if (sched.length() > 0) sched.append("\nVenue: ");
                sched.append(assignment.getVenue());
            }
            tvScheduleAndVenue.setText(sched.length() > 0 ? sched.toString() : "Schedule & Venue TBA");

            // Instructions
            if (assignment.getAdditionalInstructions() != null && !assignment.getAdditionalInstructions().isEmpty()) {
                tvInstructions.setText("Instructions: " + assignment.getAdditionalInstructions());
                tvInstructions.setVisibility(View.VISIBLE);
            } else {
                tvInstructions.setVisibility(View.GONE);
            }

            // Rejection reason box
            if ((VolunteerAssignment.STATUS_REJECTED.equals(status) || VolunteerAssignment.STATUS_NEEDS_CORRECTION.equals(status))
                    && assignment.getRejectionReason() != null && !assignment.getRejectionReason().isEmpty()) {
                layoutRejection.setVisibility(View.VISIBLE);
                tvRejectionTitle.setText(VolunteerAssignment.STATUS_NEEDS_CORRECTION.equals(status)
                        ? "Admin Feedback / Corrections Requested:"
                        : "Rejection Reason:");
                tvRejectionReason.setText(assignment.getRejectionReason());
            } else {
                layoutRejection.setVisibility(View.GONE);
            }

            // Actions visibility based on ViewMode
            if (viewMode == ViewMode.ORGANIZER) {
                layoutOrganizerActions.setVisibility(View.VISIBLE);
                layoutAdminActions.setVisibility(View.GONE);

                boolean canEdit = assignment.canEdit();
                btnEditAssignment.setVisibility(canEdit ? View.VISIBLE : View.GONE);
                btnCancelAssignment.setVisibility(!VolunteerAssignment.STATUS_CANCELLED.equals(status) ? View.VISIBLE : View.GONE);

                if (listener != null) {
                    btnEditAssignment.setOnClickListener(v -> listener.onEdit(assignment));
                    btnCancelAssignment.setOnClickListener(v -> listener.onCancel(assignment));
                }
            } else if (viewMode == ViewMode.ADMIN) {
                layoutOrganizerActions.setVisibility(View.GONE);
                layoutAdminActions.setVisibility(View.VISIBLE);

                // Admin direct intervention: can cancel/remove active assignments
                btnAdminApprove.setVisibility(View.GONE);
                btnAdminReturn.setVisibility(View.GONE);
                boolean canCancel = !VolunteerAssignment.STATUS_CANCELLED.equals(status);
                btnAdminReject.setVisibility(canCancel ? View.VISIBLE : View.GONE);
                btnAdminReject.setText("Remove Assignment");

                if (listener != null) {
                    btnAdminReject.setOnClickListener(v -> listener.onCancel(assignment));
                }
            } else {
                // USER_ASSIGNMENTS: clean read-only view
                layoutOrganizerActions.setVisibility(View.GONE);
                layoutAdminActions.setVisibility(View.GONE);
            }
        }
    }
}
