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
import java.util.Stack;

/**
 * @author Michael Hashimoto
 */
public class PQLSubqueryFactory implements PQLQueryEntityFactory {

	public static PQLSubqueryFactory getInstance() {
		return _instance;
	}

	public PQLQueryEntity build(String query, Properties properties)
		throws Exception {

		int start = getStart(query);
		int end = getEnd(query);

		if (start == -1 || end == -1) {
			throw new Exception("Invalid subquery!");
		}

		String subquery = query.substring(start, end);

		if (subquery.startsWith("(") && subquery.endsWith(")")) {
			subquery = subquery.substring(1, subquery.length() - 1);
		}

		return new PQLSubquery(subquery, properties);
	}

	public int getEnd(String query) {
		int end = -1;

		Stack stack = new Stack();

		for (int i = 0; i < query.length(); i++) {
			char c = query.charAt(i);

			if (c == '(') {
				stack.push(i);
			}

			if (c == ')') {
				if (stack.size() == 0) {
					return -1;
				}

				if (stack.size() == 1) {
					end = i + 1;

					break;
				}

				stack.pop();
			}
		}

		return end;
	}

	public int getStart(String query) {
		int end = getEnd(query);

		if (end == -1) {
			return -1;
		}

		return query.indexOf("(");
	}

	public String removeFromQuery(String query) {
		int end = getEnd(query);

		return query.substring(end);
	}

	private static final PQLSubqueryFactory _instance =
		new PQLSubqueryFactory();

}