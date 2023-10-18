/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.metrics;

import java.util.Map;

/**
 * @author Kenji Heigel
 */
public class TestSuiteJobHistory extends BaseJobHistory {

	public static String getTestSuiteName(
		BuildDataJSONObject buildDataJSONObject) {

		Map<String, String> parameters = buildDataJSONObject.getParameters();

		if (parameters.containsKey("CI_TEST_SUITE")) {
			return parameters.get("CI_TEST_SUITE");
		}

		return null;
	}

	public TestSuiteJobHistory(
		String jobName, String testSuiteName, long startTime, long duration) {

		super(jobName + "#" + testSuiteName, startTime, duration);

		_jobName = jobName;
		_testSuiteName = testSuiteName;
	}

	public String getJobName() {
		return _jobName;
	}

	public String getTestSuiteName() {
		return _testSuiteName;
	}

	@Override
	public void initTable() {
		setTable(new TestSuiteTable());
	}

	protected class TestSuiteTable extends BaseTable {

		@Override
		public String getFirstColumnEntryName() {
			return getTestSuiteName();
		}

		@Override
		public String getFirstColumnHeader() {
			return "Test Suite";
		}

	}

	private final String _jobName;
	private final String _testSuiteName;

}