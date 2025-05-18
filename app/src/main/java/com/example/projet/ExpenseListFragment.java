package com.example.projet;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet.Model.Expense;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.UUID;

public class ExpenseListFragment extends Fragment {

    private ExpenseViewModel viewModel;
    private ExpenseAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(ExpenseViewModel.class);

        viewModel.addExpense(new Expense(
                UUID.randomUUID().toString(),
                99.99,
                "Transport",
                "Test Dépense",
                System.currentTimeMillis(),
                "mock-user"
        ));

        return inflater.inflate(R.layout.fragment_expense_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerExpenses);
        adapter = new ExpenseAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(requireActivity()).get(ExpenseViewModel.class);

        viewModel.getExpenses().observe(getViewLifecycleOwner(), expenses -> {
            adapter.submitList(expenses);
        });
        FloatingActionButton fab = view.findViewById(R.id.fabAddExpense);
        fab.setOnClickListener(v -> {
            // Ouvrir le formulaire d’ajout
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_frame, new ExpenseInsertFragment())
                    .addToBackStack(null)
                    .commit();
        });

        Button btnChart = view.findViewById(R.id.btnShowChart);
        btnChart.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_frame, new ExpenseChartFragment())
                    .addToBackStack(null)
                    .commit();
        });


    }
}
