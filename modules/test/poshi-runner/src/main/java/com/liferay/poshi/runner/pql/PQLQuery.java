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
import java.util.Set;
import java.util.Stack;

/**
 * @author Michael Hashimoto
 */
public class PQLQuery extends PQLEntity {

	public static boolean isQuery(String query) {
		if (_isSimpleQuery(query)) {
			return true;
		}
		else if (_isModifiedQuery(query)) {
			return true;
		}

		return false;
	}

	public static void validateQuery(String query) throws Exception {
		if (!isQuery(query)) {
			throw new Exception("Invalid query: " + query);
		}
	}

	public PQLQuery(String query) throws Exception {
		_query = query;

		validateQuery(query);

		query = _fixQuery(query);

		if (_isModifiedQuery(query)) {
			String modifier = _getLeadingModifier(query);

			_pqlModifier = PQLModifierFactory.newInstance(modifier);

			query = query.substring(modifier.length());
		}
		else {
			_pqlModifier = null;
		}

		String[] parameters = _getParameters(query);

		String value1 = parameters[0];
		String operator = parameters[1];
		String value2 = parameters[2];

		_pqlEntity1 = PQLEntityFactory.newInstance(value1);
		_pqlOperator = PQLOperatorFactory.newInstance(operator);
		_pqlEntity2 = PQLEntityFactory.newInstance(value2);
	}

	public Boolean getValue(Properties properties) throws Exception {
		Boolean booleanValue = _pqlOperator.getValue(
			_pqlEntity1, _pqlEntity2, properties);

		if (_pqlModifier != null) {
			booleanValue = _pqlModifier.modify(booleanValue);
		}

		return booleanValue;
	}

	private static String _fixQuery(String query) {
		query = query.trim();

		while (_isSubquery(query)) {
			query = query.substring(1, query.length() - 1);

			query.trim();
		}

		return query;
	}

	private static String _getLeadingModifier(String query) {
		Set<String> availableModifiers = PQLModifier.getAvailableModifiers();

		for (String modifier : availableModifiers) {
			if (query.startsWith(modifier)) {
				return modifier;
			}
		}

		return null;
	}

	private static int _getOperatorIndex(String query, String operator) {
		Stack<Integer> stack = new Stack<>();

		for (int i = 0; i < query.length(); i++) {
			char c = query.charAt(i);

			if (c == '(') {
				stack.push(i);
			}

			if (c == ')') {
				stack.pop();
			}

			if (stack.size() == 0) {
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
		query = _fixQuery(query);

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

					break;
				}
			}

			if (targetOperator != null) {
				break;
			}
		}

		if (targetOperator != null) {
			int x = targetOperatorIndex;
			int y = targetOperatorIndex + targetOperator.length();

			String value1 = _fixQuery(query.substring(0, x));
			String operator = _fixQuery(query.substring(x, y));
			String value2 = _fixQuery(query.substring(y));

			return new String[] {value1, operator, value2};
		}

		return null;
	}

	private static boolean _isModifiedQuery(String query) {
		if (query == null) {
			return false;
		}

		query = _fixQuery(query);

		String modifier = _getLeadingModifier(query);

		if (modifier == null) {
			return false;
		}

		query = query.substring(modifier.length());

		return _isSimpleQuery(query);
	}

	private static boolean _isSimpleQuery(String query) {
		if (query == null) {
			return false;
		}

		query = _fixQuery(query);

		Stack<Integer> stack = new Stack<>();

		for (int i = 0; i < query.length(); i++) {
			char c = query.charAt(i);

			if (c == '(') {
				stack.push(i);
			}

			if (c == ')') {
				if (stack.size() == 0) {
					return false;
				}

				stack.pop();
			}
		}

		if (stack.size() != 0) {
			return false;
		}

		String[] parameters = _getParameters(query);

		if (parameters == null) {
			return false;
		}

		return true;
	}

	private static boolean _isSubquery(String query) {
		if (!query.startsWith("(") || !query.endsWith(")")) {
			return false;
		}

		String subquery = query.substring(1, query.length() - 1);

		Stack<Integer> stack = new Stack<>();

		for (int i = 0; i < subquery.length(); i++) {
			char c = subquery.charAt(i);

			if (c == '(') {
				stack.push(i);
			}

			if (c == ')') {
				if (stack.size() < 1) {
					return false;
				}

				stack.pop();
			}
		}

		if (stack.size() == 0) {
			return true;
		}

		return false;
	}

	private final PQLEntity _pqlEntity1;
	private final PQLEntity _pqlEntity2;
	private final PQLModifier _pqlModifier;
	private final PQLOperator _pqlOperator;
	private final String _query;

}