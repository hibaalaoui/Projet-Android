package com.example.projet;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class ExpenseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        Button btnInsert = findViewById(R.id.btnGoToInsert);
        Button btnList = findViewById(R.id.btnGoToList);

        btnInsert.setOnClickListener(v -> loadFragment(new ExpenseInsertFragment()));
        btnList.setOnClickListener(v -> loadFragment(new ExpenseListFragment()));

        // Afficher le fragment d'ajout par défaut
        loadFragment(new ExpenseInsertFragment());
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.expenseFragmentContainer, fragment)
                .commit();
    }
}
