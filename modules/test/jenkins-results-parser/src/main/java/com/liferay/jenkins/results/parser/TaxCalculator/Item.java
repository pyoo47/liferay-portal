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

package com.liferay.jenkins.results.parser.TaxCalculator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Charlotte Wong
 */
public class Item {

	public Item(int amount, String name, double price) {
		_amount = amount;
		_name = name;
		_price = price;

		_exemptPattern = Pattern.compile("chocolate|book|pill");

		_imported = _checkImport();
		_exempt = _checkExempt();
	}

	public int getAmount() {
		return _amount;
	}

	public String getName() {
		return _name;
	}

	public double getPrice() {
		return _price;
	}

	public boolean isExempt() {
		return _exempt;
	}

	public boolean isImported() {
		return _imported;
	}

	private boolean _checkExempt() {
		Matcher matcher = _exemptPattern.matcher(_name);

		if (matcher.find()) {
			return true;
		}

		return false;
	}

	private boolean _checkImport() {
		if (_name.indexOf("imported") >= 0) {
			return true;
		}

		return false;
	}

	private final int _amount;
	private final boolean _exempt;
	private final Pattern _exemptPattern;
	private final boolean _imported;
	private final String _name;
	private final double _price;

}