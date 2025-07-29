package com.example.recyclercardview.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recyclercardview.R;
import com.example.recyclercardview.model.Person;

import java.util.List;

public class PersonAdapter extends RecyclerView.Adapter<PersonAdapter.PersonViewHolder> {

    private List<Person> personList;
    private OnPersonClickListener listener;

    // Interface for click events
    public interface OnPersonClickListener {
        void onEditClick(Person person, int position);
        void onDeleteClick(Person person, int position);
        void onItemClick(Person person, int position);
    }

    // Constructor
    public PersonAdapter(List<Person> personList) {
        this.personList = personList;
    }

    // Set click listener
    public void setOnPersonClickListener(OnPersonClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_person, parent, false);
        return new PersonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonViewHolder holder, int position) {
        Person person = personList.get(position);
        holder.bind(person, position);
    }

    @Override
    public int getItemCount() {
        return personList.size();
    }

    // Method untuk update data
    public void updateData(List<Person> newPersonList) {
        this.personList = newPersonList;
        notifyDataSetChanged();
    }

    // Method untuk menambah data
    public void addPerson(Person person) {
        personList.add(person);
        notifyItemInserted(personList.size() - 1);
    }

    // Method untuk menghapus data
    public void removePerson(int position) {
        personList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, personList.size());
    }

    // Method untuk update data tertentu
    public void updatePerson(int position, Person person) {
        personList.set(position, person);
        notifyItemChanged(position);
    }

    // ViewHolder class
    public class PersonViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewId, textViewName, textViewAddress, textViewDate;
        private Button buttonEdit, buttonDelete;

        public PersonViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewId = itemView.findViewById(R.id.textViewId);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewAddress = itemView.findViewById(R.id.textViewAddress);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }

        public void bind(Person person, int position) {
            // Set data ke views
            textViewId.setText(String.format("#%03d", person.getId()));
            textViewName.setText(person.getName());
            textViewAddress.setText(person.getAddress());
            textViewDate.setText(person.getDateAdded());

            // Set click listeners
            if (listener != null) {
                // Item click
                itemView.setOnClickListener(v ->
                        listener.onItemClick(person, position)
                );

                // Edit button click
                buttonEdit.setOnClickListener(v ->
                        listener.onEditClick(person, position)
                );

                // Delete button click
                buttonDelete.setOnClickListener(v ->
                        listener.onDeleteClick(person, position)
                );
            }

            // Add ripple effect
            itemView.setOnClickListener(v -> {
                Toast.makeText(v.getContext(),
                        "Selected: " + person.getName(),
                        Toast.LENGTH_SHORT).show();

                if (listener != null) {
                    listener.onItemClick(person, position);
                }
            });
        }
    }
}