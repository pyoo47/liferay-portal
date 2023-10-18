/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.metrics;

import com.liferay.jenkins.results.parser.JenkinsResultsParserUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Kenji Heigel
 */
public abstract class BaseJobHistory implements JobHistory {

	public BaseJobHistory(String name, long startTime, long duration) {
		_name = name;
		_startTime = startTime;
		_duration = duration;
	}

	@Override
	public void addBuildDataJSONObject(
		BuildDataJSONObject buildDataJSONObject) {

		_buildDataJSONObjects.add(buildDataJSONObject);
	}

	@Override
	public void addTopLevelBuildURL(String url) {
		_topLevelBuildURLs.add(url);
	}

	@Override
	public boolean containsTopLevelBuildURL(String url) {
		return _topLevelBuildURLs.contains(url);
	}

	@Override
	public Set<BuildDataJSONObject> getBuildDataJSONObjects() {
		return _buildDataJSONObjects;
	}

	@Override
	public long getDuration() {
		return _duration;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public long getStartTime() {
		return _startTime;
	}

	@Override
	public Table getTable() {
		if (_table == null) {
			initTable();
		}

		return _table;
	}

	@Override
	public JSONArray getTableJSONArray() {
		Table table = getTable();

		return table.getJSONArray();
	}

	@Override
	public JobHistory.Timeline getTimeline() {
		if (_timeline == null) {
			initTimeline();
		}

		return _timeline;
	}

	@Override
	public JSONObject getTimelineJSONObject() {
		Timeline timeline = getTimeline();

		return timeline.getJSONObject();
	}

	public Set<String> getTopLevelBuildURLs() {
		return _topLevelBuildURLs;
	}

	@Override
	public void initTimeline() {
		_timeline = new BaseTimeline(this, getStartTime(), getDuration());
	}

	public void setTable(Table table) {
		_table = table;
	}

	protected void addBuildDataJSONObjects(
		Collection<BuildDataJSONObject> buildDataJSONObjects) {

		_buildDataJSONObjects.addAll(buildDataJSONObjects);
	}

	protected void addTopLevelBuildURLs(Collection<String> urls) {
		_topLevelBuildURLs.addAll(urls);
	}

	protected abstract class BaseTable implements Table {

		@Override
		public String getFirstColumnEntryName() {
			return getName();
		}

		@Override
		public JSONArray getJSONArray() {
			JSONArray jsonArray = new JSONArray();

			for (List<Object> row : _rows) {
				jsonArray.put(new JSONArray(row));
			}

			return jsonArray;
		}

		protected BaseTable() {
			Set<BuildDataJSONObject> buildDataJSONObjects =
				getBuildDataJSONObjects();

			Map<String, List<BuildDataJSONObject>>
				groupedBuildDataJSONObjectsMap = new TreeMap<>();

			final String[] dateStrings =
				JenkinsResultsParserUtil.getDateStrings(
					getStartTime(), getDuration());

			for (String dateString : dateStrings) {
				groupedBuildDataJSONObjectsMap.put(
					dateString, new ArrayList<BuildDataJSONObject>());
			}

			for (BuildDataJSONObject buildDataJSONObject :
					buildDataJSONObjects) {

				String startDateString =
					buildDataJSONObject.getStartDateString();

				if (!groupedBuildDataJSONObjectsMap.containsKey(
						startDateString)) {

					continue;
				}

				List<BuildDataJSONObject> groupedBuildDataJSONObjects =
					groupedBuildDataJSONObjectsMap.get(startDateString);

				groupedBuildDataJSONObjects.add(buildDataJSONObject);
			}

			int size = groupedBuildDataJSONObjectsMap.size();

			_averageTopLevelBuildDurations = new Long[size];
			_averageTopLevelQueueDurations = new Long[size];
			_invokedBuilds = new Long[size];
			_invokedTopLevelBuilds = new Long[size];
			_totalServerDurations = new Long[size];

			int index = 0;

			for (List<BuildDataJSONObject> groupedBuildDataJSONObjects :
					groupedBuildDataJSONObjectsMap.values()) {

				long buildsInvoked = 0;
				long topLevelBuildsInvoked = 0;
				long totalTopLevelBuildDuration = 0;
				long totalDownstreamBuildDuration = 0;
				long totalTopLevelQueueDuration = 0;

				for (BuildDataJSONObject buildDataJSONObject :
						groupedBuildDataJSONObjects) {

					if (containsTopLevelBuildURL(
							buildDataJSONObject.getURL())) {

						topLevelBuildsInvoked++;

						totalTopLevelBuildDuration +=
							buildDataJSONObject.getDuration();

						totalTopLevelQueueDuration +=
							buildDataJSONObject.getQueueDuration();
					}
					else {
						buildsInvoked++;

						totalDownstreamBuildDuration +=
							buildDataJSONObject.getDuration();
					}
				}

				_invokedBuilds[index] = buildsInvoked;
				_invokedTopLevelBuilds[index] = topLevelBuildsInvoked;

				if (topLevelBuildsInvoked != 0) {
					_averageTopLevelBuildDurations[index] =
						totalTopLevelBuildDuration / topLevelBuildsInvoked;

					_averageTopLevelQueueDurations[index] =
						totalTopLevelQueueDuration / topLevelBuildsInvoked;
				}
				else {
					_averageTopLevelBuildDurations[index] = 0L;
					_averageTopLevelQueueDurations[index] = 0L;
				}

				_totalServerDurations[index] =
					totalTopLevelBuildDuration + totalDownstreamBuildDuration;

				index++;
			}

			_rows.add(
				new ArrayList<Object>() {
					{
						add(getFirstColumnHeader());
						add("Metric");
						addAll(Arrays.asList(dateStrings));
					}
				});

			_rows.add(
				new ArrayList<Object>() {
					{
						add(getFirstColumnEntryName());
						add("Invoked Builds");
						addAll(Arrays.asList(_invokedBuilds));
					}
				});

			_rows.add(
				new ArrayList<Object>() {
					{
						add(getFirstColumnEntryName());
						add("Invoked Top Level Builds");
						addAll(Arrays.asList(_invokedTopLevelBuilds));
					}
				});

			_rows.add(
				new ArrayList<Object>() {
					{
						add(getFirstColumnEntryName());
						add("Average Top Level Build Duration");
						addAll(Arrays.asList(_averageTopLevelBuildDurations));
					}
				});

			_rows.add(
				new ArrayList<Object>() {
					{
						add(getFirstColumnEntryName());
						add("Average Top Level Duration in Queue");
						addAll(Arrays.asList(_averageTopLevelQueueDurations));
					}
				});

			_rows.add(
				new ArrayList<Object>() {
					{
						add(getFirstColumnEntryName());
						add("Total Server Duration");
						addAll(Arrays.asList(_totalServerDurations));
					}
				});
		}

		private final Long[] _averageTopLevelBuildDurations;
		private final Long[] _averageTopLevelQueueDurations;
		private final Long[] _invokedBuilds;
		private final Long[] _invokedTopLevelBuilds;
		private final List<List<Object>> _rows = new ArrayList<>();
		private final Long[] _totalServerDurations;

	}

	protected class BaseTimeline implements Timeline {

		public JSONObject getJSONObject() {
			JSONObject jsonObject = new JSONObject();

			jsonObject.put(
				"averageBuildTime", new JSONArray(_averageBuildTime)
			).put(
				"averageQueueTime", new JSONArray(_averageQueueTime)
			).put(
				"buildCounts", new JSONArray(_buildCounts)
			).put(
				"name", _name
			).put(
				"topLevelBuildCounts", new JSONArray(_topLevelBuildCounts)
			);

			return jsonObject;
		}

		protected BaseTimeline(
			JobHistory jobHistory, long startTime, long duration) {

			_startTime = startTime;
			_duration = duration;

			_size = JobHistoryDataset.getTimelineSize(duration);

			_buildCounts = new long[_size];

			_name = jobHistory.getName();
			_topLevelBuildCounts = new long[_size];

			Set<BuildDataJSONObject> buildDataJSONObjects =
				jobHistory.getBuildDataJSONObjects();

			long[] buildCountsForAverage = new long[_size];
			long[] totalBuildTime = new long[_size];
			long[] totalQueueTime = new long[_size];

			for (BuildDataJSONObject buildDataJSONObject :
					buildDataJSONObjects) {

				long buildDuration = buildDataJSONObject.getDuration();

				if (buildDuration < (5 * 60 * 1000)) {
					continue;
				}

				long buildStartTime = buildDataJSONObject.getStartTime();
				long queueDuration = buildDataJSONObject.getQueueDuration();

				int startIndex = _getIndex(buildStartTime);

				totalBuildTime[startIndex] += buildDuration;
				totalQueueTime[startIndex] += queueDuration;

				buildCountsForAverage[startIndex]++;

				int endIndex =
					startIndex + _getDurationIndexSize(buildDuration);

				if (endIndex > (_size - 1)) {
					endIndex = _size - 1;
				}

				for (int i = startIndex; i < endIndex; i++) {
					_buildCounts[i]++;

					if (jobHistory.containsTopLevelBuildURL(
							buildDataJSONObject.getURL())) {

						_topLevelBuildCounts[i]++;
					}
				}
			}

			_averageBuildTime = new long[_size];
			_averageQueueTime = new long[_size];

			for (int i = 0; i < _size; i++) {
				if (buildCountsForAverage[i] == 0) {
					_averageBuildTime[i] = 0;
					_averageQueueTime[i] = 0;

					continue;
				}

				_averageBuildTime[i] =
					totalBuildTime[i] / buildCountsForAverage[i];
				_averageQueueTime[i] =
					totalQueueTime[i] / buildCountsForAverage[i];
			}
		}

		private int _getDurationIndexSize(long duration) {
			long durationIndexSize = JobHistoryDataset.getTimelineSize(
				duration);

			long timelineSamplePeriodMillis = TimeUnit.MINUTES.toMillis(
				JobHistoryDataset.TIMELINE_SAMPLE_PERIOD_MINUTES);

			long roundingSize = timelineSamplePeriodMillis / 2;

			long durationRemainder = duration % timelineSamplePeriodMillis;

			if (durationRemainder >= roundingSize) {
				durationIndexSize++;
			}

			return (int)durationIndexSize;
		}

		private int _getIndex(long timeMillis) {
			int index = (int)((timeMillis - _startTime) * _size / _duration);

			if (index >= _size) {
				return _size - 1;
			}

			if (index < 0) {
				return 0;
			}

			return index;
		}

		private final long[] _averageBuildTime;
		private final long[] _averageQueueTime;
		private final long[] _buildCounts;
		private final long _duration;
		private final String _name;
		private final int _size;
		private final long _startTime;
		private final long[] _topLevelBuildCounts;

	}

	private final Set<BuildDataJSONObject> _buildDataJSONObjects =
		new HashSet<>();
	private final long _duration;
	private final String _name;
	private final long _startTime;
	private Table _table;
	private Timeline _timeline;
	private final Set<String> _topLevelBuildURLs = new HashSet<>();

}