/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.testray;

import com.liferay.jenkins.results.parser.JenkinsResultsParserUtil;

import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONObject;

/**
 * @author Michael Hashimoto
 */
public class LegacyTestrayProductVersion extends BaseTestrayProductVersion {

	@Override
	public long getID() {
		return _jsonObject.getLong("testrayProductVersionId");
	}

	@Override
	public String getName() {
		return _jsonObject.getString("name");
	}

	@Override
	public URL getURL() {
		return _url;
	}

	protected LegacyTestrayProductVersion(
		TestrayProject testrayProject, JSONObject jsonObject) {

		super(testrayProject);

		_jsonObject = jsonObject;

		TestrayServer testrayServer = testrayProject.getTestrayServer();

		String urlString = JenkinsResultsParserUtil.combine(
			String.valueOf(testrayServer.getURL()),
			"/home/-/testray/product_versions?testrayProjectId=",
			String.valueOf(testrayProject.getID()));

		try {
			_url = new URL(urlString);
		}
		catch (MalformedURLException malformedURLException) {
			throw new RuntimeException(malformedURLException);
		}
	}

	private final JSONObject _jsonObject;
	private final URL _url;

}