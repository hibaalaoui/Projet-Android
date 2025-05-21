package com.example.projet;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.projet.Model.Expense;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ExpenseViewModel extends ViewModel {

    private final MutableLiveData<List<Expense>> expenses = new MutableLiveData<>(new ArrayList<>());
    private final DatabaseReference dbRef;

    public ExpenseViewModel() {
        dbRef = FirebaseDatabase.getInstance().getReference("expenses");

        // 🔥 On ajoute un listener pour récupérer toutes les dépenses
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Expense> loaded = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Expense e = child.getValue(Expense.class);
                    if (e != null) {
                        e.id = child.getKey();
                        loaded.add(e);
                    }
                }
                expenses.setValue(loaded);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Vous pouvez logger l'erreur ici
            }
        });
    }

    public LiveData<List<Expense>> getExpenses() {
        return expenses;
    }

    public void addExpense(Expense expense) {
        // Ajout local
        List<Expense> currentList = expenses.getValue();
        if (currentList != null) {
            currentList.add(expense);
            expenses.setValue(currentList);
        }

        // Envoi vers Firebase
        String key = dbRef.push().getKey();
        if (key != null) {
            expense.id = key;
            dbRef.child(key).setValue(expense);
        }
    }

    public void deleteExpense(Expense expense) {
        List<Expense> currentList = expenses.getValue();
        if (currentList != null) {
            currentList.remove(expense);
            expenses.setValue(currentList);
        }

        if (expense.id != null) {
            dbRef.child(expense.id).removeValue();
        }
    }

    public void clearExpenses() {
        dbRef.removeValue();
        expenses.setValue(new ArrayList<>());
    }

    public LiveData<Object> getTotalAmount() {
        MutableLiveData<Object> totalAmount = new MutableLiveData<>();
        expenses.observeForever(expensesList -> {
            float total = 0;
            for (Expense expense : expensesList) {
                total += expense.amount;
            }
            totalAmount.setValue(total);
        });
        return totalAmount;
    }
}