/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.bui1d.run;

import com.liferay.jenkins.results.parser.Build;
import com.liferay.jenkins.results.parser.JenkinsCohort;
import com.liferay.jenkins.results.parser.JenkinsMaster;
import com.liferay.jenkins.results.parser.JenkinsResultsParserUtil;
import com.liferay.jenkins.results.parser.ParentBuild;
import com.liferay.jenkins.results.parser.Retryable;

import java.io.IOException;

import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Michael Hashimoto
 */
public class DefaultBuildRun extends BaseBuildRun {

	@Override
	public void invoke() {
		Build build = getBuild();

		JenkinsMaster jenkinsMaster = getJenkinsMaster();

		if (jenkinsMaster == null) {
			JenkinsCohort jenkinsCohort = getJenkinsCohort();

			jenkinsMaster = jenkinsCohort.getMostAvailableJenkinsMaster(
				getInvokedBatchSize(), getMinimumSlaveRAM(),
				getMaximumSlavesPerHost());

			setJenkinsMaster(jenkinsMaster);
		}

		JSONObject jsonObject = JenkinsResultsParserUtil.invokeJenkinsBuild(
			jenkinsMaster, build.getJobName(), build.getParameters());

		setJenkinsQueueId(jsonObject.getLong("queueId"));
	}

	protected DefaultBuildRun(Build build) {
		super(build);
	}

	@Override
	protected boolean isJenkinsBuildCompleted() {
		Build build = getBuild();

		if (!_isJenkinsBuildCompleted(build)) {
			return false;
		}

		if (build instanceof ParentBuild) {
			ParentBuild parentBuild = (ParentBuild)build;

			for (Build downstreamBuild : parentBuild.getDownstreamBuilds()) {
				if (!_isJenkinsBuildCompleted(downstreamBuild)) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	protected boolean isJenkinsBuildQueued() {
		try {
			JSONObject queueItemJSONObject = _getQueueItemJSONObject();

			if (queueItemJSONObject == null) {
				return false;
			}

			return true;
		}
		catch (Exception exception) {
			Build build = getBuild();

			System.out.println(
				JenkinsResultsParserUtil.combine(
					"[", build.getBuildName(), "] Unable to get queue item"));
		}

		return false;
	}

	protected boolean isJenkinsBuildRunning() {
		try {
			JSONObject runningBuildJSONObject = _getRunningBuildJSONObject();

			if (runningBuildJSONObject == null) {
				return false;
			}

			setBuildNumber(runningBuildJSONObject.getInt("number"));

			return true;
		}
		catch (Exception exception) {
			exception.printStackTrace();

			Build build = getBuild();

			System.out.println(
				JenkinsResultsParserUtil.combine(
					"[", build.getBuildName(), "] Unable to get build item"));
		}

		return false;
	}

	private JSONObject _getQueueItemJSONObject() {
		try {
			JenkinsMaster jenkinsMaster = getJenkinsMaster();

			if (jenkinsMaster == null) {
				return null;
			}

			JSONObject jsonObject = JenkinsResultsParserUtil.toJSONObject(
				JenkinsResultsParserUtil.combine(
					String.valueOf(jenkinsMaster.getURL()),
					"/queue/api/json?tree=items[id]"),
				false);

			JSONArray queueItemsJSONArray = jsonObject.getJSONArray("items");

			if (queueItemsJSONArray == null) {
				return null;
			}

			for (int i = 0; i < queueItemsJSONArray.length(); i++) {
				JSONObject queueItemJSONObject =
					queueItemsJSONArray.getJSONObject(i);

				if (Objects.equals(
						queueItemJSONObject.getLong("id"),
						getJenkinsQueueId())) {

					return queueItemJSONObject;
				}
			}
		}
		catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}

		return null;
	}

	private JSONObject _getRunningBuildJSONObject() {
		int page = 0;

		while (true) {
			JSONArray runningBuildsJSONArray = _getRunningBuildsJSONArray(page);

			if (runningBuildsJSONArray.length() == 0) {
				break;
			}

			for (int i = 0; i < runningBuildsJSONArray.length(); i++) {
				JSONObject runningBuildJSONObject =
					runningBuildsJSONArray.getJSONObject(i);

				if (Objects.equals(
						runningBuildJSONObject.getLong("queueId"),
						getJenkinsQueueId())) {

					return runningBuildJSONObject;
				}
			}

			page++;
		}

		return null;
	}

	private JSONArray _getRunningBuildsJSONArray(final int page) {
		Retryable<JSONArray> retryable = new Retryable<JSONArray>(
			true, 2, 10, true) {

			@Override
			public JSONArray execute() {
				Build build = getBuild();

				JenkinsMaster jenkinsMaster = getJenkinsMaster();

				String url = JenkinsResultsParserUtil.getLocalURL(
					JenkinsResultsParserUtil.combine(
						String.valueOf(jenkinsMaster.getURL()), "/job/",
						JenkinsResultsParserUtil.fixURL(build.getJobName()),
						"/api/json?tree=allBuilds[number,queueId]{",
						String.valueOf(page * 100), ",",
						String.valueOf((page + 1) * 100), "}"));

				try {
					JSONObject jsonObject =
						JenkinsResultsParserUtil.toJSONObject(url, false);

					return jsonObject.getJSONArray("allBuilds");
				}
				catch (IOException ioException) {
					throw new RuntimeException(ioException);
				}
			}

		};

		return retryable.executeWithRetries();
	}

	private boolean _isJenkinsBuildCompleted(Build build) {
		JSONObject buildJSONObject = build.getBuildJSONObject(
			"duration,result");

		if (buildJSONObject == null) {
			return false;
		}

		long duration = buildJSONObject.optLong("duration");
		String result = buildJSONObject.optString("result");

		if ((duration == 0) || JenkinsResultsParserUtil.isNullOrEmpty(result)) {
			return false;
		}

		return true;
	}

}