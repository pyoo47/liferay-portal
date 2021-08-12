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

/**
 * @author Brittney Nguyen
 */
public class Item {

	public Item(int quantity, String name, float price) {
		this.quantity = quantity;
		this.name = name;
		this.price = price;

		setImported();
		setExempt();
	}

	public String getName() {
		return name;
	}

	public float getPrice() {
		return price;
	}

	public float getPriceWithTax() {
		return priceWithTax;
	}

	public int getQuantity() {
		return quantity;
	}

	public float getTax() {
		return tax;
	}

	public void setExempt() {
		if (name.contains("book") || name.contains("chocolate") ||
			name.contains("pill")) {

			exempt = true;
		}
	}

	public void setImported() {
		if (name.contains("imported")) {
			imported = true;
		}
	}

	public void setTax() {
		if (imported) {
			tax = 0.05F;
		}

		if (!exempt) {
			tax = 0.10F;
		}

		if (imported && !exempt) {
			tax = 0.15F;
		}

		float updateTax = (float)(Math.ceil((tax * price) * 20.0) / 20.0);

		tax = updateTax;

		priceWithTax = price += tax;
	}

	protected boolean exempt;
	protected boolean imported;
	protected final String name;
	protected float price;
	protected float priceWithTax;
	protected final int quantity;
	protected float tax;

}