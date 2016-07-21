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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Michael Hashimoto
 */
public class PQLQueryFactory implements PQLFactory {

	public PQLQueryEntity _build(String query, Properties properties)
		throws Exception {

		return new PQLQuery(query, properties);
	}

	public int getStart(String query) {
		Matcher matcher = _pattern.matcher(query);

		if (matcher.find()) {
			return matcher.start();
		}

		return -1;
	}

	public static PQLQuery build(String query, Properties properties)
		throws Exception {

		return new PQLQuery(query, properties);
	}

	public static PQLQueryFactory getInstance() {
		return _instance;
	}

	private static final PQLQueryFactory _instance = new PQLQueryFactory();
	private static final Pattern _pattern = Pattern.compile("\\((.*?)\\)+");

}