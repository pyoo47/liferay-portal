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

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.Console;
import java.io.File;
import java.io.IOException;

import java.math.BigDecimal;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Cesar Polanco
 */
public class CartItem {

	public CartItem(int amount, String extPrice, String productName) {
		_basicSalesTaxApplicable = checkBasicTaxability(productName);
		_importSalesTaxApplicable = _checkImportTaxability(productName);
		_name = productName;
		_preTaxPrice = new BigDecimal(extPrice);
		_quantity = amount;
	}

	public boolean checkBasicTaxability(String productName) {
		if (!(_isFood(productName) ||
			 _isMedicine(productName) ||
			_isBook(productName))) {

			return true;
		}

		return false;
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
		_postTaxPrice = new BigDecimal("0.00").add(_preTaxPrice.add(taxTotal));
	}

	private void _addToFood(String productName) {

		// Add to Food file

		ArrayList<String> iterableProduct = new ArrayList<>();

		iterableProduct.add(productName);
		try {
			Files.write(
				Paths.get(_FOODFILE), iterableProduct, Charset.defaultCharset(),
				_options);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void _addToMedicine(String productName) {
		_medicineProducts.add(productName);

		try {
			_listToFile(_MEDICINEFILE, _medicineProducts);
		}
		catch (IOException ioe) {
			throw new RuntimeException(
				"Unable to write file " + _MEDICINEFILE, ioe);
		}
	}

	private void _addToSeen(String productName) {
		_seenProducts.add(productName);

		try {
			_listToFile(_SEENFILE, _seenProducts);
		}
		catch (IOException ioe) {
			throw new RuntimeException(
				"Unable to write file " + _SEENFILE, ioe);
		}
	}

	private boolean _askIfFood(String productName) {
		//Ask via commandline if this product is Food
		System.out.println(
			"This product has not been seen before. Please answer the " +
				"following for tax purposes:");

		if (_askIfType(productName, "food")) {
			_addToFood(productName);

			return true;
		}

		return false;
	}

	private boolean _askIfMedicine(String productName) {
		if (_askIfType(productName, "medicine")) {
			_addToMedicine(productName);

			return true;
		}

		return false;
	}

	private boolean _askIfType(String productName, String type) {
		try {
			Console console = System.console();

			if (console != null) {
				String response = console.readLine(
					"Is " + productName + " " + type + ": ");

				if (response.equalsIgnoreCase("yes")) {
					return true;
				}
			}

			return false;
		}
		finally {
			_addToSeen(productName);
		}
	}

	private boolean _checkImportTaxability(String productName) {
		if (Arrays.asList(productName.split(" ")).contains("imported")) {
			return true;
		}
		else {
			return false;
		}
	}

	private List<String> _fileToList(String fileName) throws IOException {
		File file = new File(fileName);

		List<String> list = new ArrayList<>();

		if (!file.exists()) {
			return list;
		}

		for (String line :
				Files.readAllLines(
					Paths.get(fileName), Charset.defaultCharset())) {

			list.add(line.trim());
		}

		return list;
	}

	private boolean _isBook(String productName) {
		if (productName.equals("book") || productName.equals("books")) {
			return true;
		}

		return false;
	}

	private boolean _isFood(String productName) {

		if (_foodProducts == null) {
			_loadFoodFile();
		}

		if (_foodProducts.contains(productName)) {
			return true;
		}

		if (_isSeen(productName)) {
			return false;
		}

		return _askIfFood(productName);
	}

	private boolean _isMedicine(String productName) {

		// Consult MEDICINE file

		if (_medicineProducts == null) {
			_loadMedicineFile();
		}

		if (_medicineProducts.contains(productName)) {
			return true;
		}

		if (_isSeen(productName)) {
			return false;
		}

		return _askIfMedicine(productName);
	}

	private boolean _isSeen(String productName) {
		if (_seenProducts == null) {
			_loadSeenFile();
		}

		return _seenProducts.contains(productName);
	}

	private void _listToFile(String fileName, List<String> list)
		throws IOException {

		Files.write(
			Paths.get(fileName), list, Charset.defaultCharset(),
			new StandardOpenOption[] {CREATE, TRUNCATE_EXISTING, WRITE});
	}

	private void _loadFoodFile() {
		try {
			_foodProducts = _fileToList(_FOODFILE);
		}
		catch (IOException ioe) {
			System.out.println(
				"WARNING - " + _FOODFILE + " could not be read. " +
					ioe.getMessage());

			_foodProducts = new ArrayList<>();
		}
	}

	private void _loadMedicineFile() {
		try {
			_medicineProducts = _fileToList(_MEDICINEFILE);
		}
		catch (IOException ioe) {
			System.out.println(
				"WARNING - " + _MEDICINEFILE + " could not be read. " +
					ioe.getMessage());

			_medicineProducts = new ArrayList<>();
		}
	}

	private void _loadSeenFile() {
		try {
			_seenProducts = _fileToList(_SEENFILE);
		}
		catch (IOException ioe) {
			System.out.println(
				"WARNING - " + _SEENFILE + " could not be read. " +
					ioe.getMessage());

			_seenProducts = new ArrayList<>();
		}
	}

	private static final String _FOODFILE = "food_file.txt";

	private static final String _MEDICINEFILE = "medicine_file.txt";

	private static final String _SEENFILE = "all_seen_products.txt";

	private static List<String> _foodProducts;
	private static List<String> _medicineProducts;
	private static List<String> _seenProducts;

	private final boolean _basicSalesTaxApplicable;
	private final boolean _importSalesTaxApplicable;
	private final String _name;
	private final StandardOpenOption[] _options =
		new StandardOpenOption[] {CREATE, TRUNCATE_EXISTING, WRITE};
	private BigDecimal _postTaxPrice;
	private final BigDecimal _preTaxPrice;
	private final int _quantity;

}