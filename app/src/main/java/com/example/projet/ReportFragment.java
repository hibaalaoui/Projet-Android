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
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class ReportFragment extends Fragment {

    private CombinedChart chartCombined;
    private LineChart     chartTrend, chartMonthlyTrend;
    private PieChart      chartCategories;
    private Button        btnExportPdf;

    private IncomeViewModel  incomeVM;
    private ExpenseViewModel expenseVM;

    // Statistiques globales
    private double totalIncome, totalExpense, totalSavings, savingsRate;
    private int    countIncome, countExpense;
    private String topCategory;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_report, container, false);

        chartCombined     = root.findViewById(R.id.chartCombined);
        chartTrend        = root.findViewById(R.id.chartTrend);
        chartCategories   = root.findViewById(R.id.chartCategories);
        chartMonthlyTrend = root.findViewById(R.id.chartMonthlyTrend);
        btnExportPdf      = root.findViewById(R.id.btnExportPdf);

        incomeVM  = new ViewModelProvider(this).get(IncomeViewModel.class);
        expenseVM = new ViewModelProvider(this).get(ExpenseViewModel.class);

        incomeVM.getIncomeList().observe(getViewLifecycleOwner(), list ->
                renderCharts(list, expenseVM.getExpenses().getValue())
        );
        expenseVM.getExpenses().observe(getViewLifecycleOwner(), list ->
                renderCharts(incomeVM.getIncomeList().getValue(), list)
        );

        btnExportPdf.setOnClickListener(v -> createPdf());
        return root;
    }

    private void renderCharts(List<Income> incomes, List<Expense> expenses) {
        if (incomes == null || expenses == null) return;

        // --- Calcul des stats globales
        totalIncome  = incomes.stream().mapToDouble(Income::getAmount).sum();
        totalExpense = expenses.stream().mapToDouble(Expense::getAmount).sum();
        totalSavings = totalIncome - totalExpense;
        countIncome  = incomes.size();
        countExpense = expenses.size();
        savingsRate  = totalIncome>0 ? (totalSavings/totalIncome)*100 : 0;

        // catégorie la plus coûteuse
        Map<String, Double> catSum = new LinkedHashMap<>();
        for (Expense e : expenses) {
            catSum.merge(e.category, e.getAmount(), Double::sum);
        }
        topCategory = catSum.entrySet().stream()
                .max(Comparator.comparing(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse("—");

        // --- 1) CombinedChart : barres Revenus vs Dépenses + courbe Épargne
        List<BarEntry> inEntries = List.of(new BarEntry(0f, (float) totalIncome));
        List<BarEntry> exEntries = List.of(new BarEntry(1f, (float) totalExpense));

        BarDataSet dsIn = new BarDataSet(inEntries, "Revenus");
        dsIn.setColor(ColorTemplate.COLORFUL_COLORS[0]);
        dsIn.setValueTextColor(Color.BLACK);

        BarDataSet dsEx = new BarDataSet(exEntries, "Dépenses");
        dsEx.setColor(ColorTemplate.COLORFUL_COLORS[1]);
        dsEx.setValueTextColor(Color.BLACK);

        BarData barData = new BarData(dsIn, dsEx);
        barData.setBarWidth(0.2f);

        List<Entry> svEntries = List.of(
                new Entry(0f, (float) totalSavings),
                new Entry(1f, (float) totalSavings)
        );
        LineDataSet dsSv = new LineDataSet(svEntries, "Épargne");
        dsSv.setColor(ColorTemplate.MATERIAL_COLORS[0]);
        dsSv.setCircleColor(ColorTemplate.MATERIAL_COLORS[0]);
        dsSv.setValueTextColor(Color.BLACK);
        dsSv.setCircleRadius(4f);

        LineData lineData = new LineData(dsSv);

        CombinedData comb = new CombinedData();
        comb.setData(barData);
        comb.setData(lineData);

        chartCombined.setData(comb);
        chartCombined.getDescription().setEnabled(false);
        chartCombined.getAxisRight().setEnabled(false);

        XAxis x = chartCombined.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(1f);
        x.setValueFormatter(new IndexAxisValueFormatter(
                new String[]{"Revenus","Dépenses"}));
        x.setTextColor(Color.BLACK);

        Legend lc = chartCombined.getLegend();
        lc.setTextColor(Color.BLACK);

        chartCombined.invalidate();

        // --- 2) LineChart : tendance de l'épargne
        chartTrend.setData(lineData);
        styleSimpleLine(chartTrend);

        // --- 3) PieChart : catégories de dépenses
        List<PieEntry> pieEntries = new ArrayList<>();
        catSum.forEach((cat, sum) -> pieEntries.add(
                new PieEntry(sum.floatValue(), cat))
        );

        PieDataSet ps = new PieDataSet(pieEntries, "Dépenses par catégorie");
        ps.setColors(ColorTemplate.VORDIPLOM_COLORS);
        ps.setValueTextColor(Color.BLACK);
        ps.setValueTextSize(12f);

        PieData pData = new PieData(ps);
        chartCategories.setData(pData);
        chartCategories.setEntryLabelColor(Color.BLACK);
        chartCategories.getDescription().setEnabled(false);
        chartCategories.getLegend().setTextColor(Color.BLACK);
        chartCategories.invalidate();

        // --- 4) LineChart mensuel : revenus vs dépenses
        TreeMap<String, Float> incByMonth = new TreeMap<>(),
                expByMonth = new TreeMap<>();
        SimpleDateFormat fmt = new SimpleDateFormat("MM/yyyy", Locale.getDefault());

        for (Income in : incomes) {
            String m = fmt.format(new Date(in.getTimestamp()));
            incByMonth.merge(m, (float)in.getAmount(), Float::sum);
        }
        for (Expense ex : expenses) {
            String m = fmt.format(new Date(ex.getTimestamp()));
            expByMonth.merge(m, (float)ex.getAmount(), Float::sum);
        }

// Construire l'ensemble trié des mois (clés)
        Set<String> monthSet = new TreeSet<>();
        monthSet.addAll(incByMonth.keySet());
        monthSet.addAll(expByMonth.keySet());

// Transformer en liste
        List<String> months = new ArrayList<>(monthSet);


        List<Entry> lineIn = new ArrayList<>(), lineEx = new ArrayList<>();
        for (int i=0; i<months.size(); i++) {
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

        LineData monthlyData = new LineData(mdi, mde);
        chartMonthlyTrend.setData(monthlyData);
        chartMonthlyTrend.getXAxis().setValueFormatter(
                new IndexAxisValueFormatter(months));
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

            // Page 1 : résumé avec taux & top-cat
            PdfDocument.PageInfo i1 =
                    new PdfDocument.PageInfo.Builder(595,842,1).create();
            var pg1 = doc.startPage(i1);
            Canvas c = pg1.getCanvas();
            c.drawText("=== Rapport Financier ===",40,40,p);
            c.drawText(String.format("Revenus      : %.2f", totalIncome),40,80,p);
            c.drawText(String.format("Dépenses     : %.2f", totalExpense),40,110,p);
            c.drawText(String.format("Économies    : %.2f", totalSavings),40,140,p);
            c.drawText(String.format("Taux économisé : %.1f%%", savingsRate),40,170,p);
            c.drawText("Catégorie la + coûteuse : "+topCategory,40,200,p);
            c.drawText("Opérations I/E : "+countIncome+" / "+countExpense,40,230,p);
            doc.finishPage(pg1);

            // Pages 2–5 : captures des 4 graphiques
            for (int idx=0; idx<4; idx++) {
                Bitmap bmp = switch(idx) {
                    case 0 -> chartCombined.getChartBitmap();
                    case 1 -> chartTrend.getChartBitmap();
                    case 2 -> chartCategories.getChartBitmap();
                    default -> chartMonthlyTrend.getChartBitmap();
                };
                PdfDocument.PageInfo ip =
                        new PdfDocument.PageInfo.Builder(
                                bmp.getWidth(), bmp.getHeight(), idx+2
                        ).create();
                var pg = doc.startPage(ip);
                pg.getCanvas().drawBitmap(bmp,0,0,null);
                doc.finishPage(pg);
            }

            File d = new File(
                    requireContext()
                            .getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                    "reports"
            );
            if (!d.exists()) d.mkdirs();
            File f = new File(d, "rapport_"+System.currentTimeMillis()+".pdf");
            doc.writeTo(new FileOutputStream(f));
            doc.close();
            Toast.makeText(getContext(),
                    "PDF enregistré : "+f.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(),
                    "Erreur export PDF", Toast.LENGTH_LONG).show();
        }
    }
}
