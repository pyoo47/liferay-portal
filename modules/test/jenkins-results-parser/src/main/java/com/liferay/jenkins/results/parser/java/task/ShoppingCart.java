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

package com.liferay.jenkins.results.parser.java.task;

import java.io.File;
import java.io.FileNotFoundException;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Brittney Nguyen
 */
public class ShoppingCart {

	public static DecimalFormat df = new DecimalFormat("0.00");
	public static float totalTax = 0.0F;

	public static float calculateSalesTax(
		boolean exempt, boolean imported, float salePrice) {

		float tax = 0.0F;

		if (imported) {
			tax = 0.05F;
		}

		if (!exempt) {
			tax = 0.10F;
		}

		if (imported && !exempt) {
			tax = 0.15F;
		}

		return (float)(Math.ceil((tax * salePrice) * 20.0) / 20.0);
	}

	public static void main(String[] args) throws FileNotFoundException {
		File basket = new File(
			"/opt/dev/projects/github/liferay-portal/modules/test/jenkins-results-parser/src/main/java/com/liferay/jenkins/results/parser/java/task/input3.txt");

		Scanner scanner = new Scanner(basket);

		float saleTax = 0.00F;
		float total = 0.00F;
		ArrayList<String> shoppingList = new ArrayList<>();

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();

			String regex = "(\\d+) (\\D+\\s?)+ at (\\d+.\\d+)";

			Pattern pattern = Pattern.compile(regex);

			Matcher matcher = pattern.matcher(line);

			while (matcher.find()) {
				String quantity = matcher.group(1);
				String itemName = matcher.group(2);
				String salePrice = matcher.group(3);

				Item item = new Item();

				item.price = Float.parseFloat(salePrice);
				item.name = itemName;
				item.exempt = item.isExempt();
				item.imported = item.isImported();
				item.quantity = Integer.parseInt(quantity);
				item.price = Float.parseFloat(salePrice);

				float itemSaleTax = calculateSalesTax(
					item.exempt, item.imported, item.price);

				saleTax += itemSaleTax;

				String priceTaxed = df.format(item.price + itemSaleTax);

				item.setPrice(priceTaxed);

				shoppingList.add(
					toString(item.quantity, item.name, item.price));

				total += item.price;
			}
		}

		printReceipt(shoppingList, saleTax, total);
	}

	public static void printReceipt(
		ArrayList<String> cart, float salesTax, float total) {

		for (String item : cart) {
			System.out.println(item);
		}

		System.out.println("Sales Tax: " + df.format(salesTax));
		System.out.print("Total: " + df.format(total));
	}

	public static String toString(int quantity, String item, float salePrice) {
		return quantity + " " + item + ": " + df.format(salePrice + totalTax);
	}

}