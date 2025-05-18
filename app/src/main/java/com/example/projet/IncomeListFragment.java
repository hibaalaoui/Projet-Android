package com.example.projet;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
public class IncomeListFragment extends Fragment {

    private RecyclerView recyclerViewIncomes;
    private IncomeAdapter incomeAdapter;
    private IncomeViewModel incomeViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_income_list, container, false);

        recyclerViewIncomes = view.findViewById(R.id.recyclerViewIncomes);
        recyclerViewIncomes.setLayoutManager(new LinearLayoutManager(getContext()));
        incomeAdapter = new IncomeAdapter(new ArrayList<>());
        recyclerViewIncomes.setAdapter(incomeAdapter);

        incomeViewModel = new ViewModelProvider(requireActivity()).get(IncomeViewModel.class);

        // Observer la liste depuis Firebase en temps réel
        incomeViewModel.getIncomeList().observe(getViewLifecycleOwner(), incomes -> {
            incomeAdapter.setIncomeList(incomes);
        });

        return view;
    }
}
