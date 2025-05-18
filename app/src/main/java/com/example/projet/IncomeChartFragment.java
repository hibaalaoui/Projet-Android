package com.example.projet;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;
public class IncomeChartFragment extends Fragment {

    private BarChart barChart;
    private PieChart pieChart;
    private IncomeViewModel incomeViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_income_chart, container, false);

        barChart = view.findViewById(R.id.barChart);
        pieChart = view.findViewById(R.id.pieChart);

        incomeViewModel = new ViewModelProvider(requireActivity()).get(IncomeViewModel.class);

        // Observer la liste depuis Firebase en temps réel
        incomeViewModel.getIncomeList().observe(getViewLifecycleOwner(), incomes -> {
            updateBarChart(incomes);
            updatePieChart(incomes);
        });

        return view;
    }

    private void updateBarChart(List<Income> incomes) {
        if (incomes == null || incomes.isEmpty()) {
            barChart.clear();
            barChart.invalidate();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < incomes.size(); i++) {
            Income income = incomes.get(i);
            if (income != null) {
                entries.add(new BarEntry(i, (float) income.getAmount()));
                labels.add(income.getSource() != null ? income.getSource() : "Source inconnue");
            }
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
        if (incomes == null || incomes.isEmpty()) {
            pieChart.clear();
            pieChart.invalidate();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();

        for (Income income : incomes) {
            if (income != null) {
                entries.add(new PieEntry((float) income.getAmount(), income.getSource() != null ? income.getSource() : "Source inconnue"));
            }
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


