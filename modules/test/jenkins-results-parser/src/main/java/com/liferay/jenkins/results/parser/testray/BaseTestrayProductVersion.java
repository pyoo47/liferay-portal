/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.testray;

/**
 * @author Michael Hashimoto
 */
public abstract class BaseTestrayProductVersion
	implements TestrayProductVersion {

	@Override
	public TestrayProject getTestrayProject() {
		return _testrayProject;
	}

	@Override
	public TestrayServer getTestrayServer() {
		return _testrayServer;
	}

	protected BaseTestrayProductVersion(TestrayProject testrayProject) {
		_testrayProject = testrayProject;

		_testrayServer = testrayProject.getTestrayServer();
	}

	private final TestrayProject _testrayProject;
	private final TestrayServer _testrayServer;

}