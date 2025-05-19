package com.example.projet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.List;
import java.util.Locale;

public class SavingsFragment extends Fragment {
    private SavingsViewModel viewModel;
    private TextView tvSavings;
    private LineChart chartSavings;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_savings, container, false);
        tvSavings    = view.findViewById(R.id.tv_savings_value);
        chartSavings = view.findViewById(R.id.chart_savings);

        viewModel = new ViewModelProvider(this).get(SavingsViewModel.class);
        viewModel.getSavings().observe(getViewLifecycleOwner(), amount -> {
            tvSavings.setText(String.format(Locale.getDefault(), "Épargne : %.2f", amount));
        });
        viewModel.getChartEntries().observe(getViewLifecycleOwner(), entries -> {
            LineDataSet set = new LineDataSet(entries, "Épargne cumulée");
            chartSavings.setData(new LineData(set));
            chartSavings.invalidate();
        });
        return view;
    }
}
