package com.example.projet;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class IncomeFragment extends Fragment {

    private EditText editTextAmount, editTextSource;
    private Button buttonAddIncome;
    private IncomeViewModel incomeViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_income, container, false);

        editTextAmount = view.findViewById(R.id.editTextAmount);
        editTextSource = view.findViewById(R.id.editTextSource);
        buttonAddIncome = view.findViewById(R.id.buttonAddIncome);

        incomeViewModel = new ViewModelProvider(this).get(IncomeViewModel.class);

        // Observer les messages de succès et d'erreur
        incomeViewModel.getSuccessMessage().observe(getViewLifecycleOwner(), message -> {
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            editTextAmount.setText("");
            editTextSource.setText("");
        });

        incomeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        });

        buttonAddIncome.setOnClickListener(v -> {
            String source = editTextSource.getText().toString().trim();
            String amountStr = editTextAmount.getText().toString().trim();

            if (source.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(getActivity(), "Champs vides", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getActivity(), "Montant invalide", Toast.LENGTH_SHORT).show();
                return;
            }

            incomeViewModel.addIncome(source, amount);
        });

        return view;
    }
}
