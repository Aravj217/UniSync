package com.example.unisync.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unisync.R;
import com.example.unisync.models.TeamRequest;
import com.example.unisync.utils.UIHelper;

import java.util.List;

/**
 * TeamRequestAdapter — RecyclerView adapter for displaying Team Support Requests cards.
 */
public class TeamRequestAdapter extends RecyclerView.Adapter<TeamRequestAdapter.RequestViewHolder> {

    public interface OnRequestClickListener {
        void onRequestClick(TeamRequest request);
    }

    private List<TeamRequest> requests;
    private final OnRequestClickListener listener;

    public TeamRequestAdapter(List<TeamRequest> requests, OnRequestClickListener listener) {
        this.requests = requests;
        this.listener = listener;
    }

    public void setRequests(List<TeamRequest> requests) {
        this.requests = requests;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_team_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        TeamRequest request = requests.get(position);
        holder.bind(request, listener);
    }

    @Override
    public int getItemCount() {
        return requests != null ? requests.size() : 0;
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvCategory;
        private final TextView tvStatus;
        private final TextView tvTitle;
        private final TextView tvDescription;
        private final TextView tvDate;
        private final TextView tvPeopleRequired;
        private final TextView tvLocation;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPeopleRequired = itemView.findViewById(R.id.tvPeopleRequired);
            tvLocation = itemView.findViewById(R.id.tvLocation);
        }

        public void bind(TeamRequest request, OnRequestClickListener listener) {
            tvCategory.setText(request.getCategory() != null ? request.getCategory() : "General");
            UIHelper.applyRequestStatusBadge(tvStatus, request.getStatus());

            tvTitle.setText(request.getTitle());
            tvDescription.setText(request.getDescription() != null ? request.getDescription() : request.getReason());

            String displayDate = UIHelper.formatDateForDisplay(request.getRequiredDate());
            tvDate.setText(!displayDate.isEmpty() ? displayDate : "Date TBA");

            int peopleCount = request.getPeopleRequired();
            tvPeopleRequired.setText(peopleCount > 0 ? (peopleCount + (peopleCount == 1 ? " Person" : " People")) : "Volunteers");

            tvLocation.setText(request.getWorkLocation() != null && !request.getWorkLocation().isEmpty()
                    ? request.getWorkLocation() : "Campus");

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRequestClick(request);
                }
            });
        }
    }
}
