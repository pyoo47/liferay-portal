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

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author Michael Hashimoto
 */
public abstract class BaseHost implements Host {

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public boolean hasChrome() {
		return _serviceNames.contains("chrome");
	}

	@Override
	public boolean hasDB2() {
		return _serviceNames.contains("db2");
	}

	@Override
	public boolean hasFirefox() {
		return _serviceNames.contains("firefox");
	}

	@Override
	public boolean hasMariaDB() {
		return _serviceNames.contains("mariadb");
	}

	@Override
	public boolean hasMySQL55() {
		return _serviceNames.contains("mysql-55");
	}

	@Override
	public boolean hasMySQL56() {
		return _serviceNames.contains("mysql-rpm");
	}

	@Override
	public boolean hasMySQL57() {
		return _serviceNames.contains("mysql-57");
	}

	@Override
	public boolean hasOracle() {
		return _serviceNames.contains("oracledb");
	}

	@Override
	public boolean hasPostgreSQL() {
		return _serviceNames.contains("postgresql");
	}

	@Override
	public boolean hasSybase() {
		return _serviceNames.contains("sybase");
	}

	protected BaseHost(String name) {
		_name = name;

		try {
			Properties jenkinsProperties =
				JenkinsResultsParserUtil.getJenkinsProperties();

			if (!jenkinsProperties.containsKey(name)) {
				return;
			}

			String serviceNames = jenkinsProperties.getProperty(name);

			Collections.addAll(_serviceNames, serviceNames.split(","));
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	private final String _name;
	private final List<String> _serviceNames = new ArrayList<>();

}