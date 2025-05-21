package com.example.projet;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.projet.Model.Expense;
import com.example.projet.Model.Income;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReportFragment extends Fragment {

    private CombinedChart chartCombined;
    private LineChart chartTrend, chartMonthlyTrend;
    private PieChart chartCategories;
    private Button btnExportPdf;

    private IncomeViewModel incomeVM;
    private ExpenseViewModel expenseVM;

    private double totalIncome, totalExpense;
    private int countIncome, countExpense;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_report, container, false);

        chartCombined = root.findViewById(R.id.chartCombined);
        chartTrend = root.findViewById(R.id.chartTrend);
        chartCategories = root.findViewById(R.id.chartCategories);
        chartMonthlyTrend = root.findViewById(R.id.chartMonthlyTrend);
        btnExportPdf = root.findViewById(R.id.btnExportPdf);

        incomeVM = new ViewModelProvider(this).get(IncomeViewModel.class);
        expenseVM = new ViewModelProvider(this).get(ExpenseViewModel.class);

        incomeVM.getIncomeList().observe(getViewLifecycleOwner(), incomes ->
                renderCharts(incomes, expenseVM.getExpenses().getValue())
        );
        expenseVM.getExpenses().observe(getViewLifecycleOwner(), expenses ->
                renderCharts(incomeVM.getIncomeList().getValue(), expenses)
        );

        btnExportPdf.setOnClickListener(v -> createPdf());
        return root;
    }

    private void renderCharts(List<Income> incomes, List<Expense> expenses) {
        if (incomes == null || expenses == null) return;

        SimpleDateFormat fmt = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
        TreeMap<String, Float> incByMonth = new TreeMap<>();
        TreeMap<String, Float> expByMonth = new TreeMap<>();

        for (Income in : incomes) {
            String m = fmt.format(new Date(in.getTimestamp()));
            incByMonth.merge(m, (float) in.getAmount(), Float::sum);
        }
        for (Expense ex : expenses) {
            String m = fmt.format(new Date(ex.getTimestamp()));
            expByMonth.merge(m, (float) ex.getAmount(), Float::sum);
        }

        Set<String> monthsSet = new TreeSet<>();
        monthsSet.addAll(incByMonth.keySet());
        monthsSet.addAll(expByMonth.keySet());
        List<String> months = new ArrayList<>(monthsSet);

        totalIncome = incByMonth.values().stream().mapToDouble(Float::doubleValue).sum();
        totalExpense = expByMonth.values().stream().mapToDouble(Float::doubleValue).sum();
        countIncome = incomes.size();
        countExpense = expenses.size();

        // CombinedChart
        List<BarEntry> barInc = new ArrayList<>();
        List<BarEntry> barExp = new ArrayList<>();
        for (int i = 0; i < months.size(); i++) {
            barInc.add(new BarEntry(i, incByMonth.getOrDefault(months.get(i), 0f)));
            barExp.add(new BarEntry(i, expByMonth.getOrDefault(months.get(i), 0f)));
        }

        BarDataSet dsInc = new BarDataSet(barInc, "Revenus");
        dsInc.setColor(ColorTemplate.COLORFUL_COLORS[0]);
        dsInc.setValueTextColor(Color.BLACK);

        BarDataSet dsExp = new BarDataSet(barExp, "Dépenses");
        dsExp.setColor(ColorTemplate.COLORFUL_COLORS[1]);
        dsExp.setValueTextColor(Color.BLACK);

        BarData barData = new BarData(dsInc, dsExp);
        barData.setBarWidth(0.3f);

        CombinedData combinedData = new CombinedData();
        combinedData.setData(barData);

        chartCombined.setData(combinedData);
        chartCombined.getDescription().setEnabled(false);
        chartCombined.getAxisRight().setEnabled(false);

        XAxis xAxis = chartCombined.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(months.size());
        chartCombined.getLegend().setTextColor(Color.BLACK);
        chartCombined.invalidate();

        // LineChart - Cumulative Savings
        List<Entry> cumEntries = new ArrayList<>();
        float cumul = 0f;
        for (int i = 0; i < months.size(); i++) {
            float inc = incByMonth.getOrDefault(months.get(i), 0f);
            float exp = expByMonth.getOrDefault(months.get(i), 0f);
            cumul += (inc - exp);
            cumEntries.add(new Entry(i, cumul));
        }

        LineDataSet dsCum = new LineDataSet(cumEntries, "Économies cumulées");
        dsCum.setColor(ColorTemplate.MATERIAL_COLORS[2]);
        dsCum.setCircleColor(ColorTemplate.MATERIAL_COLORS[2]);
        dsCum.setValueTextColor(Color.BLACK);
        dsCum.setCircleRadius(4f);

        chartTrend.setData(new LineData(dsCum));
        styleSimpleLine(chartTrend);

        // PieChart
        Map<String, Float> catSum = new TreeMap<>();
        for (Expense e : expenses) {
            catSum.merge(e.category, (float) e.getAmount(), Float::sum);
        }

        List<PieEntry> pieEntries = new ArrayList<>();
        catSum.forEach((cat, val) -> pieEntries.add(new PieEntry(val, cat)));

        PieDataSet pieSet = new PieDataSet(pieEntries, "Dépenses par catégorie");
        pieSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
        pieSet.setValueTextColor(Color.BLACK);
        pieSet.setValueTextSize(12f);

        chartCategories.setData(new PieData(pieSet));
        chartCategories.setEntryLabelColor(Color.BLACK);
        chartCategories.getDescription().setEnabled(false);
        chartCategories.getLegend().setTextColor(Color.BLACK);
        chartCategories.invalidate();

        // MonthlyTrend
        List<Entry> lineIn = new ArrayList<>(), lineEx = new ArrayList<>();
        for (int i = 0; i < months.size(); i++) {
            String m = months.get(i);
            lineIn.add(new Entry(i, incByMonth.getOrDefault(m, 0f)));
            lineEx.add(new Entry(i, expByMonth.getOrDefault(m, 0f)));
        }

        LineDataSet mdi = new LineDataSet(lineIn, "Revenus mensuels");
        mdi.setColor(ColorTemplate.MATERIAL_COLORS[1]);
        mdi.setCircleColor(ColorTemplate.MATERIAL_COLORS[1]);
        mdi.setValueTextColor(Color.BLACK);
        mdi.setCircleRadius(3f);

        LineDataSet mde = new LineDataSet(lineEx, "Dépenses mensuelles");
        mde.setColor(ColorTemplate.MATERIAL_COLORS[2]);
        mde.setCircleColor(ColorTemplate.MATERIAL_COLORS[2]);
        mde.setValueTextColor(Color.BLACK);
        mde.setCircleRadius(3f);

        chartMonthlyTrend.setData(new LineData(mdi, mde));
        chartMonthlyTrend.getXAxis().setValueFormatter(new IndexAxisValueFormatter(months));
        styleSimpleLine(chartMonthlyTrend);
    }

    private void styleSimpleLine(LineChart lc) {
        lc.getDescription().setEnabled(false);
        lc.getAxisRight().setEnabled(false);
        lc.getLegend().setTextColor(Color.BLACK);
        lc.getXAxis().setTextColor(Color.BLACK);
        lc.getAxisLeft().setTextColor(Color.BLACK);
        lc.invalidate();
    }

    private void createPdf() {
        try {
            PdfDocument doc = new PdfDocument();
            Paint p = new Paint();
            p.setTextSize(14f);
            p.setColor(Color.BLACK);

            // Page 1: Résumé
            PdfDocument.PageInfo pageInfo1 = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page1 = doc.startPage(pageInfo1);
            Canvas c = page1.getCanvas();
            c.drawText("=== Rapport Financier Complet ===", 40, 40, p);
            c.drawText(String.format("Revenus totaux      : %.2f", totalIncome), 40, 80, p);
            c.drawText(String.format("Dépenses totales    : %.2f", totalExpense), 40, 110, p);
            c.drawText(String.format("Transactions (I/E)  : %d / %d", countIncome, countExpense), 40, 140, p);
            double savings = totalIncome - totalExpense;
            c.drawText(String.format("Économies nettes    : %.2f", savings), 40, 170, p);
            double savingsRate = totalIncome > 0 ? (savings / totalIncome) * 100 : 0;
            c.drawText(String.format("Taux d’épargne      : %.1f%%", savingsRate), 40, 200, p);
            doc.finishPage(page1);

            // Pages 2–5: Graphs
            Bitmap[] charts = new Bitmap[]{
                    chartCombined.getChartBitmap(),
                    chartTrend.getChartBitmap(),
                    chartCategories.getChartBitmap(),
                    chartMonthlyTrend.getChartBitmap()
            };
            for (int i = 0; i < charts.length; i++) {
                Bitmap bmp = charts[i];
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bmp.getWidth(), bmp.getHeight(), i + 2).create();
                PdfDocument.Page page = doc.startPage(pageInfo);
                page.getCanvas().drawBitmap(bmp, 0, 0, null);
                doc.finishPage(page);
            }

            File dir = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "reports");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "rapport_" + System.currentTimeMillis() + ".pdf");
            doc.writeTo(new FileOutputStream(file));
            doc.close();

            Toast.makeText(getContext(), "PDF enregistré : " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Erreur lors de l'export PDF", Toast.LENGTH_LONG).show();
        }
    }
}