package com.example.unisync.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unisync.R;
import com.example.unisync.models.RequestPerson;

import java.util.List;

/**
 * RequestPersonAdapter — RecyclerView adapter for dynamic list of requested people.
 */
public class RequestPersonAdapter extends RecyclerView.Adapter<RequestPersonAdapter.PersonViewHolder> {

    public interface OnPersonRemoveListener {
        void onRemove(int position);
    }

    private final List<RequestPerson> people;
    private final boolean canRemove;
    private final OnPersonRemoveListener removeListener;

    public RequestPersonAdapter(List<RequestPerson> people, boolean canRemove, OnPersonRemoveListener removeListener) {
        this.people = people;
        this.canRemove = canRemove;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_request_person, parent, false);
        return new PersonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonViewHolder holder, int position) {
        RequestPerson person = people.get(position);
        holder.bind(person, position + 1, canRemove, removeListener);
    }

    @Override
    public int getItemCount() {
        return people != null ? people.size() : 0;
    }

    static class PersonViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvPersonIndex;
        private final TextView tvPersonName;
        private final TextView tvDutyRole;
        private final TextView tvPersonDetails;
        private final ImageView btnRemovePerson;

        public PersonViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPersonIndex = itemView.findViewById(R.id.tvPersonIndex);
            tvPersonName = itemView.findViewById(R.id.tvPersonName);
            tvDutyRole = itemView.findViewById(R.id.tvDutyRole);
            tvPersonDetails = itemView.findViewById(R.id.tvPersonDetails);
            btnRemovePerson = itemView.findViewById(R.id.btnRemovePerson);
        }

        public void bind(RequestPerson person, int index, boolean canRemove, OnPersonRemoveListener removeListener) {
            tvPersonIndex.setText(String.valueOf(index));
            tvPersonName.setText(person.getFullName() != null && !person.getFullName().isEmpty()
                    ? person.getFullName() : "Open Volunteer Slot");

            tvDutyRole.setText(person.getAssignedDuty() != null && !person.getAssignedDuty().isEmpty()
                    ? "Duty: " + person.getAssignedDuty() : "Duty: General");

            StringBuilder details = new StringBuilder();
            if (person.getCourse() != null && !person.getCourse().isEmpty()) {
                details.append(person.getCourse());
            }
            if (person.getYear() != null && !person.getYear().isEmpty()) {
                if (details.length() > 0) details.append(" · ");
                details.append("Year ").append(person.getYear());
            }
            if (person.getDepartment() != null && !person.getDepartment().isEmpty()) {
                if (details.length() > 0) details.append(" · ");
                details.append(person.getDepartment());
            }
            if (person.getRollNumber() != null && !person.getRollNumber().isEmpty()) {
                if (details.length() > 0) details.append(" · ");
                details.append("Roll: ").append(person.getRollNumber());
            }

            tvPersonDetails.setText(details.length() > 0 ? details.toString() : "No details provided");

            btnRemovePerson.setVisibility(canRemove ? View.VISIBLE : View.GONE);
            if (canRemove && removeListener != null) {
                btnRemovePerson.setOnClickListener(v -> removeListener.onRemove(getAdapterPosition()));
            }
        }
    }
}
