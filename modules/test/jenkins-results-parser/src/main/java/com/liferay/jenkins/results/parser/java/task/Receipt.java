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
public class Receipt {

	public String printReceipt(ShoppingCart shoppingCart) {
		String receipt = "";

		for (Item item : shoppingCart.getItems()) {
			receipt += String.format(
				"%d %s: %.2f\n", item.getQuantity(), item.getName(),
				item.getPriceWithTax());
		}

		receipt += String.format(
			"Sales Taxes: %.2f\n", shoppingCart.getSalesTax());
		receipt += String.format("Total: %.2f", shoppingCart.getTotal());

		return receipt;
	}

}