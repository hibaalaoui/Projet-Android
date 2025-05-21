package com.example.projet;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet.Model.Expense;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExpenseChartFragment extends Fragment {

    private ExpenseViewModel viewModel;
    private PieChart pieChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_expense_chart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

            super.onViewCreated(view, savedInstanceState);

            ExpenseViewModel viewModel = new ViewModelProvider(requireActivity()).get(ExpenseViewModel.class);

            TextView textTotal = view.findViewById(R.id.textTotalChart);

            // Adapter (existant)
            ExpenseAdapter adapter = new ExpenseAdapter(requireContext());

            // 🔁 Observer les dépenses
            viewModel.getExpenses().observe(getViewLifecycleOwner(), adapter::submitList);

            // 💰 Observer le total
            viewModel.getTotalAmount().observe(getViewLifecycleOwner(), total -> {
                textTotal.setText(String.format(Locale.getDefault(), "Total : %.2f DH", total));

            });

        pieChart = view.findViewById(R.id.pieChart);
        viewModel = new ViewModelProvider(requireActivity()).get(ExpenseViewModel.class);

        viewModel.getExpenses().observe(getViewLifecycleOwner(), expenses -> {
            Map<String, Float> categorySums = new HashMap<>();

            for (Expense e : expenses) {
                float current = categorySums.getOrDefault(e.category, 0f);
                categorySums.put(e.category, current + (float) e.amount);
            }

            List<PieEntry> entries = new ArrayList<>();
            for (Map.Entry<String, Float> entry : categorySums.entrySet()) {
                entries.add(new PieEntry(entry.getValue(), entry.getKey()));
            }

            PieDataSet dataSet = new PieDataSet(entries, "Dépenses par catégorie");
            dataSet.setColors(Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.CYAN, Color.YELLOW);
            PieData data = new PieData(dataSet);
            pieChart.setData(data);
            pieChart.invalidate(); // refresh
        });
    }
}
