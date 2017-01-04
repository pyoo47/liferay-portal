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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * @author Leslie Wong
 */
public class Environment {

	public Environment(
			String environmentType, Hashtable<String, Object> properties,
			String batchName)
		throws Exception {

		Properties buildProperties =
			JenkinsResultsParserUtil.getBuildProperties();

		_environmentCategory = buildProperties.getProperty(
			environmentType + ".testray.label");

		List<String> environmentOptions = new ArrayList(
			Arrays.asList(
				StringUtils.split(
					(String)properties.get(environmentType + ".types"), ",")));

		for (String environmentOption : environmentOptions) {
			if (batchName.contains(environmentOption)) {
				_name = environmentOption;

				String batchComponent = getBatchComponent(
					batchName, environmentOption);

				_environmentOption = (String)properties.get(
					"env.option." + environmentType + "." + batchComponent);

				return;
			}
		}

		_name = (String)properties.get(environmentType + ".type");

		String environmentVersion = (String)properties.get(
			environmentType + "." + _name + ".version");

		Matcher matcher = majorVersionPattern.matcher(environmentVersion);

		String environmentMajorVersion;

		if (matcher.matches()) {
			environmentMajorVersion = matcher.group(1);
		}
		else {
			environmentMajorVersion = environmentVersion;
		}

		if (environmentType.equals("java.jdk")) {
			_environmentOption = (String)properties.get(
				"env.option." + environmentType + "." + _name + "." +
					environmentMajorVersion.replace(".", ""));
		}
		else {
			_environmentOption = (String)properties.get(
				"env.option." + environmentType + "." + _name +
					environmentMajorVersion.replace(".", ""));
		}
	}

	public String getEnvironmentCategory() {
		return _environmentCategory;
	}

	public String getEnvironmentOption() {
		return _environmentOption;
	}

	public String getName() {
		return _name;
	}

	protected String getBatchComponent(
		String batchName, String environmentOption) {

		int x = batchName.indexOf(environmentOption);

		int y = batchName.indexOf("-", x);

		if (y == -1) {
			y = batchName.length();
		}

		return batchName.substring(x, y);
	}

	protected final Pattern majorVersionPattern = Pattern.compile(
		"((\\d+)\\.?(\\d+?)).*");

	private final String _environmentCategory;
	private final String _environmentOption;
	private final String _name;

}