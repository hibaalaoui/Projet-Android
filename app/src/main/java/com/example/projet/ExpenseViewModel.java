package com.example.projet;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.projet.Model.BudgetLimit;
import com.example.projet.Model.Expense;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseViewModel extends ViewModel {

    private final MutableLiveData<List<Expense>> expenses = new MutableLiveData<>(new ArrayList<>());
    private final MediatorLiveData<Double> totalAmount = new MediatorLiveData<>();
    private final MutableLiveData<Map<String, Boolean>> limitExceeded = new MutableLiveData<>();
    private final MutableLiveData<List<BudgetLimit>> limits = new MutableLiveData<>(new ArrayList<>());

    private final Map<String, Double> categoryLimits = new HashMap<>();

    private final DatabaseReference dbRef;
    private final FirebaseAuth auth;

    public ExpenseViewModel() {
        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("expenses");

        // Total calculé automatiquement
        totalAmount.addSource(expenses, list -> {
            double total = 0;
            for (Expense e : list) total += e.amount;
            totalAmount.setValue(total);
        });

        fetchUserExpenses();
        fetchLimitsFromFirebase(); // ✅ charger les limites dès le départ
    }

    // 🔄 Dépenses utilisateur
    private void fetchUserExpenses() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) return;

        dbRef.orderByChild("userId").equalTo(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Expense> userExpenses = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Expense e = child.getValue(Expense.class);
                            if (e != null) userExpenses.add(e);
                        }
                        expenses.setValue(userExpenses);
                        checkLimitExceeding(userExpenses);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    // 🔄 Limites par catégorie
    public void fetchLimitsFromFirebase() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) return;

        DatabaseReference limitRef = FirebaseDatabase.getInstance().getReference("budgetLimits").child(uid);
        limitRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<BudgetLimit> loadedLimits = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    BudgetLimit limit = child.getValue(BudgetLimit.class);
                    if (limit != null) loadedLimits.add(limit);
                }
                limits.setValue(loadedLimits);
                loadCategoryLimits(loadedLimits); // met à jour la Map
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public void loadCategoryLimits(List<BudgetLimit> limitsList) {
        categoryLimits.clear();
        for (BudgetLimit limit : limitsList) {
            categoryLimits.put(limit.category, limit.limitAmount);
        }
        if (expenses.getValue() != null) {
            checkLimitExceeding(expenses.getValue());
        }
    }

    // ⚠️ Dépassement de limite
    private void checkLimitExceeding(List<Expense> expensesList) {
        Map<String, Double> totals = new HashMap<>();
        for (Expense e : expensesList) {
            totals.put(e.category, totals.getOrDefault(e.category, 0.0) + e.amount);
        }

        Map<String, Boolean> exceeded = new HashMap<>();
        for (String category : totals.keySet()) {
            double total = totals.get(category);
            double limit = categoryLimits.getOrDefault(category, Double.MAX_VALUE);
            exceeded.put(category, total > limit);
        }

        limitExceeded.setValue(exceeded);
    }

    // ➕ Ajouter une dépense
    public void addExpense(Expense expense) {
        String key = dbRef.push().getKey();
        if (key != null && expense.userId != null) {
            expense.id = key;
            dbRef.child(key).setValue(expense);
        }
    }

    // ❌ Supprimer une dépense
    public void deleteExpense(Expense expense) {
        if (expense.id != null) {
            dbRef.child(expense.id).removeValue();
        }
    }

    public void clearExpenses() {
        expenses.setValue(new ArrayList<>());
    }

    // 🧠 Observables publics
    public LiveData<List<Expense>> getExpenses() {
        return expenses;
    }

    public LiveData<Double> getTotalAmount() {
        return totalAmount;
    }

    public LiveData<Map<String, Boolean>> getLimitExceeded() {
        return limitExceeded;
    }

    public LiveData<List<BudgetLimit>> getLimits() {
        return limits;
    }
}
