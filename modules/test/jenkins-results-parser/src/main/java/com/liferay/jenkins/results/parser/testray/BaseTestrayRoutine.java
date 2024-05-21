/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.testray;

import java.net.URL;

/**
 * @author Michael Hashimoto
 */
public abstract class BaseTestrayRoutine implements TestrayRoutine {

	@Override
	public TestrayProject getTestrayProject() {
		return _testrayProject;
	}

	@Override
	public TestrayServer getTestrayServer() {
		return _testrayServer;
	}

	@Override
	public URL getURL() {
		return _url;
	}

	protected BaseTestrayRoutine(TestrayProject testrayProject) {
		_testrayProject = testrayProject;

		_testrayServer = testrayProject.getTestrayServer();
	}

	protected BaseTestrayRoutine(URL url) {
		_url = url;
	}

	protected void setTestrayProject(TestrayProject testrayProject) {
		_testrayProject = testrayProject;
	}

	protected void setTestrayServer(TestrayServer testrayServer) {
		_testrayServer = testrayServer;
	}

	private TestrayProject _testrayProject;
	private TestrayServer _testrayServer;
	private URL _url;

}