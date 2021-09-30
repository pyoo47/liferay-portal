package com.liferay.jenkins.results.parser.TaxCalculator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Item {
    private int amount;
    private String name;
    private double price;
    private boolean imported;
    private boolean exempt;

    public Item(int amount, String name, double price) {
        this.amount = amount;
        this.name = name;
        this.price = price;
        this.imported = this.checkImport();
        this.exempt = this.checkExempt();
    }

    public int getAmount() {
        return this.amount;
    }
    public String getName() {
        return this.name;
    }
    public double getPrice() {
        return this.price;
    }
    public boolean isImported() {
        return this.imported;
    }
    public boolean isExempt() {
        return this.exempt;
    }

    private boolean checkImport() {
        if (this.name.indexOf("imported") >= 0) {
            return true;
        }
        return false;
    }

    private boolean checkExempt() {
        Matcher matcher = Pattern.compile("chocolate|book|pill").matcher(this.name);
        if (matcher.find()) {
            return true;
        }
        return false;
    }
}
