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

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * @author Michael Hashimoto
 */
public class PQLVariableTest extends TestCase {

	@Test
	public void testGetValue() throws Exception {
		_validateQueryResult("false", Boolean.valueOf(false));
		_validateQueryResult("'false'", Boolean.valueOf(false));
		_validateQueryResult("\"false\"", Boolean.valueOf(false));
		_validateQueryResult("true", Boolean.valueOf(true));
		_validateQueryResult("'true'", Boolean.valueOf(true));
		_validateQueryResult("\"true\"", Boolean.valueOf(true));

		_validateQueryResult("3.2", Double.valueOf(3.2));
		_validateQueryResult("'3.2'", Double.valueOf(3.2));
		_validateQueryResult("\"3.2\"", Double.valueOf(3.2));
		_validateQueryResult("2016.0", Double.valueOf(2016));
		_validateQueryResult("'2016.0'", Double.valueOf(2016));
		_validateQueryResult("\"2016.0\"", Double.valueOf(2016));

		_validateQueryResult("2016", Integer.valueOf(2016));
		_validateQueryResult("'2016'", Integer.valueOf(2016));
		_validateQueryResult("\"2016\"", Integer.valueOf(2016));

		_validateQueryResult("test", "test");
		_validateQueryResult("'test'", "test");
		_validateQueryResult("\"test\"", "test");

		_validateQueryResult("'test test'", "test test");
		_validateQueryResult("\"test test\"", "test test");
	}

	@Test
	public void testValueError() throws Exception {
		Set<String> variables = new HashSet<>();

		variables.add("invalid.property");

		for (String variable : variables) {
			_validateQueryError(
				variable,
				"Property not found in 'test.case.available.property.names': " +
					variable);
		}

		variables = new HashSet<>();

		variables.add(null);
		variables.add("test == test");
		variables.add("true OR true");

		for (String variable : variables) {
			_validateQueryError(variable, "Invalid query: " + variable);
		}
	}

	private void _validateQueryError(String query, String expected)
		throws Exception {

		String actual = null;

		try {
			PQLVariable pqlVariable = new PQLVariable(query);
		}
		catch (Exception e) {
			actual = e.getMessage();

			if (!actual.equals(expected)) {
				StringBuilder sb = new StringBuilder();

				sb.append("Mismatched error:\n");
				sb.append("\n\n* Actual:   \"");
				sb.append(actual);
				sb.append("\"\n* Expected: \"");
				sb.append(expected);
				sb.append("\"");

				throw new Exception(sb.toString(), e);
			}
		}
		finally {
			if (actual == null) {
				throw new Exception(
					"No error thrown for the following query:\n" + query);
			}
		}
	}

	private void _validateQueryResult(String query, Object expected)
		throws Exception {

		Properties properties = new Properties();

		properties.put("portal.smoke", query);

		Class clazz = expected.getClass();

		PQLVariable pqlVariable = new PQLVariable("portal.smoke");

		Object actual = pqlVariable.getValue(properties);

		if (!clazz.isInstance(actual)) {
			throw new Exception(
				query + " should be of type '" + clazz.getName() + "'");
		}

		if (!actual.equals(expected)) {
			StringBuilder sb = new StringBuilder();

			sb.append("Mismatched value within the following query:\n");
			sb.append(query);
			sb.append("\n\n* Actual:   \"");
			sb.append(actual);
			sb.append("\"\n* Expected: \"");
			sb.append(expected);
			sb.append("\"");

			throw new Exception(sb.toString());
		}
	}

}