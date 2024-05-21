/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.testray;

import com.liferay.jenkins.results.parser.TopLevelBuildReport;

import java.net.URL;

import java.util.List;

import org.json.JSONObject;

/**
 * @author Michael Hashimoto
 */
public interface TestrayBuild extends Comparable<TestrayBuild> {

	public String getDescription();

	public long getID();

	public String getName();

	public String getPortalBranch();

	public String getPortalSHA();

	public JSONObject getRunsJSONObject();

	public String getStartYearMonth();

	public List<TestrayCaseResult> getTestrayCaseResults();

	public List<TestrayCaseResult> getTestrayCaseResults(
		TestrayCaseType testrayCaseType, TestrayRun testrayRun);

	public TestrayProductVersion getTestrayProductVersion();

	public TestrayProject getTestrayProject();

	public TestrayRoutine getTestrayRoutine();

	public TestrayServer getTestrayServer();

	public TopLevelBuildReport getTopLevelBuildReport();

	public URL getTopLevelBuildReportURL();

	public URL getTopLevelBuildURL();

	public TestrayCaseResult getTopLevelTestrayCaseResult();

	public URL getURL();

}