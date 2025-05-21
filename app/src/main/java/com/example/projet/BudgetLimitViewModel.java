package com.example.projet;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.projet.Model.BudgetLimit;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class BudgetLimitViewModel extends ViewModel {

    private final MutableLiveData<List<BudgetLimit>> limits = new MutableLiveData<>(new ArrayList<>());
    private final DatabaseReference dbRef;
    private final FirebaseAuth auth;

    public BudgetLimitViewModel() {
        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("budgetLimits");
        fetchUserLimits();
    }

    public LiveData<List<BudgetLimit>> getLimits() {
        return limits;
    }

    public void addOrUpdateLimit(BudgetLimit limit) {
        if (limit.userId == null) return;

        String key = limit.category; // Utiliser la catégorie comme ID unique
        dbRef.child(limit.userId).child(key).setValue(limit);
    }

    private void fetchUserLimits() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) return;

        dbRef.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<BudgetLimit> result = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    BudgetLimit limit = child.getValue(BudgetLimit.class);
                    if (limit != null) result.add(limit);
                }
                limits.setValue(result);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
