/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.testray;

import java.net.URL;

import org.json.JSONObject;

/**
 * @author Michael Hashimoto
 */
public class SaaSTestrayProductVersion extends BaseTestrayProductVersion {

	@Override
	public long getID() {
		return _jsonObject.getLong("id");
	}

	@Override
	public String getName() {
		return _jsonObject.getString("name");
	}

	@Override
	public URL getURL() {
		return null;
	}

	protected SaaSTestrayProductVersion(
		TestrayProject testrayProject, JSONObject jsonObject) {

		super(testrayProject);

		_jsonObject = jsonObject;
	}

	private final JSONObject _jsonObject;

}