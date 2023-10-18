/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.metrics;

import com.liferay.jenkins.results.parser.JenkinsResultsParserUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Kenji Heigel
 */
public class JobHistoryDataset {

	public static final long TIMELINE_SAMPLE_PERIOD_MINUTES = 10;

	public static int getTimelineSize(long duration) {
		return (int)
			(duration /
				TimeUnit.MINUTES.toMillis(TIMELINE_SAMPLE_PERIOD_MINUTES));
	}

	public static JobHistoryDataset newAggregateDataset(
		String startDateString, long durationDays) {

		long startTime = JenkinsResultsParserUtil.getMillis(
			_getLocalDateTime(startDateString));

		long duration = TimeUnit.DAYS.toMillis(durationDays);

		return new JobHistoryDataset(
			startTime, duration,
			JobHistoryFactory.newAggregateJobHistories(startTime, duration));
	}

	public static JobHistoryDataset newDefaultDataset(
		String startDateString, long durationDays) {

		long startTime = JenkinsResultsParserUtil.getMillis(
			_getLocalDateTime(startDateString));

		long duration = TimeUnit.DAYS.toMillis(durationDays);

		return new JobHistoryDataset(
			startTime, duration,
			JobHistoryFactory.newDefaultJobHistories(startTime, duration));
	}

	public static JobHistoryDataset newTestSuiteDataset(
		String jobName, String startDateString, long durationDays) {

		long startTime = JenkinsResultsParserUtil.getMillis(
			_getLocalDateTime(startDateString));

		long duration = TimeUnit.DAYS.toMillis(durationDays);

		return new JobHistoryDataset(
			startTime, duration,
			JobHistoryFactory.newTestSuiteJobHistories(
				jobName, startTime, duration));
	}

	public JobHistoryDataset(
		long startTime, long duration, Collection<JobHistory> jobHistories) {

		_startTime = startTime;
		_duration = duration;

		addJobHistories(jobHistories);
	}

	public void addJobHistories(Collection<JobHistory> jobHistories) {
		_jobHistories.addAll(jobHistories);
	}

	public void addJobHistory(JobHistory jobHistory) {
		_jobHistories.add(jobHistory);
	}

	public long getDuration() {
		return _duration;
	}

	public List<JobHistory> getJobHistories() {
		List<JobHistory> jobHistories = new ArrayList<>(_jobHistories);

		Collections.sort(
			jobHistories,
			new Comparator<JobHistory>() {

				@Override
				public int compare(
					JobHistory jobHistory1, JobHistory jobHistory2) {

					Set<BuildDataJSONObject> buildDataJSONObjects1 =
						jobHistory1.getBuildDataJSONObjects();
					Set<BuildDataJSONObject> buildDataJSONObjects2 =
						jobHistory2.getBuildDataJSONObjects();

					Integer size1 = buildDataJSONObjects1.size();
					Integer size2 = buildDataJSONObjects2.size();

					return size2.compareTo(size1);
				}

			});

		return jobHistories;
	}

	public long getStartTime() {
		return _startTime;
	}

	public String getTableDataJSFileContent() {
		JSONArray jsonArray = new JSONArray();

		boolean removeHeader = false;

		AggregateJobHistory aggregateJobHistory = new AggregateJobHistory(
			"Total", getStartTime(), getDuration());

		for (JobHistory jobHistory : getJobHistories()) {
			aggregateJobHistory.addJobHistory(jobHistory);

			JSONArray tableJSONArray = jobHistory.getTableJSONArray();

			if (removeHeader) {
				tableJSONArray.remove(0);
			}
			else {
				removeHeader = true;
			}

			jsonArray.putAll(tableJSONArray);
		}

		JSONArray tableJSONArray = aggregateJobHistory.getTableJSONArray();

		tableJSONArray.remove(0);

		jsonArray.putAll(tableJSONArray);

		return "var tableData = " + jsonArray.toString();
	}

	public String getTimelineDataJSFileContent() {
		JSONObject jsonObject = new JSONObject();

		JSONArray jsonArray = new JSONArray();

		for (JobHistory jobHistory : getJobHistories()) {
			jsonArray.put(jobHistory.getTimelineJSONObject());
		}

		jsonObject.put("jobTimelines", jsonArray);

		int size = getTimelineSize(getDuration());

		long[] timeMillis = new long[size];

		for (int i = 0; i < timeMillis.length; i++) {
			if (i == 0) {
				timeMillis[i] = getStartTime();

				continue;
			}

			timeMillis[i] = timeMillis[i - 1] + (getDuration() / size);
		}

		jsonObject.put("time", new JSONArray(timeMillis));

		return "var timelineData = " + jsonObject.toString();
	}

	private static LocalDateTime _getLocalDateTime(String startDateString) {
		return LocalDateTime.parse(
			startDateString + " 00:00:00",
			DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss"));
	}

	private final long _duration;
	private final Set<JobHistory> _jobHistories = new HashSet<>();
	private final long _startTime;

}