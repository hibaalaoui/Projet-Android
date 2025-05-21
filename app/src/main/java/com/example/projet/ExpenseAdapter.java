package com.example.projet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet.Model.Expense;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ExpenseAdapter extends ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder> {

    private final Context context;
    private Map<String, Boolean> limitExceededMap = new HashMap<>();
    private Map<String, Double> limitMap = new HashMap<>();

    public ExpenseAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
    }

    public void setLimitExceededMap(Map<String, Boolean> map) {
        this.limitExceededMap = map != null ? map : new HashMap<>();
        notifyDataSetChanged();
    }

    public void setLimitMap(Map<String, Double> map) {
        this.limitMap = map != null ? map : new HashMap<>();
        notifyDataSetChanged();
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

        // Plafond affiché
        double limit = limitMap.getOrDefault(expense.category, -1.0);
        if (limit > 0) {
            holder.limitText.setText(String.format("Plafond : %.2f DH", limit));
            holder.limitText.setVisibility(View.VISIBLE);
        } else {
            holder.limitText.setVisibility(View.GONE);
        }

        // ⚠️ Si la limite est dépassée
        if (limitExceededMap.getOrDefault(expense.category, false)) {
            holder.warningIcon.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.warning_red));
        } else {
            holder.warningIcon.setVisibility(View.GONE);
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
        }
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView categoryText, amountText, noteText, dateText, warningIcon, limitText;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryText = itemView.findViewById(R.id.textCategory);
            amountText = itemView.findViewById(R.id.textAmount);
            noteText = itemView.findViewById(R.id.textNote);
            dateText = itemView.findViewById(R.id.textDate);
            warningIcon = itemView.findViewById(R.id.textLimitWarning);
            limitText = itemView.findViewById(R.id.textLimitValue);
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
