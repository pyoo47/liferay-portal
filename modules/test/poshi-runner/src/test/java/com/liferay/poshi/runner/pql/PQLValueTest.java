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
public class PQLValueTest extends TestCase {

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
	public void testGetValueNull() throws Exception {
		_validateQueryResultNull(null);
		_validateQueryResultNull("'null'");
		_validateQueryResultNull("\"null\"");
	}

	@Test
	public void testValueError() throws Exception {
		Set<String> queries = new HashSet<>();

		queries.add("test test");
		queries.add("true AND true");
		queries.add("test == test");

		for (String query : queries) {
			_validateQueryError(query, "Invalid query: " + query);
		}
	}

	@Test
	public void testValueModifier() throws Exception {
		_validateQueryResult("NOT true", Boolean.valueOf(false));
		_validateQueryResult("NOT false", Boolean.valueOf(true));
	}

	@Test
	public void testValueModifierError() throws Exception {
		_validateQueryError("NOT 3.2", "Invalid usage of 'NOT' modifier.");
		_validateQueryError("NOT 2016", "Invalid usage of 'NOT' modifier.");
		_validateQueryError("NOT test", "Invalid usage of 'NOT' modifier.");
		_validateQueryError(
			"NOT 'test test'", "Invalid usage of 'NOT' modifier.");
	}

	private void _validateQueryError(String query, String expected)
		throws Exception {

		String actual = null;

		try {
			PQLValue pqlValue = new PQLValue(query);

			Object valueObject = pqlValue.getValue(new Properties());
		}
		catch (Exception e) {
			actual = e.getMessage();

			if (!actual.equals(expected)) {
				StringBuilder sb = new StringBuilder();

				sb.append("Mismatched error within the following query:\n");
				sb.append(query);
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

		Class clazz = expected.getClass();

		PQLValue pqlValue = new PQLValue(query);

		Object actual = pqlValue.getValue(properties);

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

	private void _validateQueryResultNull(String query) throws Exception {
		Properties properties = new Properties();

		PQLValue pqlValue = new PQLValue(query);

		Object actual = pqlValue.getValue(properties);
		Object expected = null;

		if (actual != null) {
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