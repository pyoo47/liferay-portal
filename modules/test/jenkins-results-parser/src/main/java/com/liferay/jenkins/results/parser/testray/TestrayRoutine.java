/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.testray;

import java.net.URL;

import java.util.Date;
import java.util.List;

/**
 * @author Michael Hashimoto
 */
public interface TestrayRoutine {

	public TestrayBuild createTestrayBuild(
		TestrayProductVersion testrayProductVersion, String buildName);

	public TestrayBuild createTestrayBuild(
		TestrayProductVersion testrayProductVersion, String buildName,
		Date buildDate, String buildDescription, String buildSHA);

	public long getID();

	public String getName();

	public TestrayBuild getTestrayBuildByID(long buildID);

	public TestrayBuild getTestrayBuildByName(
		String buildName, String... names);

	public List<TestrayBuild> getTestrayBuilds();

	public List<TestrayBuild> getTestrayBuilds(
		int maxSize, String... nameFilters);

	public TestrayProject getTestrayProject();

	public TestrayServer getTestrayServer();

	public URL getURL();

}