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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class IncomeFragment extends Fragment {

    private EditText editTextAmount, editTextSource;
    private Button buttonAddIncome;
    private IncomeViewModel incomeViewModel;
    private RecyclerView recyclerViewIncomes;
    private IncomeAdapter incomeAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_income, container, false);

        editTextAmount = view.findViewById(R.id.editTextAmount);
        editTextSource = view.findViewById(R.id.editTextSource);
        buttonAddIncome = view.findViewById(R.id.buttonAddIncome);
        recyclerViewIncomes = view.findViewById(R.id.recyclerViewIncomes);

        // Setup RecyclerView
        recyclerViewIncomes.setLayoutManager(new LinearLayoutManager(getContext()));
        incomeAdapter = new IncomeAdapter(new ArrayList<>());
        recyclerViewIncomes.setAdapter(incomeAdapter);

        // Init ViewModel
        incomeViewModel = new ViewModelProvider(this).get(IncomeViewModel.class);

        // Observer messages succès et erreur
        incomeViewModel.getSuccessMessage().observe(getViewLifecycleOwner(), message -> {
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            editTextAmount.setText("");
            editTextSource.setText("");
        });

        incomeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        });

        // Observer la liste des revenus
        incomeViewModel.getIncomeList().observe(getViewLifecycleOwner(), incomes -> {
            incomeAdapter.setIncomeList(incomes);
            incomeAdapter.notifyDataSetChanged();
        });

        // Gestion du clic sur le bouton pour ajouter un revenu
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
