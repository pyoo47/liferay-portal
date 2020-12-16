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
		String branchName, String testSuite) {

		SpiraProject spiraProject = SpiraProject.getSpiraProjectByID(
			SpiraProject.getID("dxp"));

		int testSuiteReleaseID = Integer.parseInt(
			JenkinsResultsParserUtil.getProperty(
				_buildProperties,
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

		StringBuilder sb1 = new StringBuilder();

		String comparisonUpstreamSuites = JenkinsResultsParserUtil.getProperty(
			_buildProperties,
			"spira.test.case.results.comparison.upstream.suites");

		if (comparisonUpstreamSuites == null) {
			throw new RuntimeException(
				"The Property " +
					"'spira.test.case.results.comparison.upstream.suites' is " +
						"undefined.");
		}

		Map<String, Map<Integer, SpiraTestCaseRun>> comparisonTestSuiteMaps =
			new HashMap<>();

		for (String comparisonUpstreamSuite :
				comparisonUpstreamSuites.split(",")) {

			comparisonTestSuiteMaps.put(
				comparisonUpstreamSuite,
				_getSpiraTestCaseRunMapFromList(
					getLatestUpstreamSpiraTestCaseRuns(
						branchName, comparisonUpstreamSuite)));
		}

		Map<Integer, SpiraTestCaseRun> spiraTestCaseRuns =
			_getSpiraTestCaseRunMapFromList(
				getLatestUpstreamSpiraTestCaseRuns(branchName, testSuite));

		for (Map.Entry<Integer, SpiraTestCaseRun> entry :
				spiraTestCaseRuns.entrySet()) {

			boolean inconsistent = false;

			StringBuilder sb2 = new StringBuilder();

			SpiraTestCaseRun spiraTestCaseRun = entry.getValue();

			sb2.append(spiraTestCaseRun.getName());

			sb2.append("\n");
			sb2.append(testSuite);
			sb2.append("\n<");
			sb2.append(spiraTestCaseRun.getURL());
			sb2.append("|");

			int executionStatusId = (int)spiraTestCaseRun.getProperty(
				"ExecutionStatusId");

			sb2.append(
				SpiraTestCaseRun.Status.getStatusName(executionStatusId));

			sb2.append(">\n");

			int testCaseID = entry.getKey();

			for (Map.Entry<String, Map<Integer, SpiraTestCaseRun>>
					comparisonEntry : comparisonTestSuiteMaps.entrySet()) {

				Map<Integer, SpiraTestCaseRun> comparisonSpiraTestCaseRuns =
					comparisonEntry.getValue();

				if (!comparisonSpiraTestCaseRuns.containsKey(testCaseID)) {
					sb2.append(comparisonEntry.getKey());
					sb2.append("\nN/A\n");

					inconsistent = true;

					continue;
				}

				sb2.append(comparisonEntry.getKey());
				sb2.append("\n<");

				SpiraTestCaseRun comparisonSpiraTestCaseRun =
					comparisonSpiraTestCaseRuns.get(testCaseID);

				sb2.append(comparisonSpiraTestCaseRun.getURL());

				sb2.append("|");

				int comparisonExecutionStatusId =
					(int)comparisonSpiraTestCaseRun.getProperty(
						"ExecutionStatusId");

				if (comparisonExecutionStatusId != executionStatusId) {
					inconsistent = true;
				}

				sb2.append(
					SpiraTestCaseRun.Status.getStatusName(
						comparisonExecutionStatusId));
				sb2.append(">\n");
			}

			if (inconsistent) {
				sb1.append(sb2.toString());
				sb1.append("\n");
			}
		}

		if (sb1.length() == 0) {
			sb1.append("There are no inconsistent test results in the latest ");
			sb1.append(branchName);
			sb1.append(" upstream test run for the test suite '");
			sb1.append(testSuite);
			sb1.append("' compared to the following upstream test suite(s):\n");

			for (String comparisonUpstreamSuite :
					comparisonUpstreamSuites.split(",")) {

				sb1.append(comparisonUpstreamSuite);
				sb1.append("\n");
			}
		}

		return sb1.toString();
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