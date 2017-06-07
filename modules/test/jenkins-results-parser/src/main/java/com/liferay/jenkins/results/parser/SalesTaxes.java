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

/**
 * @author Yi-Chen Tsai
 */
public class SalesTaxes {

	public static void main(String[] args) {
		shoppingCart1();

		System.out.println();

		shoppingCart2();

		System.out.println();

		shoppingCart3();
	}

	public static void shoppingCart1() {
		Good book = new Good(false, true, 12.49, 1);

		double book_price = book.getFinalPrice();

		Good music_CD = new Good(false, false, 14.99, 1);

		double music_CD_price = music_CD.getFinalPrice();

		Good chocolate_bar = new Good(false, true, 0.85, 1);

		double chocolate_bar_price = chocolate_bar.getFinalPrice();

		double sales_taxes =
			book.getTax() + music_CD.getTax() + chocolate_bar.getTax();

		double total_cost = book_price + music_CD_price + chocolate_bar_price;

		System.out.println(
			book.getQuantity() + " book: " + String.format("%.2f", book_price));

		System.out.println(
			music_CD.getQuantity() + " music CD: " +
				String.format("%.2f", music_CD_price));

		System.out.println(
			chocolate_bar.getQuantity() + " chocolate bar: " +
				String.format("%.2f", chocolate_bar_price));

		System.out.println(
			"Sales Taxes: " + String.format("%.2f", sales_taxes));

		System.out.println("Total: " + String.format("%.2f", total_cost));
	}

	public static void shoppingCart2() {
		Good imported_chocolate = new Good(true, true, 10.00, 1);

		double imported_chocolate_price = imported_chocolate.getFinalPrice();

		Good imported_perfume = new Good(true, false, 47.50, 1);

		double imported_perfume_price = imported_perfume.getFinalPrice();

		double sales_taxes =
			imported_chocolate.getTax() + imported_perfume.getTax();

		double total_cost = imported_chocolate_price + imported_perfume_price;

		System.out.println(
			imported_chocolate.getQuantity() + " imported box of chocolate: " +
				String.format("%.2f", imported_chocolate_price));

		System.out.println(
			imported_perfume.getQuantity() + " imported bottle of perfume: " +
				String.format("%.2f", imported_perfume_price));

		System.out.println(
			"Sales Taxes: " + String.format("%.2f", sales_taxes));

		System.out.println("Total: " + String.format("%.2f", total_cost));
	}

	public static void shoppingCart3() {
		Good imported_perfume = new Good(true, false, 27.99, 1);

		double imported_perfume_price = imported_perfume.getFinalPrice();

		Good perfume = new Good(false, false, 18.99, 1);

		double perfume_price = perfume.getFinalPrice();

		Good headache_pills = new Good(false, true, 9.75, 1);

		double headache_pills_price = headache_pills.getFinalPrice();

		Good imported_chocolate = new Good(true, true, 11.25, 1);

		double imported_chocolate_price = imported_chocolate.getFinalPrice();

		double sales_taxes =
			imported_perfume.getTax() + perfume.getTax() +
				headache_pills.getTax() + imported_chocolate.getTax();

		double total_cost =
			imported_perfume_price + perfume_price + headache_pills_price +
				imported_chocolate_price;

		System.out.println(
			imported_perfume.getQuantity() + " imported bottle of perfume: " +
				String.format("%.2f", imported_perfume_price));

		System.out.println(
			perfume.getQuantity() + " bottle of perfume: " +
				String.format("%.2f", perfume_price));

		System.out.println(
			perfume.getQuantity() + " packet of headache pills: " +
				String.format("%.2f", headache_pills_price));

		System.out.println(
			imported_chocolate.getQuantity() + " imported box of chocolate: " +
				String.format("%.2f", imported_chocolate_price));

		System.out.println(
			"Sales Taxes: " + String.format("%.2f", sales_taxes));

		System.out.println("Total: " + String.format("%.2f", total_cost));
	}

	public static class Good {

		public Good(
			boolean imported, boolean exempt, double price, int quantity) {

			_imported = imported ? 1 : 0;
			_imported_tax_rate = 0.05;
			_exempt = exempt ? 0 : 1;
			_price = price;
			_quantity = quantity;
			_tax_rate = 0.1;
		}

		public double getFinalPrice() {
			return getPrice() + getTax();
		}

		public double getPrice() {
			return _price;
		}

		public int getQuantity() {
			return _quantity;
		}

		public double getTax() {
			return Math.ceil(
				(_price *
					(_imported * _imported_tax_rate + _exempt * _tax_rate)) *
						20.0) / 20.0;
		}

		private final int _exempt;
		private final int _imported;
		private final double _imported_tax_rate;
		private final double _price;
		private final int _quantity;
		private final double _tax_rate;

	}

}