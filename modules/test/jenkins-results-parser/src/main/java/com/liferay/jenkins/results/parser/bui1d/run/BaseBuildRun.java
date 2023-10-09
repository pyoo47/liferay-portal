/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.bui1d.run;

import com.liferay.jenkins.results.parser.Build;
import com.liferay.jenkins.results.parser.JenkinsMaster;
import com.liferay.jenkins.results.parser.JenkinsResultsParserUtil;
import com.liferay.jenkins.results.parser.ParentBuild;
import com.liferay.jenkins.results.parser.Retryable;

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Michael Hashimoto
 */
public abstract class BaseBuildRun implements BuildRun {

	@Override
	public Build getBuild() {
		return _build;
	}

	@Override
	public int getBuildNumber() {
		return _buildNumber;
	}

	@Override
	public URL getBuildURL() {
		if ((_jenkinsMaster == null) || (_buildNumber <= 0)) {
			return null;
		}

		String jobURL = _jenkinsMaster.getURL() + "/job/" + _build.getJobName();

		try {
			return new URL(
				JenkinsResultsParserUtil.combine(
					JenkinsResultsParserUtil.fixURL(jobURL), "/",
					String.valueOf(_buildNumber)));
		}
		catch (MalformedURLException malformedURLException) {
			throw new RuntimeException(malformedURLException);
		}
	}

	@Override
	public JenkinsMaster getJenkinsMaster() {
		return _jenkinsMaster;
	}

	@Override
	public long getJenkinsQueueId() {
		return _jenkinsQueueId;
	}

	@Override
	public Status getStatus() {
		return _status;
	}

	@Override
	public void setBuildNumber(int buildNumber) {
		_buildNumber = buildNumber;
	}

	@Override
	public void setJenkinsMaster(JenkinsMaster jenkinsMaster) {
		_jenkinsMaster = jenkinsMaster;
	}

	@Override
	public void setJenkinsQueueId(long jenkinsQueueId) {
		_jenkinsQueueId = jenkinsQueueId;
	}

	@Override
	public void setStatus(Status status) {
		_status = status;

		_build.setStatus(status.getKey());
	}

	@Override
	public void update() {
		Status status = getStatus();

		if (status == Status.COMPLETED) {
			runCompleted();
		}
		else if (status == Status.MISSING) {
			runMissing();
		}
		else if (status == Status.QUEUED) {
			runQueued();
		}
		else if (status == Status.REPORTING) {
			runReporting();
		}
		else if (status == Status.RUNNING) {
			runRunning();
		}
		else if (status == Status.STARTING) {
			runStarting();
		}
	}

	protected BaseBuildRun(Build build) {
		_build = build;
	}

	protected void runCompleted() {
		String result = _build.getResult();

		if (JenkinsResultsParserUtil.isNullOrEmpty(result)) {
			result = _getResultFromJenkins();
		}

		if (JenkinsResultsParserUtil.isNullOrEmpty(result)) {
			result = "MISSING";
		}

		_build.setResult(result);

		setStatus(Status.COMPLETED);
	}

	protected void runMissing() {
		setStatus(Status.MISSING);

		if (_isJenkinsBuildQueued()) {
			runQueued();

			return;
		}

		if (_isJenkinsBuildRunning()) {
			runRunning();

			return;
		}

		if (_build.hasMaxBuildRunCount()) {
			runReporting();

			return;
		}

		_build.invoke();

		runStarting();
	}

	protected void runQueued() {
		setStatus(Status.QUEUED);

		if (_isJenkinsBuildQueued()) {
			return;
		}

		if (_isJenkinsBuildRunning()) {
			runRunning();

			return;
		}

		setStatus(Status.MISSING);
	}

	protected void runReporting() {
		_build.setResult(_getResultFromJenkins());

		setStatus(Status.REPORTING);

		_build.isApplySlaveOfflineRules();

		if (_build.isApplyReinvokeRules()) {
			runStarting();

			return;
		}

		runCompleted();
	}

	protected void runRunning() {
		setStatus(Status.RUNNING);

		if (!_isJenkinsBuildCompleted()) {
			return;
		}

		runReporting();
	}

	protected void runStarting() {
		setStatus(Status.STARTING);

		_build.reset();

		runQueued();
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

	private String _getResultFromJenkins() {
		JSONObject buildJSONObject = _build.getBuildJSONObject(
			"duration,result");

		if (buildJSONObject == null) {
			return null;
		}

		long duration = buildJSONObject.optLong("duration");
		String result = buildJSONObject.optString("result");

		if ((duration <= 0) || JenkinsResultsParserUtil.isNullOrEmpty(result)) {
			return null;
		}

		return result;
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
				JenkinsMaster jenkinsMaster = getJenkinsMaster();

				String url = JenkinsResultsParserUtil.getLocalURL(
					JenkinsResultsParserUtil.combine(
						String.valueOf(jenkinsMaster.getURL()), "/job/",
						JenkinsResultsParserUtil.fixURL(_build.getJobName()),
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

	private boolean _isJenkinsBuildCompleted() {
		boolean jenkinsBuildCompleted = _isJenkinsBuildCompleted(_build);

		if (jenkinsBuildCompleted) {
			return true;
		}

		if (_build instanceof ParentBuild) {
			ParentBuild parentBuild = (ParentBuild)_build;

			for (Build downstreamBuild : parentBuild.getDownstreamBuilds()) {
				if (!_isJenkinsBuildCompleted(downstreamBuild)) {
					return false;
				}
			}
		}

		return true;
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

	private boolean _isJenkinsBuildQueued() {
		try {
			JSONObject queueItemJSONObject = _getQueueItemJSONObject();

			if (queueItemJSONObject == null) {
				return false;
			}

			return true;
		}
		catch (Exception exception) {
			System.out.println(
				JenkinsResultsParserUtil.combine(
					"[", _build.getBuildName(), "] Unable to get queue item"));
		}

		return false;
	}

	private boolean _isJenkinsBuildRunning() {
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

			System.out.println(
				JenkinsResultsParserUtil.combine(
					"[", _build.getBuildName(), "] Unable to get build item"));
		}

		return false;
	}

	private final Build _build;
	private int _buildNumber;
	private JenkinsMaster _jenkinsMaster;
	private long _jenkinsQueueId;
	private Status _status;

}