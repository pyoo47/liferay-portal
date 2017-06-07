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

package com.liferay.jenkins.results.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Yi-Chen Tsai
 */
public class SalesTaxes {

	public static void main(String[] args) {
		ShoppingCart cart = new ShoppingCart();

		cart.addItem("1 book at 12.49");
		cart.addItem("1 music CD at 14.99");
		cart.addItem("1 chocolate bar at 0.85");
		cart.printReceipt();

		cart = new ShoppingCart();

		cart.addItem("1 imported box of chocolates at 10.00");
		cart.addItem("1 imported bottle of perfume at 47.50");
		cart.printReceipt();

		cart = new ShoppingCart();

		cart.addItem("1 imported bottle of perfume at 27.99");
		cart.addItem("1 bottle of perfume at 18.99");
		cart.addItem("1 packet of headache pills at 9.75");
		cart.addItem("1 imported box of chocolates at 11.25");
		cart.printReceipt();
	}

	public static class Good {

		public Good(
			boolean imported, boolean exempt, String name, double price,
			int quantity) {

			_imported = imported ? 1 : 0;
			_imported_tax_rate = 0.05;
			_exempt = exempt ? 0 : 1;
			_name = name;
			_price = price;
			_quantity = quantity;
			_tax_rate = 0.1;
		}

		public double getFinalPrice() {
			return getPrice() + getTax();
		}

		public String getName() {
			return _name;
		}

		public double getPrice() {
			return _quantity * _price;
		}

		public int getQuantity() {
			return _quantity;
		}

		public double getTax() {
			return Math.ceil(
				_quantity * _price *
					(_imported * _imported_tax_rate + _exempt * _tax_rate) *
						20.0) / 20.0;
		}

		private final int _exempt;
		private final int _imported;
		private final double _imported_tax_rate;
		private final String _name;
		private final double _price;
		private final int _quantity;
		private final double _tax_rate;

	}

	public static class ShoppingCart {

		public ShoppingCart() {
			_cart = new ArrayList<>();
		}

		public void addItem(String item) {
			boolean imported = false;
			boolean exempt = false;
			String item_name = "";
			StringTokenizer item_token = new StringTokenizer(item);

			int item_token_count = item_token.countTokens();

			double price = 0.0;
			int quantity = 0;

			if (item.contains("imported")) {
				imported = true;
			}
			else {
				imported = false;
			}

			if (item.contains("book") || item.contains("chocolate") ||
				item.contains("pills")) {

				exempt = true;
			}
			else {
				exempt = false;
			}

			// Tokenize the item to get quantity, price, and item name

			for (int i = 0; i < item_token_count; i++) {
				if (i == 0) {
					quantity = Integer.parseInt(item_token.nextToken());
				}
				else if (i == (item_token_count - 1)) {
					price = Double.parseDouble(item_token.nextToken());
				}
				else {
					String tok = item_token.nextToken();

					if (!tok.contentEquals("at")) {
						if (item_name.isEmpty()) {
							item_name += tok;
						}
						else {
							item_name += " " + tok;
						}
					}
				}
			}

			_cart.add(new Good(imported, exempt, item_name, price, quantity));
		}

		public void printReceipt() {
			double sales_taxes = 0.0;
			double total = 0.0;

			// Prints in the format
			// <quantity> <item_name>: <price>
			// <sales_taxes>
			// <total>
			// <CR><LF>

			for (int i = 0; i < _cart.size(); i++) {
				Good item = _cart.get(i);

				double item_price = item.getFinalPrice();

				sales_taxes += item.getTax();

				total += item_price;

				System.out.println(
					item.getQuantity() + " " + item.getName() + ": " +
						String.format("%.2f", item_price));
			}

			System.out.println(
				"Sales Taxes: " + String.format("%.2f", sales_taxes));
			System.out.println("Total: " + String.format("%.2f", total));
			System.out.println();
		}

		private final List<Good> _cart;

	}

}