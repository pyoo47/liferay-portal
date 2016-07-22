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

import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Michael Hashimoto
 */
public class PQLConditionalFactory implements PQLQueryEntityFactory {

	public static PQLConditionalFactory getInstance() {
		return _instance;
	}

	public static Pattern getPattern() {
		return _pattern;
	}

	public PQLQueryEntity build(String query, Properties properties)
		throws Exception {

		Matcher matcher = _pattern.matcher(query);

		matcher.find();

		return new PQLConditional(matcher.group(), properties);
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

	private static final PQLConditionalFactory _instance =
		new PQLConditionalFactory();

	private static final Pattern _pattern;

	static {
		List<String> operators = PQLOperatorFactory.getOperators();

		StringBuilder sb = new StringBuilder();

		sb.append("([\\w\\.]+)\\s*(");
		sb.append(ListUtil.toString(operators, "|"));
		sb.append(")\\s*(\"(.*?)\"|[\\w]+)");

		_pattern = Pattern.compile(sb.toString());
	}

}