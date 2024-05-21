/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.testray;

import java.util.List;

/**
 * @author Michael Hashimoto
 */
public class SaaSTestrayServer extends BaseTestrayServer {

	public SaaSTestrayServer(String urlString) {
		super(urlString);
	}

	@Override
	public TestrayCaseType getTestrayCaseType(String testrayCaseTypeName) {
		return null;
	}

	@Override
	public TestrayProject getTestrayProjectByID(long projectID) {
		return null;
	}

	@Override
	public TestrayProject getTestrayProjectByName(String projectName) {
		return null;
	}

	@Override
	public List<TestrayProject> getTestrayProjects() {
		return null;
	}

}