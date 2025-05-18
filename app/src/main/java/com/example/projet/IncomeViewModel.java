package com.example.projet;

import androidx.annotation.NonNull;
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

    private final MutableLiveData<List<Income>> incomeList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final DatabaseReference databaseRef;

    public IncomeViewModel() {
        // Référence vers le noeud "incomes" dans Firebase
        databaseRef = FirebaseDatabase.getInstance().getReference("incomes");

        // Écoute les changements dans Firebase pour récupérer toute la liste à jour
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Income> incomes = new ArrayList<>();
                for (DataSnapshot incomeSnapshot : snapshot.getChildren()) {
                    Income income = incomeSnapshot.getValue(Income.class);
                    if (income != null) {
                        incomes.add(income);
                    }
                }
                incomeList.setValue(incomes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                errorMessage.setValue("Erreur lors de la lecture des revenus : " + error.getMessage());
            }
        });
    }

    public LiveData<List<Income>> getIncomeList() {
        return incomeList;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void addIncome(String source, double amount) {
        if (amount <= 0) {
            errorMessage.setValue("Le montant doit être supérieur à zéro");
            return;
        }

        // Création d'un nouvel objet Income
        Income newIncome = new Income(source, amount);

        // Générer une clé unique dans Firebase pour cet income
        String key = databaseRef.push().getKey();

        if (key != null) {
            // Ajout dans Firebase Database
            databaseRef.child(key).setValue(newIncome)
                    .addOnSuccessListener(aVoid -> successMessage.setValue("Revenu ajouté avec succès"))
                    .addOnFailureListener(e -> errorMessage.setValue("Erreur ajout revenu : " + e.getMessage()));
        } else {
            errorMessage.setValue("Erreur interne lors de la création de la clé");
        }
    }
}
