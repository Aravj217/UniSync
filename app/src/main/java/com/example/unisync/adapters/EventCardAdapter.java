package com.example.unisync.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.unisync.R;
import com.example.unisync.models.Event;
import com.example.unisync.utils.DataManager;
import com.example.unisync.utils.UIHelper;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * EventCardAdapter — RecyclerView adapter for displaying Event cards in a list.
 */
public class EventCardAdapter extends RecyclerView.Adapter<EventCardAdapter.EventViewHolder> {

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    private List<Event> events;
    private final OnEventClickListener listener;

    public EventCardAdapter(List<Event> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_card, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event, listener);
    }

    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivEventPoster;
        private final View layoutPosterPlaceholder;
        private final TextView tvPlaceholderCategory;
        private final TextView tvCategory;
        private final TextView tvStatus;
        private final TextView tvVolunteerCount;
        private final TextView tvEventName;
        private final TextView tvShortDescription;
        private final TextView tvDate;
        private final TextView tvTime;
        private final TextView tvVenue;
        private final TextView tvOrganizingTeam;
        private final MaterialButton btnViewDetails;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEventPoster = itemView.findViewById(R.id.ivEventPoster);
            layoutPosterPlaceholder = itemView.findViewById(R.id.layoutPosterPlaceholder);
            tvPlaceholderCategory = itemView.findViewById(R.id.tvPlaceholderCategory);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvVolunteerCount = itemView.findViewById(R.id.tvVolunteerCount);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvShortDescription = itemView.findViewById(R.id.tvShortDescription);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvVenue = itemView.findViewById(R.id.tvVenue);
            tvOrganizingTeam = itemView.findViewById(R.id.tvOrganizingTeam);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }

        public void bind(Event event, OnEventClickListener listener) {
            tvCategory.setText(event.getCategory() != null ? event.getCategory() : "General");
            UIHelper.applyEventStatusBadge(tvStatus, event.getStatus());

            if (event.getPosterUrl() != null && !event.getPosterUrl().trim().isEmpty()) {
                ivEventPoster.setVisibility(View.VISIBLE);
                layoutPosterPlaceholder.setVisibility(View.GONE);
                Glide.with(itemView.getContext())
                        .load(event.getPosterUrl().trim())
                        .centerCrop()
                        .into(ivEventPoster);
            } else {
                ivEventPoster.setVisibility(View.GONE);
                layoutPosterPlaceholder.setVisibility(View.VISIBLE);
                tvPlaceholderCategory.setText(event.getCategory() != null ? event.getCategory() : "UniSync Event");
            }

            tvEventName.setText(event.getEventName());
            tvShortDescription.setText(event.getShortDescription() != null ? event.getShortDescription() : "");

            String displayDate = UIHelper.formatDateForDisplay(event.getEventDate());
            tvDate.setText(!displayDate.isEmpty() ? displayDate : "Date TBA");

            String displayStart = UIHelper.formatTimeForDisplay(event.getStartTime());
            String displayEnd = UIHelper.formatTimeForDisplay(event.getEndTime());
            if (!displayStart.isEmpty() && !displayEnd.isEmpty()) {
                tvTime.setText(displayStart + " - " + displayEnd);
            } else if (!displayStart.isEmpty()) {
                tvTime.setText(displayStart);
            } else {
                tvTime.setText("Time TBA");
            }

            tvVenue.setText(event.getVenue() != null && !event.getVenue().isEmpty()
                    ? event.getVenue() : "Venue TBA");
            tvOrganizingTeam.setText(event.getOrganizingTeam() != null && !event.getOrganizingTeam().isEmpty()
                    ? event.getOrganizingTeam() : "Campus");

            // Load and display actual assigned volunteer count from database (async)
            tvVolunteerCount.setText("Volunteers...");
            DataManager.getInstance().getApprovedAssignmentsByEvent(event.getEventId(), list -> {
                int count = (list != null) ? list.size() : 0;
                tvVolunteerCount.setText(count + (count == 1 ? " Volunteer" : " Volunteers"));
            });

            View.OnClickListener clickAction = v -> {
                if (listener != null) {
                    listener.onEventClick(event);
                }
            };
            itemView.setOnClickListener(clickAction);
            if (btnViewDetails != null) {
                btnViewDetails.setOnClickListener(clickAction);
            }
        }
    }
}
