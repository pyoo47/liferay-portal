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
public class PQLConditionalFactory {

	public static PQLConditional build(
			String conditional, Properties properties)
		throws Exception {

		return new PQLConditional(conditional, properties);
	}

	public static Pattern getPattern() {
		return _conditionalPattern;
	}

	public static boolean isValidConditional(String conditional) {
		Matcher conditionalMatcher = _conditionalPattern.matcher(conditional);

		return conditionalMatcher.find();
	}

	private static final Pattern _conditionalPattern;

	static {
		List<String> operators = PQLOperatorFactory.getOperators();

		StringBuilder sb = new StringBuilder();

		sb.append("([\\w\\.]+)\\s*(");
		sb.append(ListUtil.toString(operators, "|"));
		sb.append(")\\s*(\"(.*?)\"|[\\w]+)");

		_conditionalPattern = Pattern.compile(sb.toString());
	}

}