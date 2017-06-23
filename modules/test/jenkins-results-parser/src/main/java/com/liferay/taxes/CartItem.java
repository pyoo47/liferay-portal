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
		_basicSalesTaxApplicable = _isDomesticallyTaxable(productName);
		_importSalesTaxApplicable = _checkImportTaxability(productName);
		_name = productName;
		_preTaxPrice = new BigDecimal(extPrice);
		_quantity = amount;
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

	private void _addToNonTaxable(String productName) {
		_nonTaxableProducts.add(productName);

		try {
			_listToFile(_NONTAXABLEFILE, _nonTaxableProducts);
		}
		catch (IOException ioe) {
			throw new RuntimeException(
				"Could not write to file: " + _NONTAXABLEFILE, ioe);
		}
	}

	private void _addToTaxable(String productName) {
		_taxableProducts.add(productName);

		try {
			_listToFile(_TAXABLEFILE, _taxableProducts);
		}
		catch (IOException ioe) {
			throw new RuntimeException(
				"Could not write to file: " + _TAXABLEFILE, ioe);
		}
	}

	private boolean _askIfTaxable(String productName) {
		try {
			Console console = System.console();

			if (console != null) {
				String response = console.readLine(
					"Product cannot be identified. Is it a food or medicine? ");

				if (response.equals("yes")) {
					_addToNonTaxable(productName);
					return false;
				}
				else {
					_addToTaxable(productName);
					return true;
				}
			}

			return true;
		}
		catch (Exception e) {
			throw new RuntimeException("Console unavailable to use: " + e);
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

	private boolean _isDomesticallyTaxable(String productName) {
		if (_nonTaxableProducts == null) {
			_loadNonTaxableFile();
		}

		if (_taxableProducts == null) {
			_loadTaxableFile();
		}

		if (_isBook(productName) || _nonTaxableProducts.contains(productName)) {
			return false;
		}
		else if (_taxableProducts.contains(productName)) {
			return true;
		}
		else {
			return _askIfTaxable(productName);
		}
	}

	private void _listToFile(String fileName, List<String> list)
		throws IOException {

		Files.write(
			Paths.get(fileName), list, Charset.defaultCharset(),
			new StandardOpenOption[] {CREATE, TRUNCATE_EXISTING, WRITE});
	}

	private void _loadNonTaxableFile() {
		try {
			_nonTaxableProducts = _fileToList(_NONTAXABLEFILE);
		}
		catch (IOException ioe) {
			System.out.println(
				_NONTAXABLEFILE + " could not be read. " + ioe.getMessage());

			_nonTaxableProducts = new ArrayList<>();
		}
	}

	private void _loadTaxableFile() {
		try {
			_taxableProducts = _fileToList(_TAXABLEFILE);
		}
		catch (IOException ioe) {
			System.out.println(
				_TAXABLEFILE + " could not be read. " + ioe.getMessage());

			_taxableProducts = new ArrayList<>();
		}
	}

	private static final String _NONTAXABLEFILE = "nontaxable.txt";

	private static final String _TAXABLEFILE = "taxable.txt";

	private static List<String> _nonTaxableProducts;
	private static List<String> _taxableProducts;

	private final boolean _basicSalesTaxApplicable;
	private final boolean _importSalesTaxApplicable;
	private final String _name;
	private final StandardOpenOption[] _options =
		new StandardOpenOption[] {CREATE, TRUNCATE_EXISTING, WRITE};
	private BigDecimal _postTaxPrice;
	private final BigDecimal _preTaxPrice;
	private final int _quantity;

}