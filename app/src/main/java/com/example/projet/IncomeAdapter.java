package com.example.projet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet.Model.Income;

import java.util.List;

public class IncomeAdapter extends RecyclerView.Adapter<IncomeAdapter.IncomeViewHolder> {

    private List<Income> incomeList;

    // ✅ Constructeur avec une liste d'incomes
    public IncomeAdapter(List<Income> incomeList) {
        this.incomeList = incomeList;
    }

    // ✅ Méthode pour mettre à jour la liste
    public void setIncomeList(List<Income> newList) {
        this.incomeList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IncomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_income, parent, false);
        return new IncomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IncomeViewHolder holder, int position) {
        Income income = incomeList.get(position);
        holder.textSource.setText(income.getSource());
        holder.textAmount.setText(String.valueOf(income.getAmount()));
    }

    @Override
    public int getItemCount() {
        return incomeList != null ? incomeList.size() : 0;
    }

    public static class IncomeViewHolder extends RecyclerView.ViewHolder {
        TextView textSource, textAmount;

        public IncomeViewHolder(@NonNull View itemView) {
            super(itemView);
            textSource = itemView.findViewById(R.id.textSource);
            textAmount = itemView.findViewById(R.id.textAmount);
        }
    }
}
