package com.liferay.jenkins.results.parser.TaxCalculator;

import java.io.File;
import java.io.FileNotFoundException;

import java.lang.Math;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;

public class Tax {
    private ArrayList<Item> items;

    public Tax() {
        items = new ArrayList<Item>();
    }

    public double calculateTax(Item item) {
        double tax = 0.0;
        if (!item.isExempt()) {
            tax += item.getPrice() * .10;
        }
        if (item.isImported()) {
            tax += item.getPrice() * .05;
        }

        tax *= 20;
        tax = Double.valueOf(Math.round(tax));
        tax /= 20;

        return tax;
    }

    public void getInput() {
        try {
            Class<?> clazz = Tax.class;
            URL resourceURL = clazz.getResource("/com/liferay/jenkins/results/parser/dependencies/TaxCalculatorInputs/input3.txt");
            System.out.println("resource url: " + resourceURL);
            URI resourceURI = resourceURL.toURI();

            Path resourcePath = Paths.get(resourceURI);
            File resourceFile = resourcePath.toFile();
            String path = resourceFile.getPath();

            File file = new File(path);
            Scanner scanner = new Scanner(file);

            String input = "";

            while (scanner.hasNextLine()){
                input = scanner.nextLine();
                this.parseInput(input);
            }
        } catch (FileNotFoundException | URISyntaxException fileNotFoundException) {
            System.err.println("Error, file not found.");
            fileNotFoundException.printStackTrace();
        }
    }

    public void parseInput(String input) {
        int atIndex = input.indexOf(" at ");
        if (atIndex == -1) {
            System.err.println("Invalid input.");
            return;
        }
        if (!Character.isDigit(input.charAt(0))) {
            System.err.println("Invalid input.");
            return;
        }

        Matcher matcher = Pattern.compile("\\d+").matcher(input);
        matcher.find();

        String amountString = matcher.group();
        int amountInt = Integer.valueOf(amountString);

        String itemName = input.substring(amountString.length() + 1, atIndex);
        String itemPrice = input.substring(atIndex + 4);
        Double itemPriceDouble = Double.parseDouble(itemPrice);

        this.items.add(new Item(amountInt, itemName, itemPriceDouble));

        return;
    }

    public void print() {
        double total = 0.0;
        double totalTax = 0.0;
        for (int i = 0; i < items.size(); i++) {
            double itemTax = calculateTax(items.get(i));
            double itemCostWithTax = items.get(i).getPrice() + itemTax;

            System.out.println(items.get(i).getAmount() + " " + items.get(i).getName() + ": " + String.format("%.2f", itemCostWithTax));
            totalTax += itemTax;
            total += itemCostWithTax;
        }
        System.out.println("Sales Taxes: " + String.format("%.2f", totalTax));
        System.out.println("Total: " + String.format("%.2f", total) );
    }

    public static void main(String[] args) {
        Tax testTax = new Tax();
        testTax.getInput();
        testTax.print();
    }
}