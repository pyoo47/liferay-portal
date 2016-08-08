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

/**
 * @author Michael Hashimoto
 */
public abstract class PQLEntity {

	public static String fixQuery(String query) {
		while (true) {
			query = query.trim();

			if (!query.startsWith("(") || !query.endsWith(")")) {
				break;
			}

			String subquery = query.substring(1, query.length() - 1);

			int parenthesisCount = 0;

			for (int i = 0; i < subquery.length(); i++) {
				char c = subquery.charAt(i);

				if (c == '(') {
					parenthesisCount++;
				}

				if (c == ')') {
					if (parenthesisCount < 1) {
						return query.trim();
					}

					parenthesisCount--;
				}
			}

			if (parenthesisCount > 0) {
				return query.trim();
			}

			query = subquery;
		}

		return query.trim();
	}

	public static String removeModifierFromQuery(String query) {
		query = fixQuery(query);

		String modifier = _getModifierFromQuery(query);

		if (modifier != null) {
			query = query.substring(modifier.length());
		}

		return query.trim();
	}

	public PQLEntity(String query) throws Exception {
		_query = query;

		if (query != null) {
			query = fixQuery(query);

			_setModifierFromQuery(query);

			query = removeModifierFromQuery(query);
		}

		_fixedQuery = query;
	}

	public PQLModifier getPQLModifier() {
		return _pqlModifier;
	}

	public abstract Object getValue(Properties properties) throws Exception;

	protected String getFixedQuery() throws Exception {
		return _fixedQuery;
	}

	protected String getQuery() {
		return _query;
	}

	private static String _getModifierFromQuery(String query) {
		query = fixQuery(query);

		Set<String> availableModifiers = PQLModifier.getAvailableModifiers();

		for (String modifier : availableModifiers) {
			if (query.startsWith(modifier)) {
				return modifier;
			}
		}

		return null;
	}

	private void _setModifierFromQuery(String query) throws Exception {
		query = fixQuery(query);

		String modifier = _getModifierFromQuery(query);

		if (modifier != null) {
			_pqlModifier = PQLModifierFactory.newInstance(modifier);
		}
		else {
			_pqlModifier = null;
		}
	}

	private final String _fixedQuery;
	private PQLModifier _pqlModifier;
	private final String _query;

}