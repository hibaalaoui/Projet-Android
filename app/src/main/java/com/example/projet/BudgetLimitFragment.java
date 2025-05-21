package com.example.projet;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.projet.Model.BudgetLimit;
import com.google.firebase.auth.FirebaseAuth;

import java.util.UUID;

public class BudgetLimitFragment extends Fragment {

    private BudgetLimitViewModel viewModel;
    private Spinner spinnerCategory;
    private EditText editAmount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_budget_limit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(BudgetLimitViewModel.class);

        spinnerCategory = view.findViewById(R.id.spinnerLimitCategory);
        editAmount = view.findViewById(R.id.editLimitAmount);
        Button btnSave = view.findViewById(R.id.btnSaveLimit);

        // Charger les catégories
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.expense_categories,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        btnSave.setOnClickListener(v -> {
            String amountStr = editAmount.getText().toString().trim();
            String category = spinnerCategory.getSelectedItem().toString();
            String uid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                    FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

            if (uid == null) {
                Toast.makeText(getContext(), "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(amountStr)) {
                editAmount.setError("Entrez un montant");
                return;
            }

            double amount = Double.parseDouble(amountStr);
            BudgetLimit limit = new BudgetLimit(UUID.randomUUID().toString(), category, amount, uid);
            viewModel.addOrUpdateLimit(limit);
            Toast.makeText(getContext(), "Plafond enregistré", Toast.LENGTH_SHORT).show();

            editAmount.setText("");
            spinnerCategory.setSelection(0);
        });
    }
}
