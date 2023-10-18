/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.metrics;

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Kenji Heigel
 */
public interface JobHistory {

	public void addBuildDataJSONObject(BuildDataJSONObject buildDataJSONObject);

	public void addTopLevelBuildURL(String url);

	public boolean containsTopLevelBuildURL(String url);

	public Set<BuildDataJSONObject> getBuildDataJSONObjects();

	public long getDuration();

	public String getName();

	public long getStartTime();

	public Table getTable();

	public JSONArray getTableJSONArray();

	public Timeline getTimeline();

	public JSONObject getTimelineJSONObject();

	public Set<String> getTopLevelBuildURLs();

	public void initTable();

	public void initTimeline();

	public interface Table {

		public String getFirstColumnEntryName();

		public String getFirstColumnHeader();

		public JSONArray getJSONArray();

	}

	public interface Timeline {

		public JSONObject getJSONObject();

	}

}