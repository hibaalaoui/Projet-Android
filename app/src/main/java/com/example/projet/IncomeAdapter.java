package com.example.projet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet.Model.Income;

import java.util.List;
import java.util.Locale;

public class IncomeAdapter extends RecyclerView.Adapter<IncomeAdapter.IncomeViewHolder> {

    private List<Income> incomeList;

    public IncomeAdapter(List<Income> incomeList) {
        this.incomeList = incomeList;
    }

    @NonNull
    @Override
    public IncomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new IncomeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull IncomeViewHolder holder, int position) {
        Income income = incomeList.get(position);
        holder.text1.setText(income.getSource());
        holder.text2.setText(String.format(Locale.getDefault(), "%.2f Dhs", income.getAmount()));
    }

    @Override
    public int getItemCount() {
        return incomeList.size();
    }

    public void setIncomeList(List<Income> incomes) {
        this.incomeList = incomes;
        notifyDataSetChanged();
    }

    static class IncomeViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;

        IncomeViewHolder(View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}
