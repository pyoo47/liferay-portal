/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.testray;

import com.liferay.jenkins.results.parser.TopLevelBuildReport;

import java.net.URL;

import java.util.List;

import org.json.JSONObject;

/**
 * @author Michael Hashimoto
 */
public class SaaSTestrayBuild extends BaseTestrayBuild {

	@Override
	public String getDescription() {
		return _jsonObject.optString("description");
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
	public JSONObject getRunsJSONObject() {
		return null;
	}

	@Override
	public String getStartYearMonth() {
		return null;
	}

	@Override
	public List<TestrayCaseResult> getTestrayCaseResults() {
		return null;
	}

	@Override
	public List<TestrayCaseResult> getTestrayCaseResults(
		TestrayCaseType testrayCaseType, TestrayRun testrayRun) {

		return null;
	}

	@Override
	public TestrayProductVersion getTestrayProductVersion() {
		return null;
	}

	@Override
	public TopLevelBuildReport getTopLevelBuildReport() {
		return null;
	}

	@Override
	public URL getTopLevelBuildReportURL() {
		return null;
	}

	@Override
	public URL getTopLevelBuildURL() {
		return null;
	}

	@Override
	public TestrayCaseResult getTopLevelTestrayCaseResult() {
		return null;
	}

	@Override
	public URL getURL() {
		return null;
	}

	protected SaaSTestrayBuild(
		TestrayRoutine testrayRoutine, JSONObject jsonObject) {

		super(testrayRoutine);

		_jsonObject = jsonObject;
	}

	private final JSONObject _jsonObject;

}