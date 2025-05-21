package com.example.projet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet.Model.BudgetLimit;

import java.util.List;
import java.util.Locale;

public class BudgetLimitAdapter extends RecyclerView.Adapter<BudgetLimitAdapter.LimitViewHolder> {

    private List<BudgetLimit> limitList;

    public void setLimitList(List<BudgetLimit> list) {
        this.limitList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LimitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_limit, parent, false);
        return new LimitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LimitViewHolder holder, int position) {
        BudgetLimit limit = limitList.get(position);
        holder.category.setText(limit.category);
        holder.amount.setText(String.format(Locale.getDefault(), "Limite : %.2f DH", limit.limitAmount));
    }

    @Override
    public int getItemCount() {
        return limitList != null ? limitList.size() : 0;
    }

    public static class LimitViewHolder extends RecyclerView.ViewHolder {
        TextView category, amount;

        public LimitViewHolder(@NonNull View itemView) {
            super(itemView);
            category = itemView.findViewById(R.id.textCategory);
            amount = itemView.findViewById(R.id.textLimitAmount);
        }
    }
}
