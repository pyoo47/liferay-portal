/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.testray;

import com.liferay.jenkins.results.parser.TopLevelBuild;

import java.net.URL;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

/**
 * @author Kenji Heigel
 */
public interface TestrayCaseResult {

	public TestrayAttachment getBuildResultTestrayAttachment();

	public String getCaseID();

	public String getComponentName();

	public String getErrors();

	public URL getHistoryURL();

	public long getID();

	public JSONObject getJSONObject();

	public String getName();

	public int getPriority();

	public Status getStatus();

	public String getSubcomponentNames();

	public String getTeamName();

	public List<TestrayAttachment> getTestrayAttachments();

	public TestrayBuild getTestrayBuild();

	public TestrayCase getTestrayCase();

	public TestrayProject getTestrayProject();

	public TestrayServer getTestrayServer();

	public TopLevelBuild getTopLevelBuild();

	public String getType();

	public URL getURL();

	public String[] getWarnings();

	public static enum Status {

		BLOCKED(4, "blocked"), DID_NOT_RUN(6, "dnr"), FAILED(3, "failed"),
		IN_PROGRESS(1, "in-progress"), PASSED(2, "passed"),
		TEST_FIX(7, "test-fix"), UNTESTED(1, "untested");

		public static Status get(Integer id) {
			return _statuses.get(id);
		}

		public static List<Status> getFailedStatuses() {
			return Arrays.asList(
				BLOCKED, DID_NOT_RUN, FAILED, IN_PROGRESS, TEST_FIX, UNTESTED);
		}

		public Integer getID() {
			return _id;
		}

		public String getName() {
			return _name;
		}

		private Status(Integer id, String name) {
			_id = id;
			_name = name;
		}

		private static Map<Integer, Status> _statuses = new HashMap<>();

		static {
			for (Status status : values()) {
				_statuses.put(status.getID(), status);
			}
		}

		private final Integer _id;
		private final String _name;

	}

}