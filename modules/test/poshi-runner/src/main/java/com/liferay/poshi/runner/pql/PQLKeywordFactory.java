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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Michael Hashimoto
 */
public class PQLKeywordFactory implements PQLQueryEntityFactory {

	public static PQLKeywordFactory getInstance() {
		return _instance;
	}

	public PQLQueryEntity build(String query, Properties properties)
		throws Exception {

		Matcher matcher = _pattern.matcher(query);

		matcher.find();

		String keyword = matcher.group(1);

		if (keyword.equals(_AND)) {
			return new PQLKeywordAnd();
		}
		else if (keyword.equals(_NOT)) {
			return new PQLKeywordNot();
		}
		else if (keyword.equals(_OR)) {
			return new PQLKeywordOr();
		}

		throw new Exception("Invalid keyword!");
	}

	public int getEnd(String query) {
		Matcher matcher = _pattern.matcher(query);

		if (matcher.find()) {
			return matcher.end();
		}

		return -1;
	}

	public int getStart(String query) {
		Matcher matcher = _pattern.matcher(query);

		if (matcher.find()) {
			return matcher.start();
		}

		return -1;
	}

	public String removeFromQuery(String query) {
		Matcher matcher = _pattern.matcher(query);

		matcher.find();

		return query.substring(matcher.end());
	}

	private static final String _AND = "AND";

	private static final String _NOT = "NOT";

	private static final String _OR = "OR";

	private static final PQLKeywordFactory _instance = new PQLKeywordFactory();

	private static final Pattern _pattern;

	static {
		List<String> keywords = new ArrayList<>();

		keywords.add(_AND);
		keywords.add(_NOT);
		keywords.add(_OR);

		StringBuilder sb = new StringBuilder();

		sb.append("\\s*(");
		sb.append(ListUtil.toString(keywords, "|"));
		sb.append(")\\s*");

		_pattern = Pattern.compile(sb.toString());
	}

}