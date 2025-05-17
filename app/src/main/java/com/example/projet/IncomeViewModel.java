package com.example.projet;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.projet.Model.Income;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class IncomeViewModel extends ViewModel {

    private MutableLiveData<String> successMessage = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private DatabaseReference incomeRef;

    public IncomeViewModel() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            incomeRef = FirebaseDatabase.getInstance().getReference("incomes").child(user.getUid());
        }
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
}
