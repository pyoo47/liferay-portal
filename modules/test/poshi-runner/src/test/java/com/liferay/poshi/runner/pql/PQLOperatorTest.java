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
public class PQLOperatorTest extends TestCase {

	@Test
	public void testGetOperator() throws Exception {
		Set<String> availableOperators = PQLOperator.getAvailableOperators();

		for (String operator : availableOperators) {
			PQLOperator pqlOperator = PQLOperatorFactory.newInstance(operator);

			_compare(pqlOperator.getOperator(), operator);
		}
	}

	@Test
	public void testGetValueConditionalOperatorErrors() throws Exception {
		Set<String> conditionalOperators = new HashSet<>();

		conditionalOperators.add("AND");
		conditionalOperators.add("OR");

		for (String operator : conditionalOperators) {
			String expectedError =
				"'" + operator +
					"' operators must be surrounded by 2 boolean values.";

			_validateGetValueError(null, operator, null, expectedError);
			_validateGetValueError("test", operator, "test", expectedError);
			_validateGetValueError("123", operator, "123", expectedError);
			_validateGetValueError("12.3", operator, "12.3", expectedError);

			_validateGetValueError("true", operator, null, expectedError);
			_validateGetValueError("true", operator, "test", expectedError);
			_validateGetValueError("true", operator, "123", expectedError);
			_validateGetValueError("true", operator, "12.3", expectedError);

			_validateGetValueError(null, operator, "false", expectedError);
			_validateGetValueError("test", operator, "false", expectedError);
			_validateGetValueError("123", operator, "false", expectedError);
			_validateGetValueError("12.3", operator, "false", expectedError);
		}
	}

	@Test
	public void testGetValueRelationalOperatorErrors() throws Exception {
		Set<String> conditionalOperators = new HashSet<>();

		conditionalOperators.add("<");
		conditionalOperators.add("<=");
		conditionalOperators.add(">");
		conditionalOperators.add(">=");

		for (String operator : conditionalOperators) {
			String expectedError =
				"The '" + operator + "' operator only works for number values.";

			_validateGetValueError("123", operator, null, expectedError);
			_validateGetValueError("12.3", operator, null, expectedError);
			_validateGetValueError(null, operator, null, expectedError);
			_validateGetValueError(null, operator, "123", expectedError);
			_validateGetValueError(null, operator, "12.3", expectedError);

			_validateGetValueError("123", operator, "true", expectedError);
			_validateGetValueError("12.3", operator, "true", expectedError);
			_validateGetValueError("false", operator, "true", expectedError);
			_validateGetValueError("false", operator, "123", expectedError);
			_validateGetValueError("false", operator, "12.3", expectedError);

			_validateGetValueError("123", operator, "test", expectedError);
			_validateGetValueError("12.3", operator, "test", expectedError);
			_validateGetValueError("test", operator, "test", expectedError);
			_validateGetValueError("test", operator, "123", expectedError);
			_validateGetValueError("test", operator, "12.3", expectedError);
		}
	}

	@Test
	public void testGetValueStringOperatorErrors() throws Exception {
		Set<String> conditionalOperators = new HashSet<>();

		conditionalOperators.add("~");
		conditionalOperators.add("!~");

		for (String operator : conditionalOperators) {
			String expectedError =
				"The '" + operator + "' operator only works for string values.";

			_validateGetValueError("test", operator, "true", expectedError);
			_validateGetValueError("true", operator, "true", expectedError);
			_validateGetValueError("false", operator, "test", expectedError);

			_validateGetValueError("test", operator, "12.3", expectedError);
			_validateGetValueError("12.3", operator, "12.3", expectedError);
			_validateGetValueError("12.3", operator, "test", expectedError);

			_validateGetValueError("test", operator, "123", expectedError);
			_validateGetValueError("123", operator, "123", expectedError);
			_validateGetValueError("123", operator, "test", expectedError);
		}
	}

	@Test
	public void testOperatorConditionalOperatorAND() throws Exception {
		_validateGetValue("true", "AND", "true", Boolean.valueOf(true));
		_validateGetValue("true", "AND", "false", Boolean.valueOf(false));
		_validateGetValue("false", "AND", "false", Boolean.valueOf(false));
	}

