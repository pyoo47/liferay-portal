/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.jenkins.results.parser;

import com.liferay.jenkins.results.parser.spira.SearchQuery;
import com.liferay.jenkins.results.parser.spira.SpiraProject;
import com.liferay.jenkins.results.parser.spira.SpiraRelease;
import com.liferay.jenkins.results.parser.spira.SpiraReleaseBuild;
import com.liferay.jenkins.results.parser.spira.SpiraTestCaseRun;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Yi-Chen Tsai
 */
public class SpiraTestCaseResultsUtil {

	public static List<SpiraTestCaseRun> getLatestUpstreamSpiraTestCaseRuns(
			String branchName, String testSuite)
		throws IOException {

		SpiraProject spiraProject = SpiraProject.getSpiraProjectByID(
			SpiraProject.getID("dxp"));

		int testSuiteReleaseID = Integer.parseInt(
			JenkinsResultsParserUtil.getBuildProperty(
				"spira.release.id[test-portal-testsuite-upstream(" +
					branchName + ")][" + testSuite + "]"));

		SpiraRelease spiraRelease = spiraProject.getSpiraReleaseByID(
			testSuiteReleaseID);

		SearchQuery.SearchParameter searchParameter =
			new SearchQuery.SearchParameter(
				SpiraRelease.getKeyID(SpiraRelease.class), testSuiteReleaseID);

		List<SpiraReleaseBuild> spiraReleaseBuilds =
			SpiraReleaseBuild.getSpiraReleaseBuilds(
				spiraProject, spiraRelease, searchParameter);

		if (spiraReleaseBuilds.isEmpty()) {
			return new ArrayList<>();
		}

		SpiraReleaseBuild spiraReleaseBuild = spiraReleaseBuilds.get(0);

		return spiraReleaseBuild.getSpiraTestCaseRuns(50000);
	}

	private String _getLatestUpstreamComparisonMessage(
		String branchName, String testSuite) {

		String message = "";

		StringBuilder sb = new StringBuilder();

		String comparisonUpstreamSuites = JenkinsResultsParserUtil.getProperty(
			_buildProperties,
			"spira.test.case.results.comparison.upstream.suites");

		int testSuiteReleaseID = Integer.parseInt(
			JenkinsResultsParserUtil.getProperty(
				_buildProperties,
				"spira.release.id[test-portal-testsuite-upstream(" +
					branchName + ")][" + testSuite + "]"));

		return message;
	}

	private Map<Integer, SpiraTestCaseRun> _getSpiraTestCaseRunMapFromList(
		List<SpiraTestCaseRun> spiraTestCaseRuns) {

		Map<Integer, SpiraTestCaseRun> spiraTestCaseRunMap = new HashMap<>();

		for (SpiraTestCaseRun spiraTestCaseRun : spiraTestCaseRuns) {
			spiraTestCaseRunMap.put(
				(int)spiraTestCaseRun.getProperty("TestCaseId"),
				spiraTestCaseRun);
		}

		return spiraTestCaseRunMap;
	}

	private static final Properties _buildProperties;

	static {
		try {
			_buildProperties = JenkinsResultsParserUtil.getBuildProperties();
		}
		catch (IOException ioException) {
			throw new RuntimeException(
				"Unable to get build.properties", ioException);
		}
	}

}