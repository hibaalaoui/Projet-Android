package com.example.projet;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.projet.Model.Expense;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ExpenseViewModel extends ViewModel {

    private final MutableLiveData<List<Expense>> expenses = new MutableLiveData<>(new ArrayList<>());
    private final DatabaseReference dbRef;

    public ExpenseViewModel() {
        dbRef = FirebaseDatabase.getInstance().getReference("expenses");
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

        // 🔥 Envoi vers Firebase
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
        expenses.setValue(new ArrayList<>());
    }
}
