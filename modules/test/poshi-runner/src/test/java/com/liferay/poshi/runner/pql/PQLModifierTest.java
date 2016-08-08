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
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * @author Michael Hashimoto
 */
public class PQLModifierTest extends TestCase {

	@Test
	public void testGetModifier() throws Exception {
		Set<String> availableModifiers = PQLModifier.getAvailableModifiers();

		for (String modifier : availableModifiers) {
			PQLModifier pqlModifier = PQLModifierFactory.newInstance(modifier);

			_compare(pqlModifier.getModifier(), modifier);
		}
	}

	@Test
	public void testModify() throws Exception {
		_validateModify("NOT", Boolean.valueOf(true), Boolean.valueOf(false));
		_validateModify("NOT", Boolean.valueOf(false), Boolean.valueOf(true));
	}

	@Test
	public void testModifyError() throws Exception {
		_validateModifyError("NOT", null);
		_validateModifyError("NOT", "test");
		_validateModifyError("NOT", Double.valueOf(10.0));
		_validateModifyError("NOT", Integer.valueOf(10));
	}

	@Test
	public void testValidateModifier() throws Exception {
		Set<String> availableModifiers = PQLModifier.getAvailableModifiers();

		for (String modifier : availableModifiers) {
			PQLModifier.validateModifier(modifier);
		}
	}

	@Test
	public void testValidateModifierError() throws Exception {
		Set<String> modifiers = new HashSet<>();

		modifiers.add(null);
		modifiers.add("bad");
		modifiers.add("NOT bad");
		modifiers.addAll(PQLOperator.getAvailableOperators());

		for (String modifier : modifiers) {
			_validateModifierError(
				modifier, "Invalid '" + modifier + "' modifier.");
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

	private void _validateModifierError(String modifier, String expected)
		throws Exception {

		String actual = null;

		try {
			PQLModifier.validateModifier(modifier);
		}
		catch (Exception e) {
			actual = e.getMessage();

			if (!actual.equals(expected)) {
				StringBuilder sb = new StringBuilder();

				sb.append("Mismatched error validate modifier:\n");
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
				throw new Exception("No error thrown.");
			}
		}
	}

	private void _validateModify(
			String operator, Object objectValue, Object expected)
		throws Exception {

		PQLModifier pqlModifier = PQLModifierFactory.newInstance(operator);

		Object actual = pqlModifier.modify(objectValue);

		_compare(actual, expected);
	}

	private void _validateModifyError(String operator, Object objectValue)
		throws Exception {

		PQLModifier pqlModifier = PQLModifierFactory.newInstance(operator);

		String actual = null;
		String expected = "Invalid usage of '" + operator + "' modifier.";

		try {
			pqlModifier.modify(objectValue);
		}
		catch (Exception e) {
			actual = e.getMessage();

			if (!actual.equals(expected)) {
				StringBuilder sb = new StringBuilder();

				sb.append("Mismatched error modify:\n");
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
				throw new Exception("No error thrown.");
			}
		}
	}

}