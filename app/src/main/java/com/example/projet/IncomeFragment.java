package com.example.projet;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet.Model.Income;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;


import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class IncomeFragment extends Fragment {

    private EditText editTextAmount, editTextSource;
    private Button buttonAddIncome;
    private IncomeViewModel incomeViewModel;
    private RecyclerView recyclerViewIncomes;
    private IncomeAdapter incomeAdapter;
    private BarChart barChart;
    private PieChart pieChart;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_income, container, false);

        editTextAmount = view.findViewById(R.id.editTextAmount);
        editTextSource = view.findViewById(R.id.editTextSource);
        buttonAddIncome = view.findViewById(R.id.buttonAddIncome);
        recyclerViewIncomes = view.findViewById(R.id.recyclerViewIncomes);
        barChart = view.findViewById(R.id.barChart);
        pieChart = view.findViewById(R.id.pieChart);

        recyclerViewIncomes.setLayoutManager(new LinearLayoutManager(getContext()));
        incomeAdapter = new IncomeAdapter(new ArrayList<>());
        recyclerViewIncomes.setAdapter(incomeAdapter);

        incomeViewModel = new ViewModelProvider(this).get(IncomeViewModel.class);

        incomeViewModel.getSuccessMessage().observe(getViewLifecycleOwner(), message -> {
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            editTextAmount.setText("");
            editTextSource.setText("");
        });

        incomeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        });

        incomeViewModel.getIncomeList().observe(getViewLifecycleOwner(), incomes -> {
            incomeAdapter.setIncomeList(incomes);
            incomeAdapter.notifyDataSetChanged();
            updateBarChart(incomes);
            updatePieChart(incomes);  // <-- Ajout ici pour le PieChart
        });

        buttonAddIncome.setOnClickListener(v -> {
            String source = editTextSource.getText().toString().trim();
            String amountStr = editTextAmount.getText().toString().trim();

            if (source.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(getActivity(), "Champs vides", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getActivity(), "Montant invalide", Toast.LENGTH_SHORT).show();
                return;
            }

            incomeViewModel.addIncome(source, amount);
        });

        return view;
    }

    private void updateBarChart(List<Income> incomes) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < incomes.size(); i++) {
            Income income = incomes.get(i);
            entries.add(new BarEntry(i, (float) income.getAmount()));
            labels.add(income.getSource());
        }

        BarDataSet dataSet = new BarDataSet(entries, "Revenus");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(14f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        barChart.getDescription().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    private void updatePieChart(List<Income> incomes) {
        List<PieEntry> entries = new ArrayList<>();

        for (Income income : incomes) {
            entries.add(new PieEntry((float) income.getAmount(), income.getSource()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Répartition des revenus");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(14f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(30f);
        pieChart.setTransparentCircleRadius(35f);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }
}
