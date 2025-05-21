package com.example.projet.Model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    // Getters (ajoutez-en d'autres si nécessaire)
    public double getAmount() {
        return amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @return timestamp formatted as "dd/MM/yyyy"
     */
    public String getDate() {
        Date date = new Date(this.timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(date);
    }
}