	@Test
	public void testOperatorConditionalOperatorOR() throws Exception {
		_validateGetValue("true", "OR", "true", Boolean.valueOf(true));
		_validateGetValue("true", "OR", "false", Boolean.valueOf(true));
		_validateGetValue("false", "OR", "false", Boolean.valueOf(false));
	}

	@Test
	public void testOperatorEqualityOperatorEquals() throws Exception {
		_validateGetValue("test", "==", "test", Boolean.valueOf(true));
		_validateGetValue(null, "==", "test", Boolean.valueOf(false));
		_validateGetValue("test", "==", null, Boolean.valueOf(false));
		_validateGetValue(null, "==", null, Boolean.valueOf(false));
		_validateGetValue("test1", "==", "test2", Boolean.valueOf(false));
	}

	@Test
	public void testOperatorEqualityOperatorNotEquals() throws Exception {
		_validateGetValue("test", "!=", "test", Boolean.valueOf(false));
		_validateGetValue(null, "!=", "test", Boolean.valueOf(false));
		_validateGetValue("test", "!=", null, Boolean.valueOf(false));
		_validateGetValue(null, "!=", null, Boolean.valueOf(false));
		_validateGetValue("test1", "!=", "test2", Boolean.valueOf(true));
	}

	@Test
	public void testOperatorRelationalOperatorGreaterThan() throws Exception {
		_validateGetValue("2", ">", "1", Boolean.valueOf(true));
		_validateGetValue("2.1", ">", "1", Boolean.valueOf(true));
		_validateGetValue("2", ">", "1.1", Boolean.valueOf(true));
		_validateGetValue("2.1", ">", "1.1", Boolean.valueOf(true));

		_validateGetValue("2", ">", "2", Boolean.valueOf(false));
		_validateGetValue("2.1", ">", "2.1", Boolean.valueOf(false));

		_validateGetValue("1", ">", "2", Boolean.valueOf(false));
		_validateGetValue("1.1", ">", "2", Boolean.valueOf(false));
		_validateGetValue("1", ">", "2.1", Boolean.valueOf(false));
		_validateGetValue("1.1", ">", "2.1", Boolean.valueOf(false));
	}

	@Test
	public void testOperatorRelationalOperatorGreaterThanEquals()
		throws Exception {

		_validateGetValue("2", ">=", "1", Boolean.valueOf(true));
		_validateGetValue("2.1", ">=", "1", Boolean.valueOf(true));
		_validateGetValue("2", ">=", "1.1", Boolean.valueOf(true));
		_validateGetValue("2.1", ">=", "1.1", Boolean.valueOf(true));

		_validateGetValue("2", ">=", "2", Boolean.valueOf(true));
		_validateGetValue("2.1", ">=", "2.1", Boolean.valueOf(true));

		_validateGetValue("1", ">=", "2", Boolean.valueOf(false));
		_validateGetValue("1.1", ">=", "2", Boolean.valueOf(false));
		_validateGetValue("1", ">=", "2.1", Boolean.valueOf(false));
		_validateGetValue("1.1", ">=", "2.1", Boolean.valueOf(false));
	}

	@Test
	public void testOperatorRelationalOperatorLessThan() throws Exception {
		_validateGetValue("2", "<", "1", Boolean.valueOf(false));
		_validateGetValue("2.1", "<", "1", Boolean.valueOf(false));
		_validateGetValue("2", "<", "1.1", Boolean.valueOf(false));
		_validateGetValue("2.1", "<", "1.1", Boolean.valueOf(false));

		_validateGetValue("2", "<", "2", Boolean.valueOf(false));
		_validateGetValue("2.1", "<", "2.1", Boolean.valueOf(false));

		_validateGetValue("1", "<", "2", Boolean.valueOf(true));
		_validateGetValue("1.1", "<", "2", Boolean.valueOf(true));
		_validateGetValue("1", "<", "2.1", Boolean.valueOf(true));
		_validateGetValue("1.1", "<", "2.1", Boolean.valueOf(true));
	}

