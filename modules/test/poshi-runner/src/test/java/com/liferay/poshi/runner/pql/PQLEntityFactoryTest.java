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
import java.util.TreeSet;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * @author Michael Hashimoto
 */
public class PQLEntityFactoryTest extends TestCase {

	@Test
	public void testPQLQueryErrorComparativeOperator() throws Exception {
		Properties properties = new Properties();

		properties.setProperty("component.names", "Blogs,Message Boards,WEM");
		properties.setProperty("portal.smoke", "true");
		properties.setProperty("priority", "5");

		_validateQueryError("true ==", "Invalid value: true ==", properties);
		_validateQueryError("false ==", "Invalid value: false ==", properties);
		_validateQueryError("== true", "Invalid value: == true", properties);
		_validateQueryError("== false", "Invalid value: == false", properties);
	}

	@Test
	public void testPQLQueryErrorConditionalOperator() throws Exception {
		Properties properties = new Properties();

		properties.setProperty("component.names", "Blogs,Message Boards,WEM");
		properties.setProperty("portal.smoke", "true");
		properties.setProperty("priority", "5");

		_validateQueryError(
			"AND true == true", "Invalid value: AND true == true", properties);
		_validateQueryError(
			"true == true AND", "Invalid value: true == true AND", properties);
		_validateQueryError(
			"OR true == true", "Invalid value: OR true == true", properties);
		_validateQueryError(
			"true == true OR", "Invalid value: true == true OR", properties);
		_validateQueryError(
			"true == true AND AND false == false",
			"Invalid value: AND false == false", properties);
	}

	@Test
	public void testPQLQueryErrorSpecial() throws Exception {
		Properties properties = new Properties();

		properties.setProperty("component.names", "Blogs,Message Boards,WEM");
		properties.setProperty("portal.smoke", "true");
		properties.setProperty("priority", "5");

		_validateQueryError(
			"(true == true) AND", "Invalid value: (true == true) AND",
			properties);
		_validateQueryError(
			"(true == true) OR", "Invalid value: (true == true) OR",
			properties);
	}

	@Test
	public void testPQLQueryGetValueComparativeOperators() throws Exception {
		Properties properties = new Properties();

		properties.setProperty("component.names", "Blogs,Message Boards,WEM");
		properties.setProperty("portal.smoke", "true");
		properties.setProperty("priority", "5");

		Set<String> queries = new TreeSet<>();

		queries.add("component.names ~ 'Message Boards'");
		queries.add("component.names !~ 'Journal'");

		queries.add("portal.smoke == true");
		queries.add("portal.smoke != false");

		queries.add("priority > 4");
		queries.add("priority >= 5");
		queries.add("priority < 6");
		queries.add("priority <= 5");

		queries.add("priority > 4.1");
		queries.add("priority >= 4.9");
		queries.add("priority < 5.1");
		queries.add("priority <= 5.1");

		for (String query : queries) {
			_validateQueryResult(query, Boolean.valueOf(true), properties);
		}

		queries = new TreeSet<>();

		queries.add("component.names !~ 'Message Boards'");
		queries.add("component.names ~ 'Journal'");
		queries.add("portal.smoke != true");
		queries.add("portal.smoke == false");

		queries.add("priority < 4");
		queries.add("priority <= 4");
		queries.add("priority > 6");
		queries.add("priority >= 6");

		queries.add("priority < 4.1");
		queries.add("priority <= 4.9");
		queries.add("priority > 5.1");
		queries.add("priority >= 5.1");

		for (String query : queries) {
			_validateQueryResult(query, Boolean.valueOf(false), properties);
		}
	}

	@Test
	public void testPQLQueryGetValueConditionalOperators() throws Exception {
		Properties properties = new Properties();

		properties.setProperty("component.names", "Blogs,Message Boards,WEM");
		properties.setProperty("portal.smoke", "true");
		properties.setProperty("priority", "5");

		Set<String> queries = new TreeSet<>();

		queries.add("portal.smoke == true AND portal.smoke != false");
		queries.add("portal.smoke == true OR portal.smoke == false");

		queries.add("true AND true");
		queries.add("true OR true");
		queries.add("false OR true");
		queries.add("true OR false");

		for (String query : queries) {
			_validateQueryResult(query, Boolean.valueOf(true), properties);
		}

		queries = new TreeSet<>();

		queries.add("portal.smoke == true AND portal.smoke == false");
		queries.add("portal.smoke != true OR portal.smoke == false");

		queries.add("false AND true");
		queries.add("true AND false");
		queries.add("false AND false");
		queries.add("false OR false");

		for (String query : queries) {
			_validateQueryResult(query, Boolean.valueOf(false), properties);
		}
	}

