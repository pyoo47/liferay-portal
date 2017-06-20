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

/* Item class
 * This is the class of which every item in the basket will be an instance.
 */

package com.liferay.taxes;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

import java.io.Console;
import java.io.IOException;

import java.math.BigDecimal;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Cesar Polanco
 */
public class CartItem {

	public CartItem(int amount, String productName, String extPrice) {
		_quantity = amount;
		_name = productName;
		_basicSalesTaxApplicable = checkBasicTaxability(productName);
		_importSalesTaxApplicable = _checkImportTaxability(productName);
		_preTaxPrice = new BigDecimal(extPrice);
	}

	public boolean checkBasicTaxability(String productName) {
		if (_checkIfSeen(productName)) {
			if (!(_checkIfFood(productName) ||
				 _checkIfMedicine(productName) ||
				_checkIfBook(productName))) {

				return true;
			}

			return false;
		}
		else {
			_addToSeen(productName);

			if (_checkIfBook(productName)) {
				return false;
			}
			else if (_askIfFood(productName)) {
				_addToFood(productName);
				return false;
			}
			else if (_askIfMedicine(productName)) {
				_addToMedicine(productName);
				return false;
			}
			else {
				return true;
			}
		}
	}

	public String getName() {
		return _name;
	}

	public BigDecimal getPostTaxPrice() {
		return _postTaxPrice;
	}

	public BigDecimal getPreTaxPrice() {
		return _preTaxPrice;
	}

	public int getQuantity() {
		return _quantity;
	}

	public boolean isImported() {
		return _importSalesTaxApplicable;
	}

	public boolean isTaxable() {
		return _basicSalesTaxApplicable;
	}

	public void setPostTaxPrice(BigDecimal taxTotal) {
		_postTaxPrice = _preTaxPrice.add(taxTotal);
	}

	private void _addToFood(String productName) {

		// Add to Food file

		ArrayList<String> iterableProduct = new ArrayList<>();

		iterableProduct.add(productName);
		try {
			Files.write(Paths.get(_FOODFILE), iterableProduct, _options);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void _addToMedicine(String productName) {

		// Add to Medicine file

		ArrayList<String> iterableProduct = new ArrayList<>();

		iterableProduct.add(productName);
		try {
			Files.write(Paths.get(_MEDICINEFILE), iterableProduct, _options);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void _addToSeen(String productName) {

		// Add to SEEN file

		ArrayList<String> iterableProduct = new ArrayList<>();

		iterableProduct.add(productName);
		try {
			Files.write(Paths.get(_SEENFILE), iterableProduct, _options);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean _askIfFood(String productName) {
		//Ask via commandline if this product is Food
		System.out.println(
			"This product has not been seen before. Please answer the " +
				"following for tax purposes:");
		boolean answer = false;
		Console console = null;
		String response = null;

		try {
			console = System.console();

			if (console != null) {
				response = console.readLine("Is " + productName + " food: ");

				if (response.equals("yes") || response.equals("Yes")) {
					answer = true;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return answer;
	}

	private boolean _askIfMedicine(String productName) {

		// Ask via commandline if this product is Medicine

		boolean answer = false;
		Console console = null;
		String response = null;

		try {
			console = System.console();

			if (console != null) {
				response = console.readLine(
					"Is " + productName + " medicine: ");

				if (response.equals("yes") || response.equals("Yes")) {
					answer = true;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return answer;
	}

	private boolean _checkIfBook(String productName) {
		if (productName.equals("book") || productName.equals("books")) {
			return true;
		}

		return false;
	}

	private boolean _checkIfFood(String productName) {

		// consult Food file

		boolean existsInFood = false;

		try {
			existsInFood = Files.lines(
				Paths.get(_FOODFILE)).anyMatch((item) -> item.equals(
					productName));
		}
		catch (IOException e) {
			//e.printStackTrace();
			try {
				Files.createFile(Paths.get(_FOODFILE));
				_checkIfFood(productName);
			}
			catch (IOException x) {
				x.printStackTrace();
			}
		}

		return existsInFood;
	}

	private boolean _checkIfMedicine(String productName) {

		// Consult MEDICINE file

		boolean existsInMedicine = false;

		try {
			existsInMedicine = Files.lines(
				Paths.get(_MEDICINEFILE)).anyMatch((item) -> item.equals(
					productName));
		}
		catch (IOException e) {
			//e.printStackTrace();
			try {
				Files.createFile(Paths.get(_MEDICINEFILE));
				_checkIfMedicine(productName);
			}
			catch (IOException x) {
				x.printStackTrace();
			}
		}

		return existsInMedicine;
	}

	private boolean _checkIfSeen(String productName) {

		// Consult SEEN file

		boolean existsInSeen = false;

		try {
			existsInSeen = Files.lines(
				Paths.get(_SEENFILE)).anyMatch((item) -> item.equals(
					productName));
		}
		catch (IOException e) {
			//e.printStackTrace();
			try {
				Files.createFile(Paths.get(_SEENFILE));
				_checkIfSeen(productName);
			}
			catch (IOException x) {
				x.printStackTrace();
			}
		}

		return existsInSeen;
	}

	private boolean _checkImportTaxability(String productName) {
		if (Arrays.asList(productName.split(" ")).contains("imported")) {
			return true;
		}
		else {
			return false;
		}
	}

	private static final String _FOODFILE = "food_file.txt";

	private static final String _MEDICINEFILE = "medicine_file.txt";

	private static final String _SEENFILE = "all_seen_products.txt";

	private final boolean _basicSalesTaxApplicable;
	private final boolean _importSalesTaxApplicable;
	private final String _name;
	private final StandardOpenOption[] _options =
		new StandardOpenOption[] {APPEND, CREATE};
	private final BigDecimal _postTaxPrice;
	private final BigDecimal _preTaxPrice;
	private final int _quantity;

}