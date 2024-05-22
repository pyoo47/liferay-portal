/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.testray;

import org.json.JSONObject;

/**
 * @author Michael Hashimoto
 */
public class SaaSTestrayCase extends BaseTestrayCase {

	public SaaSTestrayCase(
		TestrayProject testrayProject, JSONObject jsonObject) {

		super(testrayProject);

		_jsonObject = jsonObject;
	}

	@Override
	public String getComponent() {
		return null;
	}

	@Override
	public String getID() {
		return null;
	}

	@Override
	public String getName() {
		return _jsonObject.getString("name");
	}

	@Override
	public int getPriority() {
		return -1;
	}

	@Override
	public String getTeamName() {
		return null;
	}

	@Override
	public String getType() {
		return null;
	}

	private final JSONObject _jsonObject;

}