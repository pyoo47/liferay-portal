/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.testray;

/**
 * @author Michael Hashimoto
 */
public abstract class BaseTestrayCase implements TestrayCase {

	public BaseTestrayCase(TestrayProject testrayProject) {
		_testrayProject = testrayProject;
	}

	@Override
	public TestrayProject getTestrayProject() {
		return _testrayProject;
	}

	private final TestrayProject _testrayProject;

}