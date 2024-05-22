/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.testray;

import com.liferay.jenkins.results.parser.TopLevelBuild;

import org.json.JSONObject;

/**
 * @author Michael Hashimoto
 */
public class LegacyTestrayCaseResult extends BaseTestrayCaseResult {

	protected LegacyTestrayCaseResult(
		TestrayBuild testrayBuild, JSONObject jsonObject) {

		super(testrayBuild, jsonObject);
	}

	protected LegacyTestrayCaseResult(
		TestrayBuild testrayBuild, TopLevelBuild topLevelBuild) {

		super(testrayBuild, topLevelBuild);
	}

}