package com.example.projet.Model;

public class Income {
    private String id;
    private String source;
    private double amount;

    // Constructeur vide requis par Firebase
    public Income() {
        // Requis pour Firebase
    }

    public Income(String id, String source, double amount) {
        this.id = id;
        this.source = source;
        this.amount = amount;
    }

    // Getters et setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
