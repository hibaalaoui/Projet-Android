package com.example.projet;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.projet.Model.Income;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class IncomeViewModel extends ViewModel {

    private MutableLiveData<String> successMessage = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<List<Income>> incomeListLiveData = new MutableLiveData<>();
    private DatabaseReference incomeRef;

    private ValueEventListener incomeListener;

    public IncomeViewModel() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            incomeRef = FirebaseDatabase.getInstance().getReference("incomes").child(user.getUid());
            listenToIncomeChanges();
        }
    }

    private void listenToIncomeChanges() {
        incomeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Income> incomes = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Income income = childSnapshot.getValue(Income.class);
                    if (income != null) {
                        incomes.add(income);
                    }
                }
                incomeListLiveData.setValue(incomes);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                errorMessage.setValue("Erreur lors de la lecture des données : " + error.getMessage());
            }
        };
        incomeRef.addValueEventListener(incomeListener);
    }

    public LiveData<List<Income>> getIncomeList() {
        return incomeListLiveData;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void addIncome(String source, double amount) {
        if (incomeRef == null) {
            errorMessage.setValue("Utilisateur non authentifié");
            return;
        }

        String id = incomeRef.push().getKey();
        Income income = new Income(id, source, amount);

        incomeRef.child(id).setValue(income)
                .addOnSuccessListener(unused -> successMessage.setValue("Revenu ajouté"))
                .addOnFailureListener(e -> errorMessage.setValue("Erreur : " + e.getMessage()));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (incomeRef != null && incomeListener != null) {
            incomeRef.removeEventListener(incomeListener);
        }
    }
}

