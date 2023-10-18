/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.metrics;

import com.liferay.jenkins.results.parser.JenkinsResultsParserUtil;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;

/**
 * @author Kenji Heigel
 */
public class JobHistoryFactory {

	public static Set<JobHistory> newAggregateJobHistories(
		long startTime, long duration) {

		Set<JobHistory> jobHistories = new HashSet<>();

		for (JobHistory defaultJobHistory :
				newDefaultJobHistories(startTime, duration)) {

			String categoryName = AggregateJobHistory.getCategoryName(
				defaultJobHistory.getName());

			if (!_containsJobHistory(jobHistories, categoryName)) {
				jobHistories.add(
					new AggregateJobHistory(categoryName, startTime, duration));
			}

			AggregateJobHistory aggregateJobHistory =
				(AggregateJobHistory)_getJobHistory(jobHistories, categoryName);

			if (aggregateJobHistory != null) {
				aggregateJobHistory.addJobHistory(defaultJobHistory);
			}
		}

		return jobHistories;
	}

	public static Set<JobHistory> newDefaultJobHistories(
		long startTime, long duration) {

		String key = String.valueOf(startTime) + duration;

		if (_defaultJobHistoriesMap.containsKey(key)) {
			return _defaultJobHistoriesMap.get(key);
		}

		Set<JobHistory> jobHistories = new HashSet<>();

		for (String dateString :
				JenkinsResultsParserUtil.getDateStrings(startTime, duration)) {

			if (!_buildDataJSONObjectsMap.containsKey(dateString)) {
				System.out.println("Loading build JSONs for " + dateString);

				_loadBuildJSONObjects(dateString);
			}

			List<BuildDataJSONObject> buildDataJSONObjects =
				_buildDataJSONObjectsMap.get(dateString);

			for (BuildDataJSONObject buildDataJSONObject :
					buildDataJSONObjects) {

				String name = DefaultJobHistory.getName(
					buildDataJSONObject.getJobName());

				if (!_containsJobHistory(jobHistories, name)) {
					jobHistories.add(
						new DefaultJobHistory(name, startTime, duration));
				}

				JobHistory jobHistory = _getJobHistory(jobHistories, name);

				if (jobHistory != null) {
					jobHistory.addBuildDataJSONObject(buildDataJSONObject);
				}
			}
		}

		_defaultJobHistoriesMap.put(key, jobHistories);

		return jobHistories;
	}

	public static Set<JobHistory> newTestSuiteJobHistories(
		String jobName, long startTime, long duration) {

		JobHistory jobHistory = _getJobHistory(
			newDefaultJobHistories(startTime, duration), jobName);

		if (jobHistory == null) {
			return Collections.emptySet();
		}

		Set<JobHistory> jobHistories = new HashSet<>();

		Set<BuildDataJSONObject> buildDataJSONObjects =
			jobHistory.getBuildDataJSONObjects();

		for (BuildDataJSONObject buildDataJSONObject : buildDataJSONObjects) {
			String url = buildDataJSONObject.getURL();

			if (!jobHistory.containsTopLevelBuildURL(url)) {
				continue;
			}

			String testSuiteName = TestSuiteJobHistory.getTestSuiteName(
				buildDataJSONObject);

			if (testSuiteName == null) {
				continue;
			}

			String jobHistoryName = jobName + "#" + testSuiteName;

			if (!_containsJobHistory(jobHistories, jobHistoryName)) {
				jobHistories.add(
					new TestSuiteJobHistory(
						jobName, testSuiteName, startTime, duration));
			}

			TestSuiteJobHistory testSuiteJobHistory =
				(TestSuiteJobHistory)_getJobHistory(
					jobHistories, jobHistoryName);

			if (testSuiteJobHistory != null) {
				testSuiteJobHistory.addBuildDataJSONObject(buildDataJSONObject);
				testSuiteJobHistory.addTopLevelBuildURL(url);
			}
		}

		for (BuildDataJSONObject buildDataJSONObject : buildDataJSONObjects) {
			if (jobHistory.containsTopLevelBuildURL(
					buildDataJSONObject.getURL())) {

				continue;
			}

			Map<String, String> parameters =
				buildDataJSONObject.getParameters();

			if (!parameters.containsKey("DIST_PATH")) {
				continue;
			}

			Matcher distPathMatcher = _distPathPattern.matcher(
				parameters.get("DIST_PATH"));

			if (!distPathMatcher.find()) {
				continue;
			}

			String buildURL = JenkinsResultsParserUtil.combine(
				"https://", distPathMatcher.group("masterName"),
				".liferay.com/job/", distPathMatcher.group("jobName"), "/",
				distPathMatcher.group("buildNumber"), "/");

			for (JobHistory testSuiteJobHistory : jobHistories) {
				if (testSuiteJobHistory.containsTopLevelBuildURL(buildURL)) {
					buildDataJSONObject.put("topLevelBuildURL", buildURL);

					testSuiteJobHistory.addBuildDataJSONObject(
						buildDataJSONObject);

					break;
				}
			}
		}

		return jobHistories;
	}

	private static boolean _containsJobHistory(
		Set<JobHistory> jobHistories, String name) {

		for (JobHistory jobHistory : jobHistories) {
			if (name.equals(jobHistory.getName())) {
				return true;
			}
		}

		return false;
	}

	private static JobHistory _getJobHistory(
		Set<JobHistory> jobHistories, String name) {

		for (JobHistory jobHistory : jobHistories) {
			if (name.equals(jobHistory.getName())) {
				return jobHistory;
			}
		}

		return null;
	}

	private static void _loadBuildJSONObjects(String dateString) {
		File dateDir = new File(_BASE_DIR, dateString);

		if (!dateDir.exists()) {
			_buildDataJSONObjectsMap.put(
				dateString, new ArrayList<BuildDataJSONObject>());
		}

		if (dateDir.listFiles() == null) {
			return;
		}

		List<BuildDataJSONObject> buildJSONObjects = new ArrayList<>();

		for (File jsonFile : dateDir.listFiles()) {
			try {
				String jsonFileName = jsonFile.getCanonicalPath();

				if (jsonFileName.contains("test-1-0") ||
					jsonFileName.contains("test-1-41")) {

					continue;
				}

				String content = JenkinsResultsParserUtil.read(jsonFile);

				JSONArray jsonArray = new JSONArray(content.trim());

				for (int i = 0; i < jsonArray.length(); i++) {
					buildJSONObjects.add(
						new BuildDataJSONObject(jsonArray.getJSONObject(i)));
				}
			}
			catch (IOException ioException) {
				System.out.println("Unable to read " + jsonFile);
			}
		}

		_buildDataJSONObjectsMap.put(dateString, buildJSONObjects);
	}

	private static final File _BASE_DIR = new File(
		"/opt/dev/projects/github/liferay-jenkins-ee/tmp/jenkins");

	private static final Map<String, List<BuildDataJSONObject>>
		_buildDataJSONObjectsMap = new HashMap<>();
	private static final Map<String, Set<JobHistory>> _defaultJobHistoriesMap =
		new HashMap<>();
	private static final Pattern _distPathPattern = Pattern.compile(
		JenkinsResultsParserUtil.combine(
			"[\\w\\/]+(?<masterName>test-[\\d]+-[\\d]+)\\/",
			"(?<jobName>[\\w\\-\\(\\)]+)\\/(?<buildNumber>[\\d]+)"));

}