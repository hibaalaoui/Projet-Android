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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class ReportFragment extends Fragment {

    private CombinedChart chartCombined;
    private LineChart chartTrend, chartMonthlyTrend;
    private PieChart chartCategories;
    private Button btnExportPdf;

    private IncomeViewModel incomeVM;
    private ExpenseViewModel expenseVM;

    // Pour le résumé PDF
    private double totalIncome, totalExpense;
    private int countIncome, countExpense;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_report, container, false);

        chartCombined = root.findViewById(R.id.chartCombined);
        chartTrend = root.findViewById(R.id.chartTrend);
        chartCategories = root.findViewById(R.id.chartCategories);
        chartMonthlyTrend = root.findViewById(R.id.chartMonthlyTrend);
        btnExportPdf = root.findViewById(R.id.btnExportPdf);

        incomeVM = new ViewModelProvider(this).get(IncomeViewModel.class);
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

        // --- 0) Regrouper par mois
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

        // liste triée des mois
        Set<String> monthsSet = new TreeSet<>();
        monthsSet.addAll(incByMonth.keySet());
        monthsSet.addAll(expByMonth.keySet());
        List<String> months = new ArrayList<>(monthsSet);

        // statistiques globales pour le PDF
        totalIncome = incByMonth.values().stream().mapToDouble(v -> v).sum();
        totalExpense = expByMonth.values().stream().mapToDouble(v -> v).sum();
        countIncome = incomes.size();
        countExpense = expenses.size();


        List<BarEntry> barInc = new ArrayList<>();
        List<BarEntry> barExp = new ArrayList<>();

        for (int i = 0; i < months.size(); i++) {
            String month = months.get(i);
            barInc.add(new BarEntry(i, incByMonth.getOrDefault(month, 0f)));
            barExp.add(new BarEntry(i, expByMonth.getOrDefault(month, 0f)));
        }
        BarDataSet dsInc = new BarDataSet(barInc, "Revenus");
        BarDataSet dsExp = new BarDataSet(barExp, "Dépenses");


// 1) Préparez votre BarData avec 2 DataSets (revenus et dépenses)
        dsInc.setColor(ColorTemplate.COLORFUL_COLORS[0]);
        dsInc.setValueTextColor(Color.BLACK);

        dsExp.setColor(ColorTemplate.COLORFUL_COLORS[1]);
        dsExp.setValueTextColor(Color.BLACK);

// 2) Créez le BarData et réglez largeur + espacement
        BarData barData = new BarData(dsInc, dsExp);
        float groupSpace = 0.2f;
        float barSpace = 0.05f;
        float barWidth = (1f - groupSpace) / 2f;
        barData.setBarWidth(barWidth);

// **C’est ici qu’on “groupe” les barres**
//                               fromX = 0f
        barData.groupBars(0f, groupSpace, barSpace);

// 3) Injectez dans le CombinedData (vous pouvez y ajouter d’autres séries si besoin)
        CombinedData combinedData = new CombinedData();
        combinedData.setData(barData);

// 4) Configurez le CombinedChart
        chartCombined.setData(combinedData);
        chartCombined.getDescription().setEnabled(false);
        chartCombined.getAxisRight().setEnabled(false);

// 5) Ajustez l’axe X pour qu’il couvre exactement tous les groupes
        XAxis x = chartCombined.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(1f);
        x.setValueFormatter(new IndexAxisValueFormatter(months));
        x.setAxisMinimum(0f);
// largeur totale = largeur d’un groupe × nombre de groupes
        float totalGroupWidth = combinedData.getBarData().getGroupWidth(groupSpace, barSpace) * months.size();
        x.setAxisMaximum(totalGroupWidth);

// 6) Légende et redraw
        chartCombined.getLegend().setTextColor(Color.BLACK);
        chartCombined.invalidate();


        // --- 2) LineChart : économies cumulées mois par mois
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

        LineData lineCum = new LineData(dsCum);
        chartTrend.setData(lineCum);
        styleSimpleLine(chartTrend);

        // --- 3) PieChart : catégories de dépenses (inchangé)
        List<PieEntry> pieEntries = new ArrayList<>();
        // on re-calcule ici la répartition par catégorie
        TreeMap<String, Float> catSum = new TreeMap<>();
        for (Expense e : expenses) {
            catSum.merge(e.category, (float) e.getAmount(), Float::sum);
        }
        catSum.forEach((cat, val) -> pieEntries.add(new PieEntry(val, cat)));

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

        // --- 4) MonthlyTrend (inchangé)
        // … votre code existant pour chartMonthlyTrend …

        for (Income in : incomes) {
            String m = fmt.format(new Date(in.getTimestamp()));
            incByMonth.merge(m, (float) in.getAmount(), Float::sum);
        }
        for (Expense ex : expenses) {
            String m = fmt.format(new Date(ex.getTimestamp()));
            expByMonth.merge(m, (float) ex.getAmount(), Float::sum);
        }

// Construire l'ensemble trié des mois (clés)
        Set<String> monthSet = new TreeSet<>();
        monthSet.addAll(incByMonth.keySet());
        monthSet.addAll(expByMonth.keySet());

// Transformer en liste


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

            // --- Page 1: Résumé Statistique
            PdfDocument.PageInfo pageInfo1 = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page1 = doc.startPage(pageInfo1);
            Canvas c = page1.getCanvas();
            c.drawText("=== Rapport Financier Complet ===", 40, 40, p);
            c.drawText(String.format("Revenus totaux      : %.2f", totalIncome), 40, 80, p);
            c.drawText(String.format("Dépenses totales    : %.2f", totalExpense), 40, 110, p);
            c.drawText(String.format("Transactions (I/E)   : %d / %d", countIncome, countExpense), 40, 140, p);
            double savings = totalIncome - totalExpense;
            c.drawText(String.format("Économies nettes     : %.2f", savings), 40, 170, p);
            double savingsRate = totalIncome > 0 ? (savings / totalIncome) * 100 : 0;
            c.drawText(String.format("Taux d’épargne       : %.1f%%", savingsRate), 40, 200, p);
            doc.finishPage(page1);

            // --- Pages 2–5: Graphiques (chartCombined, chartTrend, chartCategories, chartMonthlyTrend)
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

            // --- Save the PDF
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