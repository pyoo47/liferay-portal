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

import java.util.ArrayList;

/**
 * @author Brittney Nguyen
 */
public class ShoppingCart {

	public Item addItem(int quantity, String name, float price) {
		Item item = new Item(quantity, name, price);

		items.add(item);

		return item;
	}

	public ArrayList<Item> getItems() {
		return items;
	}

	public float getSalesTax() {
		float salesTax = 0.0F;

		for (Item item : items) {
			salesTax += item.getTax();
		}

		return salesTax;
	}

	public float getTotal() {
		float total = 0.0F;

		for (Item item : items) {
			total += item.getPriceWithTax();
		}

		return total;
	}

	protected ArrayList<Item> items = new ArrayList<>();

}