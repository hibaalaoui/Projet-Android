package com.example.projet.Model;


public class Expense {
    public String id;
    public double amount;
    public String category;
    public String note;
    public String type = "expense"; // utile pour filtrer
    public long timestamp;
    public String userId;

    public Expense() {
        // Requis par Firebase ou Room
    }

    public Expense(String id, double amount, String category, String note, long timestamp, String userId) {
        this.id = id;
        this.amount = amount;
        this.category = category;
        this.note = note;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    // Tu peux ajouter getters/setters si tu utilises Room
}
