package com.example.projet;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.projet.Model.Expense;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ExpenseViewModel extends ViewModel {

    private final MutableLiveData<List<Expense>> expenses = new MutableLiveData<>(new ArrayList<>());
    private final MediatorLiveData<Double> totalAmount = new MediatorLiveData<>();

    private final DatabaseReference dbRef;
    private final FirebaseAuth auth;

    public ExpenseViewModel() {
        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("expenses");

        // Lier le calcul du total à chaque mise à jour de la liste
        totalAmount.addSource(expenses, list -> {
            double total = 0;
            for (Expense e : list) total += e.amount;
            totalAmount.setValue(total);
        });

        fetchUserExpenses();
    }

    public LiveData<List<Expense>> getExpenses() {
        return expenses;
    }

    public LiveData<Double> getTotalAmount() {
        return totalAmount;
    }

    public void addExpense(Expense expense) {
        String key = dbRef.push().getKey();
        if (key != null && expense.userId != null) {
            expense.id = key;
            dbRef.child(key).setValue(expense);
        }
    }

    public void deleteExpense(Expense expense) {
        if (expense.id != null) {
            dbRef.child(expense.id).removeValue();
        }
    }

    public void clearExpenses() {
        expenses.setValue(new ArrayList<>());
    }

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
                        expenses.setValue(userExpenses); // mettra aussi à jour totalAmount
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Log/Toast en cas d’erreur
                    }
                });
    }
}
