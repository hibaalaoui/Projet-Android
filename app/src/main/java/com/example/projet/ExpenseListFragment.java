package com.example.projet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet.Model.BudgetLimit;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ExpenseListFragment extends Fragment {

    private ExpenseViewModel viewModel;
    private ExpenseAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_expense_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recyclerExpenses);
        adapter = new ExpenseAdapter(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(ExpenseViewModel.class);

        // Observer les dépenses
        viewModel.getExpenses().observe(getViewLifecycleOwner(), adapter::submitList);

        // Observer les dépassements de plafond
        viewModel.getLimitExceeded().observe(getViewLifecycleOwner(), adapter::setLimitExceededMap);

        // Observer les plafonds pour les afficher
        viewModel.getLimits().observe(getViewLifecycleOwner(), limits -> {
            Map<String, Double> limitMap = new HashMap<>();
            for (BudgetLimit limit : limits) {
                limitMap.put(limit.category, limit.limitAmount);
            }
            adapter.setLimitMap(limitMap);
        });

        // Bouton Ajouter une dépense
        FloatingActionButton fab = view.findViewById(R.id.fabAddExpense);
        fab.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_frame, new ExpenseInsertFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Bouton graphique
        FloatingActionButton btnChart = view.findViewById(R.id.btnShowChart);
        btnChart.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_frame, new ExpenseChartFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Afficher le total
        TextView totalText = view.findViewById(R.id.textTotalAmount);
        viewModel.getTotalAmount().observe(getViewLifecycleOwner(), total -> {
            if (totalText != null) {
                totalText.setText(String.format(Locale.getDefault(), "Total : %.2f DH", total));
            }
        });
    }
}
