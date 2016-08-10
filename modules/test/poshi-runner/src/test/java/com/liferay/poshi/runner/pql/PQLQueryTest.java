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
public class PQLQueryTest extends TestCase {

	@Test
	public void testGetValueComparativeOperators() throws Exception {
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
	public void testGetValueConditionalOperators() throws Exception {
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
	public void testGetValueParenthesis() throws Exception {
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
	public void testQueryErrorComparativeOperator() throws Exception {
		Properties properties = new Properties();

		properties.setProperty("component.names", "Blogs,Message Boards,WEM");
		properties.setProperty("portal.smoke", "true");
		properties.setProperty("priority", "5");

		_validateQueryError("true ==", "Invalid query: true ==", properties);
		_validateQueryError("false ==", "Invalid query: false ==", properties);
		_validateQueryError("== true", "Invalid query: == true", properties);
		_validateQueryError("== false", "Invalid query: == false", properties);
	}

	@Test
	public void testQueryErrorConditionalOperator() throws Exception {
		Properties properties = new Properties();

		properties.setProperty("component.names", "Blogs,Message Boards,WEM");
		properties.setProperty("portal.smoke", "true");
		properties.setProperty("priority", "5");

		_validateQueryError(
			"AND true == true", "Invalid query: AND true == true", properties);
		_validateQueryError(
			"true == true AND", "Invalid query: true == true AND", properties);
		_validateQueryError(
			"OR true == true", "Invalid query: OR true == true", properties);
		_validateQueryError(
			"true == true OR", "Invalid query: true == true OR", properties);
		_validateQueryError(
			"true == true AND AND false == false",
			"Invalid query: AND false == false", properties);
	}

	@Test
	public void testQueryErrorParenthesis() throws Exception {
		Properties properties = new Properties();

		properties.setProperty("component.names", "Blogs,Message Boards,WEM");
		properties.setProperty("portal.smoke", "true");
		properties.setProperty("priority", "5");

		_validateQueryError(
			"(true == true", "Invalid query: (true == true", properties);
		_validateQueryError(
			"true == true) AND", "Invalid query: true) AND", properties);
		_validateQueryError(
			")true == true(", "Invalid query: )true == true(", properties);
	}

	@Test
	public void testQueryModifier() throws Exception {
		Properties properties = new Properties();

		properties.setProperty("portal.smoke", "true");

		_validateQueryResult(
			"NOT portal.smoke == true", Boolean.valueOf(false), properties);
		_validateQueryResult(
			"NOT portal.smoke == false", Boolean.valueOf(true), properties);
	}

	@Test
	public void testQueryModifierError() throws Exception {
		_validateQueryError(
			"portal.smoke == true NOT", "Invalid query: true NOT");
		_validateQueryError(
			"portal.smoke == false NOT", "Invalid query: false NOT");
		_validateQueryError(
			"portal.smoke == true NOT AND true", "Invalid query: true NOT");
		_validateQueryError(
			"portal.smoke == false NOT AND true", "Invalid query: false NOT");
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
			PQLQuery pqlQuery = new PQLQuery(query);

			Object result = pqlQuery.getValue(properties);
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

	private static void _validateQueryResult(
			String query, Object expected, Properties properties)
		throws Exception {

		PQLQuery pqlQuery = new PQLQuery(query);

		Object actual = pqlQuery.getValue(properties);

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

}