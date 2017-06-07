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

package com.liferay.jenkins.results.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Yi-Chen Tsai
 */
public class Good {

	public static Pattern namePattern;
	public static Pattern pricePattern;

	public Good(String goodDescriptor) {
		_importedTaxRate = 0.05;
		_taxRate = 0.1;

		_imported = goodDescriptor.contains("imported");

		_exempt = goodDescriptor.contains("book") ||
				goodDescriptor.contains("chocolate") ||
				goodDescriptor.contains("pills");

		namePattern = Pattern.compile("([a-zA-Z][a-zA-Z\\s]*)(?:\\sat)");

		Matcher nameMatcher = namePattern.matcher(goodDescriptor);

		if (nameMatcher.find()) {
			_name = nameMatcher.group(0);
		}
		else {
			_name = "-";
		}

		pricePattern = Pattern.compile("[0-9]+[.]([0-9])*");

		Matcher priceMatcher = pricePattern.matcher(goodDescriptor);

		if (priceMatcher.find()) {
			_price = Double.parseDouble(priceMatcher.group(0));
		}
		else {
			_price = 0.0;
		}
	}

	public double getFinalPrice() {
		return getPrice() + getTax();
	}

	public String getName() {
		return _name;
	}

	public double getPrice() {
		return _price;
	}

	public double getTax() {
		return roundToNearestNickel(_price * getTaxRate());
	}

	protected double getTaxRate() {
		double taxRate = 0.0;

		if (_imported) {
			taxRate += _importedTaxRate;
		}

		if (!_exempt) {
			taxRate += _taxRate;
		}

		return taxRate;
	}

	protected double roundToNearestNickel(double tax) {
		return Math.ceil(tax * 20.0) / 20.0;
	}

	private final boolean _exempt;
	private final boolean _imported;
	private final double _importedTaxRate;
	private final String _name;
	private final double _price;
	private final double _taxRate;

}