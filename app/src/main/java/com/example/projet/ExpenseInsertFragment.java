package com.example.projet;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.projet.Model.Expense;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;
import java.util.UUID;

public class ExpenseInsertFragment extends Fragment {

    private ExpenseViewModel viewModel;
    private EditText editAmount, editNote;
    private Spinner spinnerCategory;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_expense_insert, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(ExpenseViewModel.class);

        editAmount = view.findViewById(R.id.editExpenseAmount);
        editNote = view.findViewById(R.id.editExpenseNote);
        spinnerCategory = view.findViewById(R.id.spinnerExpenseCategory);
        Button btnAdd = view.findViewById(R.id.btnAddExpense);

        // Lier le spinner aux catégories
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.expense_categories,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> {
            String amountStr = editAmount.getText().toString().trim();
            String note = editNote.getText().toString().trim();
            String category = spinnerCategory.getSelectedItem().toString();

            if (TextUtils.isEmpty(amountStr)) {
                editAmount.setError("Veuillez entrer un montant");
                return;
            }

            // 🔐 Récupérer l'UID de l'utilisateur connecté
            FirebaseAuth auth = FirebaseAuth.getInstance();
            String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

            if (userId == null) {
                Toast.makeText(getContext(), "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                String id = UUID.randomUUID().toString();
                long timestamp = System.currentTimeMillis();

                Expense expense = new Expense(id, amount, category, note, timestamp, userId);
                viewModel.addExpense(expense);

                Toast.makeText(getContext(), "Dépense ajoutée", Toast.LENGTH_SHORT).show();

                // Réinitialiser les champs
                editAmount.setText("");
                editNote.setText("");
                spinnerCategory.setSelection(0);

            } catch (NumberFormatException e) {
                editAmount.setError("Montant invalide");
            }
        });
    }
}
