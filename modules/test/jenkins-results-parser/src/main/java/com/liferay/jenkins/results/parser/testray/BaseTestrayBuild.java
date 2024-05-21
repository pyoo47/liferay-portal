/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.testray;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Michael Hashimoto
 */
public abstract class BaseTestrayBuild implements TestrayBuild {

	@Override
	public int compareTo(TestrayBuild testrayBuild) {
		if (testrayBuild == null) {
			throw new NullPointerException("Testray build is null");
		}

		Long id = testrayBuild.getID();

		return id.compareTo(getID());
	}

	@Override
	public String getPortalBranch() {
		Matcher matcher = _portalBranchPattern.matcher(getDescription());

		if (!matcher.find()) {
			return null;
		}

		return matcher.group("portalBranch");
	}

	@Override
	public String getPortalSHA() {
		Matcher matcher = _portalSHAPattern.matcher(getDescription());

		if (!matcher.find()) {
			return null;
		}

		return matcher.group("portalSHA");
	}

	@Override
	public TestrayProject getTestrayProject() {
		return _testrayProject;
	}

	@Override
	public TestrayRoutine getTestrayRoutine() {
		return _testrayRoutine;
	}

	@Override
	public TestrayServer getTestrayServer() {
		return _testrayServer;
	}

	protected BaseTestrayBuild(TestrayRoutine testrayRoutine) {
		_testrayRoutine = testrayRoutine;

		_testrayProject = testrayRoutine.getTestrayProject();
		_testrayServer = testrayRoutine.getTestrayServer();
	}

	private static final Pattern _portalBranchPattern = Pattern.compile(
		"Portal Branch: (?<portalBranch>[^;]+);");
	private static final Pattern _portalSHAPattern = Pattern.compile(
		"Portal SHA: (?<portalSHA>[^;]+);");

	private final TestrayProject _testrayProject;
	private final TestrayRoutine _testrayRoutine;
	private final TestrayServer _testrayServer;

}