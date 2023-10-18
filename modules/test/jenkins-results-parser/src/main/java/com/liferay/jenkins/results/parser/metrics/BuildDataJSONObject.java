/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.metrics;

import com.liferay.jenkins.results.parser.JenkinsResultsParserUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Kenji Heigel
 */
public class BuildDataJSONObject extends JSONObject {

	public BuildDataJSONObject(JSONObject jsonObject) {
		this(jsonObject.toString());
	}

	public BuildDataJSONObject(String source) {
		super(source);
	}

	public long getDuration() {
		return optLong("duration");
	}

	public String getJobName() {
		return _getJobName(getURL());
	}

	public Map<String, String> getParameters() {
		Map<String, String> parameters = new HashMap<>();

		JSONArray parametersJSONArray = optJSONArray("parameters");

		for (int i = 0; i < parametersJSONArray.length(); i++) {
			JSONObject jsonObject = parametersJSONArray.getJSONObject(i);

			parameters.put(
				jsonObject.optString("name"), jsonObject.optString("value"));
		}

		return parameters;
	}

	public long getQueueDuration() {
		return optLong("queueDuration");
	}

	public String getStartDateString() {
		if (_startDateString == null) {
			LocalDate startDate = JenkinsResultsParserUtil.getLocalDate(
				getStartTime());

			_startDateString = startDate.format(
				DateTimeFormatter.ofPattern("yyyyMMdd"));
		}

		return _startDateString;
	}

	public long getStartTime() {
		return optLong("startTime");
	}

	public String getTopLevelBuildURL() {
		return _topLevelBuildURL;
	}

	public String getURL() {
		return optString("url");
	}

	public void setTopLevelBuildURL(String topLevelBuildURL) {
		_topLevelBuildURL = topLevelBuildURL;
	}

	private String _getJobName(String buildURL) {
		if (buildURL == null) {
			return null;
		}

		Matcher matcher = _buildURLPattern.matcher(buildURL);

		if (!matcher.find()) {
			return null;
		}

		return matcher.group("jobName");
	}

	private static final Pattern _buildURLPattern = Pattern.compile(
		JenkinsResultsParserUtil.combine(
			"(?<jobURL>https?://(?<masterHostname>",
			"(?<cohortName>test-\\d+)-\\d+)(\\.liferay\\.com)?/job/",
			"(?<jobName>[^/]+)/(.*/)?)(?<buildNumber>\\d+)/?"));

	private String _startDateString;
	private String _topLevelBuildURL;

}