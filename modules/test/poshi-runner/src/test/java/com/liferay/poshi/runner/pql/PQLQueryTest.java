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

	public static void validateQueryError(
			String query, Properties properties, String expectedError)
		throws Exception {

		String actualError = null;

		try {
			PQLQuery pqlQuery = PQLQueryFactory.newInstance(query);

			Boolean actualResult = pqlQuery.getValue(properties);
		}
		catch (Exception e) {
			actualError = e.getMessage();

			if (!actualError.equals(expectedError)) {
				StringBuilder sb = new StringBuilder();

				sb.append("Mismatched error within the following query:\n");
				sb.append(query);
				sb.append("\n\n* Actual:   \"");
				sb.append(actualError);
				sb.append("\"\n* Expected: \"");
				sb.append(expectedError);
				sb.append("\"");

				throw new Exception(sb.toString(), e);
			}
		}
		finally {
			if (actualError == null) {
				throw new Exception(
					"No error thrown for the following query:\n" + query);
			}
		}
	}

	public static void validateQueryResult(
			String query, Properties properties, Boolean expectedResult)
		throws Exception {

		PQLQuery pqlQuery = PQLQueryFactory.newInstance(query);

		Boolean actualResult = pqlQuery.getValue(properties);

		if (!actualResult.equals(expectedResult)) {
			StringBuilder sb = new StringBuilder();

			sb.append("Expected '");
			sb.append(expectedResult);
			sb.append("' given the following query:\n");
			sb.append(query);

			throw new Exception(sb.toString());
		}
	}

	@Test
	public void testContains() throws Exception {
		Properties properties = new Properties();

		properties.setProperty("component", "Blogs,Message Boards,WEM");
		properties.setProperty("portal", "true");

		Set<String> queries = new TreeSet<>();

		queries.add("component ~ 'Blogs'");
		queries.add("(NOT (component !~ 'Message Boards'))");

		for (String query : queries) {
			validateQueryResult(query, properties, true);
		}
	}

	@Test
	public void testEquals() throws Exception {
		Properties properties = new Properties();

		properties.setProperty("component", "Blogs,Search,WEM");
		properties.setProperty("portal", "true");

		Set<String> queries = new TreeSet<>();

		queries.add("portal == true");
		queries.add("portal != false");

		for (String query : queries) {
			validateQueryResult(query, properties, true);
		}

		queries = new TreeSet<>();

		queries.add("fake != false");
		queries.add("fake == false");

		for (String query : queries) {
			validateQueryResult(query, properties, false);
		}
	}

	@Test
	public void testInvalidQuery() throws Exception {
		Properties properties = new Properties();

		properties.setProperty("aaa", "true");
		properties.setProperty("bbb", "true");
		properties.setProperty("ccc", "true");

		Set<String> queries = new TreeSet<>();

		queries.add("(aaa == true");
		queries.add("aaa == true)");
		queries.add(")aaa == true(");

		for (String query : queries) {
			validateQueryError(query, properties, "Invalid query: " + query);
		}
	}

	@Test
	public void testInvalidQueryKeywords() throws Exception {
		Properties properties = new Properties();

		properties.setProperty("aaa", "true");
		properties.setProperty("bbb", "true");
		properties.setProperty("ccc", "true");

		validateQueryError(
			"AND aaa == true", properties,
			"'AND' operators must be surrounded by 2 boolean values.");

		validateQueryError(
			"OR aaa == true", properties,
			"'OR' operators must be surrounded by 2 boolean values.");

		validateQueryError(
			"aaa == true AND", properties,
			"'AND' operators must be surrounded by 2 boolean values.");

		validateQueryError(
			"aaa == true NOT", properties, "Invalid usage of 'NOT' modifier.");

		validateQueryError(
			"aaa == true OR", properties,
			"'OR' operators must be surrounded by 2 boolean values.");

		validateQueryError(
			"aaa == true NOT AND bbb == false", properties,
			"Invalid usage of 'NOT' modifier.");

		validateQueryError(
			"bbb == true AND AND bbb == false", properties,
			"'AND' operators must be surrounded by 2 boolean values.");

		validateQueryError("NOT NOT", properties, "Invalid query: NOT NOT");
	}

	@Test
	public void testNotKeyword() throws Exception {
		Properties properties = new Properties();

		properties.setProperty("aaa", "true");
		properties.setProperty("bbb", "true");
		properties.setProperty("ccc", "true");

		Set<String> queries = new TreeSet<>();

		queries.add("aaa != false");
		queries.add("NOT (aaa != true)");
		queries.add("((aaa == true) AND NOT (bbb == false))");

		for (String query : queries) {
			validateQueryResult(query, properties, true);
		}

		queries = new TreeSet<>();

		queries.add("aaa != true");
		queries.add("NOT aaa == true");
		queries.add("(NOT ((aaa == true) AND (bbb == true)))");
		queries.add("((aaa == true) AND NOT (bbb == true))");

		for (String query : queries) {
			validateQueryResult(query, properties, false);
		}
	}

}