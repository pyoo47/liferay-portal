/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.testray;

import java.io.File;

import java.util.List;

/**
 * @author Michael Hashimoto
 */
public class SaaSTestrayRun extends BaseTestrayRun {

	public SaaSTestrayRun(
		TestrayBuild testrayBuild, String batchName,
		List<File> propertiesFiles) {

		super(testrayBuild, batchName, propertiesFiles);
	}

	@Override
	public String getRunID() {
		return null;
	}

}