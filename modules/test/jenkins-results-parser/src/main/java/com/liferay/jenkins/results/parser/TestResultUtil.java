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

import com.liferay.jenkins.results.parser.testray.TestrayBuild;
import com.liferay.jenkins.results.parser.testray.TestrayCaseResult;
import com.liferay.jenkins.results.parser.testray.TestrayProject;
import com.liferay.jenkins.results.parser.testray.TestrayRoutine;
import com.liferay.jenkins.results.parser.testray.TestrayServer;

import java.io.IOException;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Kenji Heigel
 */
public class TestResultUtil {

	public static void addToTestHistoryMap(
		String name, String batchName, String status, String buildURL,
		String errorSnippet) {

		String key = name + "/" + batchName;

		if (!_testHistoryMap.containsKey(key)) {
			_testHistoryMap.put(
				key,
				new TestHistory(
					name, batchName, status, buildURL, errorSnippet));

			return;
		}

		TestHistory testHistory = _testHistoryMap.get(key);

		testHistory.update(buildURL, errorSnippet, status);
	}

	public static Map<String, TestHistory> getTestHistoryMap() {
		return _testHistoryMap;
	}

	public static void loadBuildResultJSON(JSONObject jsonObject) {
		JSONArray batchResultsJSONArray = jsonObject.getJSONArray(
			"batchResults");

		for (int i = 0; i < batchResultsJSONArray.length(); i++) {
			JSONObject batchResultJSONObject =
				batchResultsJSONArray.getJSONObject(i);

			String jobVariant = batchResultJSONObject.getString("jobVariant");

			jobVariant = jobVariant.replaceAll("(.*)/.*", "$1");

			JSONArray testResultsJSONArray = batchResultJSONObject.getJSONArray(
				"testResults");

			for (int j = 0; j < testResultsJSONArray.length(); j++) {
				JSONObject testResultJSONObject =
					testResultsJSONArray.getJSONObject(j);

				String name = testResultJSONObject.optString("name");

				String status = testResultJSONObject.optString("status");

				status = status.replace("REGRESSION", "FAILED");
				status = status.replace("FIXED", "PASSED");

				if (name.startsWith("PortalLogAssertorTest") ||
					name.startsWith("JenkinsLogAsserterTest") ||
					status.equals("SKIPPED")) {

					continue;
				}

				String buildURL = testResultJSONObject.optString("buildURL");

				String errorDetails = testResultJSONObject.optString(
					"errorDetails");

				addToTestHistoryMap(
					name, jobVariant, status, buildURL, errorDetails);
			}
		}
	}

	public static void loadBuildResultJSON(URL url) {
		try {
			JSONObject jsonObject = JenkinsResultsParserUtil.toJSONObject(
				url.toString());

			loadBuildResultJSON(jsonObject);
		}
		catch (IOException ioException) {
			System.out.println("Unable to load " + url);
		}
	}

	public static void loadTestrayBuilds(
		String testrayServerName, String projectName, String routineName,
		int maxBuilds) {

		TestrayServer testrayServer = new TestrayServer(testrayServerName);

		TestrayProject testrayProject = testrayServer.getTestrayProjectByName(
			projectName);

		TestrayRoutine testrayRoutine = testrayProject.getTestrayRoutineByName(
			routineName);

		for (TestrayBuild testrayBuild :
				testrayRoutine.getTestrayBuilds(maxBuilds)) {

			for (TestrayCaseResult testrayCaseResult :
					testrayBuild.getTestrayCaseResults()) {

				String name = testrayCaseResult.getName();

				String jobVariant = null;

				List<TestrayCaseResult.Attachment> attachments =
					testrayCaseResult.getAttachments();

				if (!attachments.isEmpty()) {
					for (TestrayCaseResult.Attachment attachment :
							attachments) {

						String attachmentValue = attachment.getValue();

						Matcher matcher = _testrayLogPattern.matcher(
							attachmentValue);

						matcher.find();

						jobVariant = matcher.group("jobVariant");
					}
				}

				TestrayCaseResult.Status status = testrayCaseResult.getStatus();

				URL url = testrayCaseResult.getURL();

				addToTestHistoryMap(
					name, jobVariant, status.getName(), url.toString(),
					testrayCaseResult.getErrors());
			}
		}
	}

	public static void setFlakinessAlgorithm(String type) {
		_flakinessAlgorithm = FlakinessAlgorithm.get(type);
	}

	public static void writeFlakyTestDataJavaScriptFile(String filePath)
		throws IOException {

		String acceptanceUpstreamJobURL =
			"https://test-1-1.liferay.com/job" +
				"/test-portal-acceptance-upstream-dxp(master)/";

		List<String> buildResultJsonURLs = _getBuildResultJsonURLs(
			acceptanceUpstreamJobURL, 25);

		Map<String, JSONObject> buildResultJSONObjects =
			_getBuildResultJSONObjects(buildResultJsonURLs);

		for (String buildResultJsonURL : buildResultJsonURLs) {
			loadBuildResultJSON(buildResultJSONObjects.get(buildResultJsonURL));
		}

		JSONArray flakyTestDataJSONArray = new JSONArray();

		flakyTestDataJSONArray.put(
			new String[] {"Name", "Batch Type", "Results", "Status Changes"});

		Map<String, TestHistory> testHistoryMap = getTestHistoryMap();

		for (TestHistory testHistory : testHistoryMap.values()) {
			if (testHistory.hasFlakiness()) {
				flakyTestDataJSONArray.put(testHistory.toJSONArray());
			}
		}

		StringBuilder sb = new StringBuilder();

		sb.append("var flakyTestData = ");
		sb.append(flakyTestDataJSONArray.toString());
		sb.append(";");

		JenkinsResultsParserUtil.write(filePath, sb.toString());
	}

