package com.example.projet.Model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Income {
    private String source;
    private double amount;
    private long timestamp;

    // Constructeur vide requis par Firebase
    public Income() {
    }

    public Income(String source, double amount, long timestamp) {
        this.source = source;
        this.amount = amount;
        this.timestamp = timestamp;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
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
