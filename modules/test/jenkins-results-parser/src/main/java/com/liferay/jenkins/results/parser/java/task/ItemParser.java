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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Brittney Nguyen
 */
public class ItemParser {

	public ItemParser(String file) throws IOException {
		FileReader fr = new FileReader(file);

		BufferedReader br = new BufferedReader(fr);

		cart = new ShoppingCart();

		String regex = "(\\d+) (\\D+\\s?)+ at (\\d+.\\d+)";

		Pattern pattern = Pattern.compile(regex);

		while (br.ready()) {
			String line = br.readLine();

			Matcher matcher = pattern.matcher(line);

			while (matcher.find()) {
				String quantity = matcher.group(1);
				String itemName = matcher.group(2);
				String salePrice = matcher.group(3);

				cart.addItem(
					Integer.parseInt(quantity), itemName,
					Float.parseFloat(salePrice));
			}
		}

		br.close();
	}

	public ShoppingCart getCart() {
		return cart;
	}

	protected ShoppingCart cart;

}