/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.testray;

/**
 * @author Michael Hashimoto
 */
public abstract class BaseTestrayProject implements TestrayProject {

	@Override
	public TestrayServer getTestrayServer() {
		return _testrayServer;
	}

	protected BaseTestrayProject(TestrayServer testrayServer) {
		_testrayServer = testrayServer;
	}

	private final TestrayServer _testrayServer;

}