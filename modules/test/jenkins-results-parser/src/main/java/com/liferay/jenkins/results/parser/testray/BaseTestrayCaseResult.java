/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.testray;

import com.liferay.jenkins.results.parser.JenkinsResultsParserUtil;
import com.liferay.jenkins.results.parser.TopLevelBuild;

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Michael Hashimoto
 */
public class BaseTestrayCaseResult implements TestrayCaseResult {

	@Override
	public TestrayAttachment getBuildResultTestrayAttachment() {
		_initTestrayAttachments();

		return _testrayAttachments.get("Build Result (Top Level)");
	}

	@Override
	public String getCaseID() {
		return jsonObject.optString("testrayCaseId");
	}

	@Override
	public String getComponentName() {
		return jsonObject.getString("testrayComponentName");
	}

	@Override
	public String getErrors() {
		return jsonObject.optString("errors");
	}

	@Override
	public URL getHistoryURL() {
		try {
			return new URL(getURL() + "/history");
		}
		catch (MalformedURLException malformedURLException) {
			throw new RuntimeException(malformedURLException);
		}
	}

	@Override
	public long getID() {
		return jsonObject.optLong("testrayCaseResultId");
	}

	@Override
	public JSONObject getJSONObject() {
		return jsonObject;
	}

	@Override
	public String getName() {
		return jsonObject.optString("testrayCaseName");
	}

	@Override
	public int getPriority() {
		TestrayCase testrayCase = getTestrayCase();

		return testrayCase.getPriority();
	}

	@Override
	public Status getStatus() {
		int statusID = jsonObject.optInt("status");

		return Status.get(statusID);
	}

	@Override
	public String getSubcomponentNames() {
		return "";
	}

	@Override
	public String getTeamName() {
		return jsonObject.getString("testrayTeamName");
	}

	@Override
	public List<TestrayAttachment> getTestrayAttachments() {
		_initTestrayAttachments();

		return new ArrayList<>(_testrayAttachments.values());
	}

	@Override
	public TestrayBuild getTestrayBuild() {
		return _testrayBuild;
	}

	@Override
	public TestrayCase getTestrayCase() {
		if (_testrayCase != null) {
			return _testrayCase;
		}

		TestrayServer testrayServer = getTestrayServer();

		String testrayCaseURL = JenkinsResultsParserUtil.combine(
			String.valueOf(testrayServer.getURL()), "/home/-/testray/cases/",
			getCaseID(), ".json");

		try {
			_testrayCase = new TestrayCase(
				getTestrayProject(),
				JenkinsResultsParserUtil.toJSONObject(
					testrayCaseURL, testrayServer.getHTTPAuthorization()));
		}
		catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}

		return _testrayCase;
	}

	@Override
	public TestrayProject getTestrayProject() {
		return _testrayBuild.getTestrayProject();
	}

	@Override
	public TestrayServer getTestrayServer() {
		return _testrayBuild.getTestrayServer();
	}

	@Override
	public TopLevelBuild getTopLevelBuild() {
		return _topLevelBuild;
	}

	@Override
	public String getType() {
		TestrayCase testrayCase = getTestrayCase();

		return testrayCase.getType();
	}

	@Override
	public URL getURL() {
		TestrayServer testrayServer = getTestrayServer();

		try {
			return new URL(
				testrayServer.getURL(),
				"home/-/testray/case_results/" + getID());
		}
		catch (MalformedURLException malformedURLException) {
			throw new RuntimeException(malformedURLException);
		}
	}

	@Override
	public String[] getWarnings() {
		JSONArray jsonArray = jsonObject.optJSONArray("warnings");

		if (jsonArray == null) {
			return null;
		}

		String[] warnings = new String[jsonArray.length()];

		for (int i = 0; i < warnings.length; i++) {
			warnings[i] = jsonArray.optString(i);
		}

		return warnings;
	}

	protected BaseTestrayCaseResult(
		TestrayBuild testrayBuild, JSONObject jsonObject) {

		_testrayBuild = testrayBuild;
		this.jsonObject = jsonObject;
	}

	protected BaseTestrayCaseResult(
		TestrayBuild testrayBuild, TopLevelBuild topLevelBuild) {

		_testrayBuild = testrayBuild;
		_topLevelBuild = topLevelBuild;
		jsonObject = new JSONObject();
	}

	protected final JSONObject jsonObject;

	private synchronized void _initTestrayAttachments() {
		if (_testrayAttachments != null) {
			return;
		}

		_testrayAttachments = new TreeMap<>();

		JSONObject attachmentsJSONObject = jsonObject.optJSONObject(
			"attachments");

		for (String name : attachmentsJSONObject.keySet()) {
			TestrayAttachment testrayAttachment =
				TestrayFactory.newTestrayAttachment(
					this, name, attachmentsJSONObject.getString(name));

			_testrayAttachments.put(
				testrayAttachment.getName(), testrayAttachment);
		}
	}

	private Map<String, TestrayAttachment> _testrayAttachments;
	private final TestrayBuild _testrayBuild;
	private TestrayCase _testrayCase;
	private TopLevelBuild _topLevelBuild;

}