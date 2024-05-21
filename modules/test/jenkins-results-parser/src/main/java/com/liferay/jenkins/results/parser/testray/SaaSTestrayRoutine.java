/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.testray;

import java.net.URL;

import java.util.Date;
import java.util.List;

import org.json.JSONObject;

/**
 * @author Michael Hashimoto
 */
public class SaaSTestrayRoutine extends BaseTestrayRoutine {

	public SaaSTestrayRoutine(
		TestrayProject testrayProject, JSONObject jsonObject) {

		super(testrayProject);

		_jsonObject = jsonObject;
	}

	public SaaSTestrayRoutine(URL testrayRoutineURL) {
		super(testrayRoutineURL);
	}

	@Override
	public TestrayBuild createTestrayBuild(
		TestrayProductVersion testrayProductVersion, String buildName) {

		return createTestrayBuild(
			testrayProductVersion, buildName, null, null, null);
	}

	@Override
	public TestrayBuild createTestrayBuild(
		TestrayProductVersion testrayProductVersion, String buildName,
		Date buildDate, String buildDescription, String buildSHA) {

		return null;
	}

	@Override
	public long getID() {
		return _jsonObject.getLong("id");
	}

	@Override
	public String getName() {
		return _jsonObject.getString("name");
	}

	@Override
	public TestrayBuild getTestrayBuildByID(long buildID) {
		return null;
	}

	@Override
	public TestrayBuild getTestrayBuildByName(
		String buildName, String... names) {

		return null;
	}

	@Override
	public List<TestrayBuild> getTestrayBuilds() {
		return null;
	}

	@Override
	public List<TestrayBuild> getTestrayBuilds(
		int maxSize, String... nameFilters) {

		return null;
	}

	private JSONObject _jsonObject;

}