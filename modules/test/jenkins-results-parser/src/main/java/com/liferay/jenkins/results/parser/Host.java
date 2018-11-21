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

/**
 * @author Michael Hashimoto
 */
public interface Host {

	public String getName();

	public boolean hasChrome();

	public boolean hasDB2();

	public boolean hasFirefox();

	public boolean hasMariaDB();

	public boolean hasMySQL55();

	public boolean hasMySQL56();

	public boolean hasMySQL57();

	public boolean hasOracle();

	public boolean hasPostgreSQL();

	public boolean hasSybase();

}