package com.example.projet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet.Model.Expense;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExpenseAdapter extends ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder> {

    public ExpenseAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = getItem(position);
        holder.categoryText.setText(expense.category);
        holder.amountText.setText(String.format(Locale.getDefault(), "%.2f DH", expense.amount));
        holder.noteText.setText(expense.note);
        holder.dateText.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(new Date(expense.timestamp)));
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView categoryText, amountText, noteText, dateText;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryText = itemView.findViewById(R.id.textCategory);
            amountText = itemView.findViewById(R.id.textAmount);
            noteText = itemView.findViewById(R.id.textNote);
            dateText = itemView.findViewById(R.id.textDate);
        }
    }

    public static final DiffUtil.ItemCallback<Expense> DIFF_CALLBACK = new DiffUtil.ItemCallback<Expense>() {
        @Override
        public boolean areItemsTheSame(@NonNull Expense oldItem, @NonNull Expense newItem) {
            return oldItem.id != null && oldItem.id.equals(newItem.id);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Expense oldItem, @NonNull Expense newItem) {
            return oldItem.amount == newItem.amount &&
                    oldItem.category.equals(newItem.category) &&
                    oldItem.note.equals(newItem.note) &&
                    oldItem.timestamp == newItem.timestamp;
        }
    };
}
