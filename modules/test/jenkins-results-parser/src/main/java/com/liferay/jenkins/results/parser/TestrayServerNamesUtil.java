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

package com.liferay.jenkins.results.parser;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Kevin Yen
 */
public class TestrayServerNamesUtil {

	public static String getTestrayServerNames(
		Hashtable<String, Object> antProperties, String key, String parameter) {

		Map<String, String> properties = _getTestrayServerNamesProperties(
			antProperties);

		Map<String, String> defaultProperties = _getDefaultProperties(
			properties);

		Map<String, String> matchingProperties = _getMatchingProperties(
			properties, key);

		String value = _findMatchingProperty(matchingProperties, parameter);

		if (value != null) {
			return value;
		}

		value = _findMatchingProperty(defaultProperties, key);

		if (value != null) {
			return value;
		}

		return "";
	}

	private static String _findMatchingProperty(
		Map<String, String> properties, String parameter) {

		for (Map.Entry<String, String> entry : properties.entrySet()) {
			if (parameter.startsWith(entry.getKey())) {
				return entry.getValue();
			}
		}

		return null;
	}

	private static Map<String, String> _getDefaultProperties(
		Map<String, String> testrayServerNamesProperties) {

		Map<String, String> defaultProperties = new HashMap<>();

		for (Map.Entry<String, String> entry :
				testrayServerNamesProperties.entrySet()) {

			if (!entry.getKey().contains("/")) {
				defaultProperties.put(entry.getKey(), entry.getValue());
			}
		}

		return defaultProperties;
	}

	private static Map<String, String> _getMatchingProperties(
		Map<String, String> properties, String key) {

		Map<String, String> matchingProperties = new HashMap<>();

		for (Map.Entry<String, String> entry : properties.entrySet()) {
			if (entry.getKey().contains("/")) {
				String[] splitExpressions = entry.getKey().split("/");

				if (key.startsWith(splitExpressions[0])) {
					matchingProperties.put(
						splitExpressions[1], entry.getValue());
				}
			}
		}

		return matchingProperties;
	}

	private static Map<String, String> _getTestrayServerNamesProperties(
		Hashtable<String, Object> antProperties) {

		Map<String, String> properties = new HashMap<>();

		for (Map.Entry<String, Object> entry : antProperties.entrySet()) {
			Matcher matcher = _testrayServerNamesPattern.matcher(
				entry.getKey());

			if (matcher.matches()) {
				properties.put(
					matcher.group("parameter"), entry.getValue().toString());
			}
		}

		return properties;
	}

	private static final Pattern _testrayServerNamesPattern;

	static {
		_testrayServerNamesPattern = Pattern.compile(
			"testray\\.server\\.names\\[(?<parameter>.*)]");
	}

}