	public static class TestHistory {

		public TestHistory(
			String name, String batchName, String status, String buildURL,
			String errorSnippet) {

			_name = name;
			_batchName = batchName;

			_buildURLs.add(buildURL);

			_errorSnippets.add(errorSnippet);

			_statuses.add(status);
		}

		public void addBuildURL(String buildURL) {
			_buildURLs.add(buildURL);
		}

		public void addErrorSnippet(String errorSnippet) {
			_errorSnippets.add(errorSnippet);
		}

		public void addStatus(String status) {
			_statuses.add(status);
		}

		public String getBatchName() {
			return _batchName;
		}

		public String getName() {
			return _name;
		}

		public boolean hasFlakiness() {
			if (_flakinessAlgorithm == FlakinessAlgorithm.STATUS_CHANGE) {
				return hasFlakinessByStatusChangeAlgorithm();
			}

			return hasFlakinessByBasicAlgorithm();
		}

		public JSONArray toJSONArray() {
			JSONArray jsonArray = new JSONArray();

			jsonArray.put(getName());
			jsonArray.put(getBatchName());

			JSONArray statusesJSONArray = new JSONArray();

			for (int i = 0; i < _statuses.size(); i++) {
				JSONArray statusJSONArray = new JSONArray();

				statusJSONArray.put(_statuses.get(i));
				statusJSONArray.put(_buildURLs.get(i));

				statusesJSONArray.put(statusJSONArray);
			}

			jsonArray.put(statusesJSONArray);
			jsonArray.put(_statusChanges);

			return jsonArray;
		}

		public void update(
			String buildURL, String errorDetails, String status) {

			_buildURLs.add(buildURL);
			_errorSnippets.add(errorDetails);
			_statuses.add(status);
		}

		protected boolean hasFlakinessByBasicAlgorithm() {
			if (Collections.frequency(_statuses, _statuses.get(0)) <
					_statuses.size()) {

				return true;
			}

			return false;
		}

		protected boolean hasFlakinessByStatusChangeAlgorithm() {
			String lastStatus = null;

			for (String status : _statuses) {
				if (lastStatus == null) {
					lastStatus = status;

					continue;
				}

				if (!lastStatus.equals(status)) {
					lastStatus = status;

					_statusChanges++;
				}
			}

			if (_statusChanges > 1) {
				return true;
			}

			return false;
		}

		private final String _batchName;
		private final List<String> _buildURLs = new ArrayList<>();
		private final List<String> _errorSnippets = new ArrayList<>();
		private final String _name;
		private int _statusChanges;
		private final List<String> _statuses = new ArrayList<>();

	}

	public enum FlakinessAlgorithm {

		BASIC("basic"), STATUS_CHANGE("status_change");

		public static FlakinessAlgorithm get(String type) {
			return _flakinessAlgorithms.get(type);
		}

		public String getType() {
			return _type;
		}

		private FlakinessAlgorithm(String type) {
			_type = type;
		}

		private static Map<String, FlakinessAlgorithm> _flakinessAlgorithms =
			new HashMap<>();

		static {
			for (FlakinessAlgorithm flakinessAlgorithm : values()) {
				_flakinessAlgorithms.put(
					flakinessAlgorithm.getType(), flakinessAlgorithm);
			}
		}

		private final String _type;

	}

	private static Map<String, JSONObject> _getBuildResultJSONObjects(
		List<String> buildResultJsonURLs) {

		final Map<String, JSONObject> buildResultJSONObjects =
			Collections.synchronizedMap(new HashMap<String, JSONObject>());

		List<Callable<Void>> callables = new ArrayList<>();

		for (final String buildResultJsonURL : buildResultJsonURLs) {
			Callable<Void> callable = new Callable<Void>() {

				@Override
				public Void call() throws IOException {
					buildResultJSONObjects.put(
						buildResultJsonURL,
						JenkinsResultsParserUtil.toJSONObject(
							buildResultJsonURL));

					return null;
				}

			};

			callables.add(callable);
		}

		ThreadPoolExecutor threadPoolExecutor =
			JenkinsResultsParserUtil.getNewThreadPoolExecutor(25, true);

		ParallelExecutor<Void> parallelExecutor = new ParallelExecutor<>(
			callables, threadPoolExecutor);

		parallelExecutor.execute();

		return buildResultJSONObjects;
	}

	private static List<String> _getBuildResultJsonURLs(
		String jobURL, int maxBuilds) {

		List<String> buildResultJsonURLs = new ArrayList<>();

		int lastCompletedBuildNumber =
			JenkinsAPIUtil.getLastCompletedBuildNumber(jobURL);

		int buildNumber = lastCompletedBuildNumber;

		while (buildNumber > (lastCompletedBuildNumber - maxBuilds)) {
			String buildURL = jobURL + buildNumber;

			buildResultJsonURLs.add(
				JenkinsResultsParserUtil.getBuildArtifactURL(
					buildURL, "build-result.json"));

			buildNumber--;
		}

		return buildResultJsonURLs;
	}

	private static FlakinessAlgorithm _flakinessAlgorithm =
		FlakinessAlgorithm.BASIC;
	private static final Map<String, TestHistory> _testHistoryMap =
		new HashMap<>();
	private static final Pattern _testrayLogPattern = Pattern.compile(
		"test[0-9-]+\\/[0-9]+\\/.+?\\/[0-9]+\\/(?<jobVariant>.+?)\\/.*");

}