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

/* Cart class
 * This is the class which will hold all necessary information for printing,
 * as well as the formatting.
 */

package com.liferay.taxes;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Cesar Polanco
 */
public class Cart {

	public Cart() {
		_contents = new ArrayList<>();
		_total = new BigDecimal("0.00");
		_totalTax = new BigDecimal("0.00");
	}

	public void addToCart(CartItem newItem) {
		_contents.add(newItem);
	}

	public void addToCart(String entry) {
		String[] brokenDownEntry = entry.split(" ");
		String nameString = "";

		// Get Quantity

		int quantity = Integer.parseInt(brokenDownEntry[0]);

		// Get Name

		for (int i = 1; i < Arrays.asList(brokenDownEntry).indexOf("at"); i++) {
			nameString = nameString + brokenDownEntry[i] + " ";
		}

		if (!nameString.equals("")) {
			nameString = nameString.substring(0, nameString.length() - 1);
		}

		// Get price

		String price = brokenDownEntry[brokenDownEntry.length - 1];

		CartItem product = new CartItem(quantity, nameString, price);

		addToCart(product);
	}

	public void calculateTotal() {
		BigDecimal cumulative = new BigDecimal("0.00");
		BigDecimal totalForItem;

		for (CartItem item : _contents) {
			totalForItem = new BigDecimal(item.getQuantity());

			totalForItem = totalForItem.multiply(item.getPostTaxPrice());
			totalForItem = totalForItem.setScale(2, RoundingMode.HALF_UP);

			cumulative = cumulative.add(totalForItem);
		}

		_total = cumulative;
	}

	public ArrayList<CartItem> getContents() {
		return _contents;
	}

	public BigDecimal getTotal() {
		return _total;
	}

	public BigDecimal getTotalTax() {
		return _totalTax;
	}

	public void removeFromCart(CartItem itemToRemove) {
		_contents.remove(itemToRemove);
	}

	public void setTotalTax(BigDecimal taxTotal) {
		_totalTax = taxTotal;
	}

	private final ArrayList<CartItem> _contents;
	private BigDecimal _total;
	private BigDecimal _totalTax;

    addToCart(product);
  }
}