package com.example.projet;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.projet.Model.Expense;
import com.example.projet.Model.Income;
import com.github.mikephil.charting.data.Entry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SavingsViewModel extends ViewModel {
    private final MutableLiveData<Double> savings = new MutableLiveData<>(0.0);
    private final MutableLiveData<List<Entry>> chartEntries = new MutableLiveData<>(new ArrayList<>());

    public LiveData<Double> getSavings() { return savings; }
    public LiveData<List<Entry>> getChartEntries() { return chartEntries; }

    public SavingsViewModel() {
        DatabaseReference incRef = FirebaseDatabase.getInstance().getReference("incomes");
        DatabaseReference expRef = FirebaseDatabase.getInstance().getReference("expenses");

        // One listener that re-computes whenever incomes OR expenses change
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Load both lists sequentially
                loadAndCompute(incRef, expRef);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };
        incRef.addValueEventListener(listener);
        expRef.addValueEventListener(listener);
    }

    private void loadAndCompute(DatabaseReference incRef, DatabaseReference expRef) {
        incRef.get().addOnSuccessListener(snap1 -> {
            List<Income> incomes = new ArrayList<>();
            for (DataSnapshot c : snap1.getChildren()) {
                Income i = c.getValue(Income.class);
                if (i != null) incomes.add(i);
            }
            expRef.get().addOnSuccessListener(snap2 -> {
                List<Expense> expenses = new ArrayList<>();
                for (DataSnapshot c2 : snap2.getChildren()) {
                    Expense e = c2.getValue(Expense.class);
                    if (e != null) expenses.add(e);
                }
                compute(incomes, expenses);
            });
        });
    }

    private void compute(List<Income> incomes, List<Expense> expenses) {
        // 1) Total savings
        double totalI = 0, totalE = 0;
        for (Income i : incomes)  totalI += i.getAmount();
        for (Expense e : expenses) totalE += e.getAmount();
        savings.postValue(totalI - totalE);

        // 2) Build a date-keyed map using Calendar
        Map<String, Double> map = new TreeMap<>();
        Calendar cal = Calendar.getInstance();
        for (Income i : incomes) {
            Date d = new Date(i.getTimestamp());
            cal.setTime(d);
            String key = String.format(
                    "%04d-%02d-%02d",
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH)
            );
            map.merge(key, i.getAmount(), Double::sum);
        }
        for (Expense e : expenses) {
            Date d = new Date(e.getTimestamp());
            cal.setTime(d);
            String key = String.format(
                    "%04d-%02d-%02d",
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH)
            );
            map.merge(key, -e.getAmount(), Double::sum);
        }

        // 3) Turn that into a cumulative list of chart Entries
        List<Entry> entries = new ArrayList<>();
        double cum = 0;
        int idx = 0;
        for (Double delta : map.values()) {
            cum += delta;
            entries.add(new Entry(idx++, (float) cum));
        }
        chartEntries.postValue(entries);
    }
}