	@Test
	public void testOperatorRelationalOperatorLessThanEquals()
		throws Exception {

		_validateGetValue("2", "<=", "1", Boolean.valueOf(false));
		_validateGetValue("2.1", "<=", "1", Boolean.valueOf(false));
		_validateGetValue("2", "<=", "1.1", Boolean.valueOf(false));
		_validateGetValue("2.1", "<=", "1.1", Boolean.valueOf(false));

		_validateGetValue("2", "<=", "2", Boolean.valueOf(true));
		_validateGetValue("2.1", "<=", "2.1", Boolean.valueOf(true));

		_validateGetValue("1", "<=", "2", Boolean.valueOf(true));
		_validateGetValue("1.1", "<=", "2", Boolean.valueOf(true));
		_validateGetValue("1", "<=", "2.1", Boolean.valueOf(true));
		_validateGetValue("1.1", "<=", "1.1", Boolean.valueOf(true));
	}

	@Test
	public void testOperatorStringOperatorContains() throws Exception {
		_validateGetValue("test", "~", "test", Boolean.valueOf(true));
		_validateGetValue("test1", "~", "test", Boolean.valueOf(true));
		_validateGetValue(null, "~", "test", Boolean.valueOf(false));
		_validateGetValue("test", "~", null, Boolean.valueOf(false));
		_validateGetValue(null, "~", null, Boolean.valueOf(false));
		_validateGetValue("test1", "~", "test2", Boolean.valueOf(false));
	}

	@Test
	public void testOperatorStringOperatorNotContains() throws Exception {
		_validateGetValue("test", "!~", "test", Boolean.valueOf(false));
		_validateGetValue("test1", "!~", "test", Boolean.valueOf(false));
		_validateGetValue(null, "!~", "test", Boolean.valueOf(false));
		_validateGetValue("test", "!~", null, Boolean.valueOf(false));
		_validateGetValue(null, "!~", null, Boolean.valueOf(false));
		_validateGetValue("test1", "!~", "test2", Boolean.valueOf(true));
	}

	@Test
	public void testOperatorValidate() throws Exception {
		Set<String> availableOperators = PQLOperator.getAvailableOperators();

		for (String operator : availableOperators) {
			PQLOperator.validateOperator(operator);
		}
	}

	@Test
	public void testOperatorValidateError() throws Exception {
		Set<String> operators = new HashSet<>();

		operators.add(null);
		operators.add("bad");
		operators.add("bad value");
		operators.addAll(PQLModifier.getAvailableModifiers());

		for (String operator : operators) {
			_validateOperatorError(
				operator, "Invalid '" + operator + "' operator.");
		}
	}

	private void _compare(Object actual, Object expected) throws Exception {
		if (!actual.equals(expected)) {
			StringBuilder sb = new StringBuilder();

			sb.append("Mismatched values:\n");
			sb.append("\n\n* Actual:   \"");
			sb.append(actual);
			sb.append("\"\n* Expected: \"");
			sb.append(expected);
			sb.append("\"");

			throw new Exception(sb.toString());
		}
	}

	private void _validateGetValue(
			String value1, String operator, String value2, Object expected)
		throws Exception {

		PQLEntity pqlEntity1 = PQLEntityFactory.newInstance(value1);
		PQLOperator pqlOperator = PQLOperatorFactory.newInstance(operator);
		PQLEntity pqlEntity2 = PQLEntityFactory.newInstance(value2);

		Object actual = pqlOperator.getValue(
			pqlEntity1, pqlEntity2, new Properties());

		_compare(actual, expected);
	}

	private void _validateGetValueError(
			String value1, String operator, String value2, String expected)
		throws Exception {

		PQLEntity pqlEntity1 = PQLEntityFactory.newInstance(value1);
		PQLOperator pqlOperator = PQLOperatorFactory.newInstance(operator);
		PQLEntity pqlEntity2 = PQLEntityFactory.newInstance(value2);

		String actual = null;

		try {
			pqlOperator.getValue(pqlEntity1, pqlEntity2, new Properties());
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
				throw new Exception("No error thrown for getValue.");
			}
		}
	}

	private void _validateOperatorError(String operator, String expected)
		throws Exception {

		String actual = null;

		try {
			PQLOperator.validateOperator(operator);
		}
		catch (Exception e) {
			actual = e.getMessage();

			if (!actual.equals(expected)) {
				StringBuilder sb = new StringBuilder();

				sb.append("Mismatched error for PQLOperator declaration:\n");
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
				throw new Exception("No error thrown for invalid PQLOperator.");
			}
		}
	}

}