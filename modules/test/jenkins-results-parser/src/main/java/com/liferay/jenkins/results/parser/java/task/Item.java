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

	public float getPrice() {
		return price;
	}

	public boolean isExempt() {
		return name.contains("book") || name.contains("chocolate") ||
				name.contains("pill");
	}

	public boolean isImported() {
		return name.contains("imported");
	}

	public void setPrice(String price) {
		this.price = Float.parseFloat(price);
	}

	public boolean exempt;
	public boolean imported;
	public String name;
	public float price;
	public int quantity;

}