	@Test
	public void testPQLQueryGetValueParenthesis() throws Exception {
		Properties properties = new Properties();

		properties.setProperty("component.names", "Blogs,Message Boards,WEM");
		properties.setProperty("portal.smoke", "true");
		properties.setProperty("priority", "5");

		Set<String> queries = new TreeSet<>();

		queries.add("(portal.smoke == true OR portal.smoke == false) AND true");
		queries.add("(portal.smoke == true AND portal.smoke == false) OR true");

		queries.add("(true OR false) AND true");
		queries.add("(true AND false) OR true");
		queries.add("(true AND false) OR true");
		queries.add("(true AND true) OR false");

		for (String query : queries) {
			_validateQueryResult(query, Boolean.valueOf(true), properties);
		}

		queries = new TreeSet<>();

		queries.add("(portal.smoke != true OR portal.smoke == false) AND true");
		queries.add("(portal.smoke != true AND portal.smoke == true) OR false");

		queries.add("(false OR false) AND true");
		queries.add("(false OR true) AND false");
		queries.add("(false OR false) AND false");
		queries.add("(false AND true) OR false");

		for (String query : queries) {
			_validateQueryResult(query, Boolean.valueOf(false), properties);
		}
	}

	@Test
	public void testPQLQueryModifier() throws Exception {
		Properties properties = new Properties();

		properties.setProperty("portal.smoke", "true");

		_validateQueryResult(
			"NOT portal.smoke == true", Boolean.valueOf(false), properties);
		_validateQueryResult(
			"NOT portal.smoke == false", Boolean.valueOf(true), properties);
	}

	@Test
	public void testPQLQueryModifierError() throws Exception {
		_validateQueryError(
			"portal.smoke == true NOT", "Invalid value: true NOT");
		_validateQueryError(
			"portal.smoke == false NOT", "Invalid value: false NOT");
		_validateQueryError(
			"portal.smoke == true NOT AND true", "Invalid value: true NOT");
		_validateQueryError(
			"portal.smoke == false NOT AND true", "Invalid value: false NOT");
	}

	@Test
	public void testPQLValueGetValue() throws Exception {
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
	public void testPQLValueModifier() throws Exception {
		_validateQueryResult("NOT true", Boolean.valueOf(false));
		_validateQueryResult("NOT false", Boolean.valueOf(true));
	}

	@Test
	public void testPQLValueModifierError() throws Exception {
		_validateQueryError("NOT 3.2", "Invalid usage of 'NOT' modifier.");
		_validateQueryError("NOT 2016", "Invalid usage of 'NOT' modifier.");
		_validateQueryError("NOT test", "Invalid usage of 'NOT' modifier.");
		_validateQueryError(
			"NOT 'test test'", "Invalid usage of 'NOT' modifier.");
	}

	@Test
	public void testPQLVariableGetValue() throws Exception {
		_validateVariableResult("false", Boolean.valueOf(false));
		_validateVariableResult("'false'", Boolean.valueOf(false));
		_validateVariableResult("\"false\"", Boolean.valueOf(false));
		_validateVariableResult("true", Boolean.valueOf(true));
		_validateVariableResult("'true'", Boolean.valueOf(true));
		_validateVariableResult("\"true\"", Boolean.valueOf(true));

		_validateVariableResult("3.2", Double.valueOf(3.2));
		_validateVariableResult("'3.2'", Double.valueOf(3.2));
		_validateVariableResult("\"3.2\"", Double.valueOf(3.2));
		_validateVariableResult("2016.0", Double.valueOf(2016));
		_validateVariableResult("'2016.0'", Double.valueOf(2016));
		_validateVariableResult("\"2016.0\"", Double.valueOf(2016));

		_validateVariableResult("2016", Integer.valueOf(2016));
		_validateVariableResult("'2016'", Integer.valueOf(2016));
		_validateVariableResult("\"2016\"", Integer.valueOf(2016));

		_validateVariableResult("test", "test");
		_validateVariableResult("'test'", "test");
		_validateVariableResult("\"test\"", "test");

		_validateVariableResult("'test test'", "test test");
		_validateVariableResult("\"test test\"", "test test");
	}

	private static void _validateQueryError(String query, String expected)
		throws Exception {

		_validateQueryError(query, expected, new Properties());
	}

	private static void _validateQueryError(
			String query, String expected, Properties properties)
		throws Exception {

		String actual = null;

		try {
			PQLEntity pqlEntity = PQLEntityFactory.newEntity(query);

			Object objectValue = pqlEntity.getValue(properties);
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

	private static void _validateQueryResult(String query, Object expected)
		throws Exception {

		_validateQueryResult(query, expected, new Properties());
	}

	private static void _validateQueryResult(
			String query, Object expected, Properties properties)
		throws Exception {

		PQLEntity pqlEntity = PQLEntityFactory.newEntity(query);

		Object actual = pqlEntity.getValue(properties);

		if (!actual.equals(expected)) {
			StringBuilder sb = new StringBuilder();

			sb.append("Mismatched result within the following query:\n");
			sb.append(query);
			sb.append("\n\n* Actual:   \"");
			sb.append(actual);
			sb.append("\"\n* Expected: \"");
			sb.append(expected);
			sb.append("\"");

			throw new Exception(sb.toString());
		}
	}

	private static void _validateVariableResult(String query, Object expected)
		throws Exception {

		Properties properties = new Properties();

		properties.put("portal.smoke", query);

		_validateQueryResult("portal.smoke", expected, properties);
	}

}