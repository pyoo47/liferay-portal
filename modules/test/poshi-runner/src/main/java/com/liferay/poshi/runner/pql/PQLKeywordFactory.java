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
import java.util.regex.Pattern;

/**
 * @author Michael Hashimoto
 */
public class PQLKeywordFactory {

	public static PQLKeyword build(String keyword) throws Exception {
		PQLKeyword pqlKeyword = null;

		if (keyword.equals(_AND)) {
			pqlKeyword = new PQLKeywordAnd();
		}
		else if (keyword.equals(_OR)) {
			pqlKeyword = new PQLKeywordOr();
		}
		else {
			throw new Exception("Invalid keyword!");
		}

		return pqlKeyword;
	}

	public static List<String> getKeywords() {
		return _keywords;
	}

	public static Pattern getPattern() {
		return _keywordPattern;
	}

	public static boolean isValidKeyword(String keyword) {
		return _keywords.contains(keyword);
	}

	private static final String _AND = "AND";

	private static final String _OR = "OR";

	private static final Pattern _keywordPattern;
	private static final List<String> _keywords = new ArrayList<>();

	static {
		_keywords.add(_AND);
		_keywords.add(_OR);

		StringBuilder sb = new StringBuilder();

		sb.append("\\s*(");
		sb.append(ListUtil.toString(_keywords, "|"));
		sb.append(")\\s*");

		_keywordPattern = Pattern.compile(sb.toString());
	}

}