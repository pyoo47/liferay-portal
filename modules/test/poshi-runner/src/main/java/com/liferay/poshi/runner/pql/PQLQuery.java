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

import com.liferay.poshi.runner.util.ListUtil;

import java.util.ArrayList;
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

		String[] queryTokens = _getQueryTokens(query);

		if (queryTokens == null) {
			return false;
		}

		return true;
	}

	public PQLQuery(String query) throws Exception {
		super(query);

		_validateQuery(query);

		String[] queryTokens = _getQueryTokens(getPQL());

		String value1 = queryTokens[0];
		String operator = queryTokens[1];
		String value2 = queryTokens[2];

		_pqlEntity1 = PQLEntityFactory.newEntity(value1);
		_pqlOperator = PQLOperatorFactory.newOperator(operator);
		_pqlEntity2 = PQLEntityFactory.newEntity(value2);
	}

	public Object getValue(Properties properties) throws Exception {
		Object objectValue = _pqlOperator.getValue(
			_pqlEntity1, _pqlEntity2, properties);

		if (!(objectValue instanceof Boolean)) {
			throw new Exception("Unable to evaluate " + getPQL());
		}

		PQLModifier pqlModifier = getPQLModifier();

		if (pqlModifier != null) {
			objectValue = pqlModifier.modify(objectValue);
		}

		return objectValue;
	}

	private static List<String> _getAllTokens(String query) {
		List<String> tokens = new ArrayList<>();

		query = fixPQL(query);

		while (true) {
			if (query.startsWith("(") && query.contains(")")) {
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
						return null;
					}

					if (parenthesisCount == 0) {
						int x = i + 1;

						String token = query.substring(0, x);

						tokens.add(token.trim());

						query = query.substring(x);

						query = query.trim();

						break;
					}
				}

				if (parenthesisCount > 0) {
					return null;
				}
			}
			else if (_startsWithReservedToken(query)) {
				String reservedToken = _getStartingReservedToken(query);

				tokens.add(reservedToken);

				query = query.substring(reservedToken.length());

				query = query.trim();
			}
			else if (query.startsWith("\"") || query.startsWith("'")) {
				int quotationTokenEndIndex = _getQuotationTokenEndIndex(query);

				if (quotationTokenEndIndex == -1) {
					return null;
				}

				String token = query.substring(0, quotationTokenEndIndex);

				tokens.add(token.trim());

				query = query.substring(quotationTokenEndIndex);

				query = query.trim();
			}
			else if (!query.equals("")) {
				if (query.contains(" ")) {
					int x = query.indexOf(" ");

					String token = query.substring(0, x);

					tokens.add(token.trim());

					query = query.substring(x);

					query = query.trim();
				}
				else {
					tokens.add(query.trim());

					query = "";
				}
			}
			else {
				break;
			}
		}

		return tokens;
	}

	private static String[] _getQueryTokens(String query) {
		List<String> tokens = _getAllTokens(query);

		if (tokens == null) {
			return null;
		}

		List<List<String>> prioritizedOperatorList =
			PQLOperator.getPrioritizedOperatorList();

		int x = -1;

		for (int i = (prioritizedOperatorList.size() - 1); i >= 0; i--) {
			List<String> operators = prioritizedOperatorList.get(i);

			for (int j = 0; j < tokens.size(); j++) {
				String token = tokens.get(j);

				if (operators.contains(token)) {
					x = j;

					break;
				}
			}

			if (x != -1) {
				break;
			}
		}

		if (x != -1) {
			String entity1 = ListUtil.toString(tokens.subList(0, x), null, " ");
			String operator = tokens.get(x);
			String entity2 = ListUtil.toString(
				tokens.subList(x + 1, tokens.size()), null, " ");

			if (entity1.equals("") || entity2.equals("")) {
				return null;
			}

			return new String[] {entity1, operator, entity2};
		}

		return null;
	}

	private static int _getQuotationTokenEndIndex(String query) {
		boolean escapeNextChar = false;

		char quotation = query.charAt(0);

		for (int i = 1; i < query.length(); i++) {
			char c = query.charAt(i);

			if (escapeNextChar) {
				escapeNextChar = false;

				continue;
			}

			if (c == '\\') {
				escapeNextChar = true;

				continue;
			}

			if (c == quotation) {
				return i + 1;
			}
		}

		return -1;
	}

	private static String _getStartingReservedToken(String query) {
		List<String> reservedTokens = new ArrayList<>();

		reservedTokens.addAll(PQLModifier.getAvailableModifiers());
		reservedTokens.addAll(PQLOperator.getAvailableOperators());

		for (String reservedToken : reservedTokens) {
			if (query.equals(reservedToken) ||
				query.startsWith(reservedToken + " ")) {

				return reservedToken;
			}
		}

		return null;
	}

	private static boolean _startsWithReservedToken(String query) {
		String startingReservedToken = _getStartingReservedToken(query);

		if (startingReservedToken != null) {
			return true;
		}

		return false;
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