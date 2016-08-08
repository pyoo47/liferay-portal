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

import java.util.List;
import java.util.Properties;

/**
 * @author Michael Hashimoto
 */
public class PQLQuery extends PQLEntity {

	public static boolean isQuery(String query) {
		if (query == null) {
			return false;
		}

		String[] parameters = _getParameters(query);

		if (parameters == null) {
			return false;
		}

		return true;
	}

	public PQLQuery(String query) throws Exception {
		super(query);

		_validateQuery(query);

		String[] parameters = _getParameters(getFixedQuery());

		String value1 = parameters[0];
		String operator = parameters[1];
		String value2 = parameters[2];

		_pqlEntity1 = PQLEntityFactory.newInstance(value1);
		_pqlOperator = PQLOperatorFactory.newInstance(operator);
		_pqlEntity2 = PQLEntityFactory.newInstance(value2);
	}

	public Object getValue(Properties properties) throws Exception {
		Object objectValue = _pqlOperator.getValue(
			_pqlEntity1, _pqlEntity2, properties);

		if (!(objectValue instanceof Boolean)) {
			throw new Exception("Unable to evaluate " + getFixedQuery());
		}

		PQLModifier pqlModifier = getPQLModifier();

		if (pqlModifier != null) {
			objectValue = pqlModifier.modify(objectValue);
		}

		return objectValue;
	}

	private static int _getOperatorIndex(String query, String operator) {
		int parenthesisCount = 0;

		for (int i = 0; i < query.length(); i++) {
			char c = query.charAt(i);

			if (c == '(') {
				parenthesisCount++;
			}

			if (c == ')') {
				parenthesisCount--;
			}

			if (parenthesisCount < 0) {
				return -1;
			}

			if (parenthesisCount == 0) {
				boolean found = true;

				for (int j = 0; j < operator.length(); j++) {
					if ((i + j) > (query.length() - 1)) {
						found = false;

						break;
					}

					if (!(operator.charAt(j) == query.charAt(i + j))) {
						found = false;

						break;
					}
				}

				if (found) {
					return i;
				}
			}
		}

		return -1;
	}

	private static String[] _getParameters(String query) {
		query = fixQuery(query);

		String targetOperator = null;
		int targetOperatorIndex = query.length();

		List<List<String>> prioritizedOperatorList =
			PQLOperator.getPrioritizedOperatorList();

		for (int i = (prioritizedOperatorList.size() - 1); i >= 0; i--) {
			List<String> operators = prioritizedOperatorList.get(i);

			for (String operator : operators) {
				int operatorIndex = _getOperatorIndex(query, operator);

				if ((operatorIndex > -1) &&
					(operatorIndex < targetOperatorIndex)) {

					targetOperator = operator;
					targetOperatorIndex = operatorIndex;
				}
			}

			if (targetOperator != null) {
				break;
			}
		}

		if (targetOperator != null) {
			int x = targetOperatorIndex;
			int y = targetOperatorIndex + targetOperator.length();

			String value1 = fixQuery(query.substring(0, x));
			String operator = fixQuery(query.substring(x, y));
			String value2 = fixQuery(query.substring(y));

			if (value1.equals("") || value2.equals("")) {
				return null;
			}

			return new String[] {value1, operator, value2};
		}

		return null;
	}

	private void _validateQuery(String query) throws Exception {
		if (!isQuery(query)) {
			throw new Exception("Invalid query: " + query);
		}
	}

	private final PQLEntity _pqlEntity1;
	private final PQLEntity _pqlEntity2;
	private final PQLOperator _pqlOperator;

}