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
import java.util.Set;

/**
 * @author Michael Hashimoto
 */
public class PQLEntity {

	public static void validateEntity(String value) throws Exception {
		if (value == null) {
			return;
		}

		if ((value.startsWith("'") && value.endsWith("'")) ||
			(value.startsWith("\"") && value.endsWith("\""))) {

			return;
		}

		Set<String> availableModifiers = PQLModifier.getAvailableModifiers();

		for (String modifier : availableModifiers) {
			if (value.contains(modifier)) {
				throw new Exception(
					"Invalid usage of '" + modifier + "' modifier.");
			}
		}

		Set<String> availableOperators = PQLOperator.getAvailableOperators();

		for (String operator : availableOperators) {
			if (value.contains(operator)) {
				throw new Exception(
					"Invalid usage of '" + operator + "' operator.");
			}
		}
	}

	public PQLEntity() {
		_value = null;
	}

	public PQLEntity(String value) throws Exception {
		validateEntity(value);

		_value = value;
	}

	public Object getValue(Properties properties) throws Exception {
		if (properties.containsKey(_value)) {
			return _getValue(properties.getProperty(_value), true);
		}

		return _getValue(_value);
	}

	private Object _getValue(String value) {
		return _getValue(value, false);
	}

	private Object _getValue(String value, boolean validValue) {
		if (value == null) {
			return null;
		}

		if ((value.startsWith("'") && value.endsWith("'")) ||
			(value.startsWith("\"") && value.endsWith("\""))) {

			value = value.substring(1, value.length() - 1);

			validValue = true;
		}

		if (value.equals("true") || value.equals("false")) {
			return Boolean.valueOf(value);
		}
		else if (value.matches("\\d+\\.\\d+")) {
			return Double.valueOf(value);
		}
		else if (value.matches("\\d+")) {
			return Integer.valueOf(value);
		}

		if (validValue) {
			return value;
		}

		return null;
	}

	private final String _value;

}