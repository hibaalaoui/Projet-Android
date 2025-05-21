package com.example.projet.Model;

public class BudgetLimit {
    public String id;
    public String category;
    public double limitAmount;
    public String userId;

    public BudgetLimit() {
        // Requis pour Firebase
    }

    public BudgetLimit(String id, String category, double limitAmount, String userId) {
        this.id = id;
        this.category = category;
        this.limitAmount = limitAmount;
        this.userId = userId;
    }

    // Getters et setters si nécessaire (facultatif avec Firebase)
}
