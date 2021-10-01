/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.jenkins.results.parser.TaxCalculator;

import java.io.File;
import java.io.FileNotFoundException;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Charlotte Wong
 */
public class Tax {

	public static void main(String[] args) {
		Tax testTax = new Tax();

		testTax.getInput();
		testTax.print();
	}

	public Tax() {
		_digitPattern = Pattern.compile("\\d+");
		_resourceFolderURL = "/com/liferay/jenkins/results/parser/dependencies";
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

			URL resourceURL = clazz.getResource(
				_resourceFolderURL + "/TaxCalculatorInputs/input3.txt");

			URI resourceURI = resourceURL.toURI();

			Path resourcePath = Paths.get(resourceURI);

			File resourceFile = resourcePath.toFile();

			String path = resourceFile.getPath();

			File file = new File(path);

			Scanner scanner = new Scanner(file);

			String input = "";

			while (scanner.hasNextLine()) {
				input = scanner.nextLine();

				parseInput(input);
			}
		}
		catch (FileNotFoundException | URISyntaxException exception) {
			System.err.println("Error, file not found.");
			exception.printStackTrace();
		}
	}

	public void parseInput(String input) {
		int atIndex = input.indexOf(" at ");

		if ((atIndex == -1) || !Character.isDigit(input.charAt(0))) {
			System.err.println("Invalid input.");

			return;
		}

		Matcher matcher = _digitPattern.matcher(input);

		matcher.find();

		String amountString = matcher.group();

		int amountInt = Integer.valueOf(amountString);

		String itemName = input.substring(amountString.length() + 1, atIndex);

		String itemPrice = input.substring(atIndex + 4);

		Double itemPriceDouble = Double.parseDouble(itemPrice);

		_items.add(new Item(amountInt, itemName, itemPriceDouble));
	}

	public void print() {
		double total = 0.0;
		double totalTax = 0.0;

		for (Item item : _items) {
			double itemTax = calculateTax(item);

			double itemCostWithTax = item.getPrice() + itemTax;

			System.out.println(
				item.getAmount() + " " + item.getName() + ": " +
					String.format("%.2f", itemCostWithTax));

			totalTax += itemTax;

			total += itemCostWithTax;
		}

		System.out.println("Sales Taxes: " + String.format("%.2f", totalTax));
		System.out.println("Total: " + String.format("%.2f", total));
	}

	private final Pattern _digitPattern;
	private final ArrayList<Item> _items = new ArrayList<>();
	private final String _resourceFolderURL;

}