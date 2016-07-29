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

import com.liferay.poshi.runner.util.StringPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Michael Hashimoto
 */
public class PQLOperatorFactory {

	public static PQLOperator build(String operator, Properties properties)
		throws Exception {

		PQLOperator pqlOperator = null;

		switch (operator) {
			case _CONTAINS:
				pqlOperator = new PQLOperatorContains(properties);

				break;

			case _EQUALS:
				pqlOperator = new PQLOperatorEquals(properties);

				break;

			case _NOT_CONTAINS:
				pqlOperator = new PQLOperatorNotEquals(properties);

				break;

			case _NOT_EQUALS:
				pqlOperator = new PQLOperatorNotEquals(properties);

				break;

			default:
				throw new Exception("Invalid operator!");
		}

		return pqlOperator;
	}

	public static List<String> getOperators() {
		return _operators;
	}

	private static final String _CONTAINS = StringPool.TILDE;

	private static final String _EQUALS = StringPool.EQUAL + StringPool.EQUAL;

	private static final String _NOT_CONTAINS = "!~";

	private static final String _NOT_EQUALS = StringPool.NOT_EQUAL;

	private static final List<String> _operators = new ArrayList<>();

	static {
		_operators.add(_CONTAINS);
		_operators.add(_EQUALS);
		_operators.add(_NOT_CONTAINS);
		_operators.add(_NOT_EQUALS);
	}

}