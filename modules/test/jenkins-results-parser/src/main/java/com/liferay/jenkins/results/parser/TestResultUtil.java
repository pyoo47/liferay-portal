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

import java.io.IOException;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Kenji Heigel
 */
public class TestResultUtil {

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

				String key = name + "/" + jobVariant;

				if (!_testDataMap.containsKey(key)) {
					_testDataMap.put(
						key,
						new TestData(
							name, jobVariant, status, buildURL, errorDetails));

					continue;
				}

				TestData testData = _testDataMap.get(key);

				testData.update(buildURL, errorDetails, status);
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

	public static class TestData {

		public TestData(
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

		public boolean isFlaky() {
			return isFlakyBasicAlgorithm();
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

			return jsonArray;
		}

		public void update(
			String buildURL, String errorDetails, String status) {

			_buildURLs.add(buildURL);
			_errorSnippets.add(errorDetails);
			_statuses.add(status);
		}

		protected boolean isFlakyAlgorithm1() {
			int statusChanges = 0;

			String lastStatus = null;

			for (String status : _statuses) {
				if (lastStatus == null) {
					lastStatus = status;

					continue;
				}

				if (!lastStatus.equals(status)) {
					lastStatus = status;

					statusChanges++;
				}
			}

			if (statusChanges > 1) {
				return true;
			}

			return false;
		}

		protected boolean isFlakyBasicAlgorithm() {
			if (Collections.frequency(_statuses, _statuses.get(0)) <
					_statuses.size()) {

				return true;
			}

			return false;
		}

		private final String _batchName;
		private final List<String> _buildURLs = new ArrayList<>();
		private final List<String> _errorSnippets = new ArrayList<>();
		private final String _name;
		private final List<String> _statuses = new ArrayList<>();

	}

	private static final Map<String, TestData> _testDataMap = new HashMap<>();

}