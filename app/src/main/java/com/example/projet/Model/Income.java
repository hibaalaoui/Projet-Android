package com.example.projet.Model;

public class Income {
    private String source;
    private double amount;

    // Constructeur vide requis par Firebase
    public Income() {
    }

    public Income(String source, double amount) {
        this.source = source;
        this.amount = amount;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
