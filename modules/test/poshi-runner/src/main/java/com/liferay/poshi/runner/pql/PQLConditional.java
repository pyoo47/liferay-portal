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

package com.liferay.poshi.runner.pql;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Michael Hashimoto
 */
public class PQLConditional implements PQLQueryEntity {

	public PQLConditional(String conditional, Properties properties)
		throws Exception {

		_conditional = conditional;
		_properties = properties;

		Pattern conditionalPattern = PQLConditionalFactory.getPattern();

		Matcher conditionalMatcher = conditionalPattern.matcher(_conditional);

		conditionalMatcher.find();

		_pqlField = new PQLField(conditionalMatcher.group(1));

		_pqlFieldValue = _pqlField.getPQLValue(_properties);

		_pqlOperator = PQLOperatorFactory.build(
			conditionalMatcher.group(2), properties);

		String value = conditionalMatcher.group(3);

		if (value.startsWith("\"") && value.endsWith("\"")) {
			value = value.substring(1, value.length() - 1);
		}

		_pqlValue = PQLValueFactory.build(value);
	}

	public boolean getResult() throws Exception {
		return _pqlOperator.evaluate(_pqlFieldValue, _pqlValue);
	}

	public String toString() {
		return _conditional;
	}

	private final String _conditional;
	private final PQLField _pqlField;
	private final PQLValue _pqlFieldValue;
	private final PQLOperator _pqlOperator;
	private final PQLValue _pqlValue;
	private final Properties _properties;

}