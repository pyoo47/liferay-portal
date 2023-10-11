/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.bui1d.run;

import com.liferay.jenkins.results.parser.Build;
import com.liferay.jenkins.results.parser.JenkinsCohort;
import com.liferay.jenkins.results.parser.JenkinsMaster;
import com.liferay.jenkins.results.parser.JenkinsResultsParserUtil;

import java.net.MalformedURLException;
import java.net.URL;

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

		String jobURL =
			_jenkinsMaster.getRemoteURL() + "job/" + _build.getJobName();

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
	public int getInvokedBatchSize() {
		return _invokedBatchSize;
	}

	@Override
	public JenkinsCohort getJenkinsCohort() {
		if (_jenkinsCohort != null) {
			return _jenkinsCohort;
		}

		if (_jenkinsMaster != null) {
			_jenkinsCohort = _jenkinsMaster.getJenkinsCohort();
		}

		return _jenkinsCohort;
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
	public int getMaximumSlavesPerHost() {
		return _maximumSlavesPerHost;
	}

	@Override
	public int getMinimumSlaveRAM() {
		return _minimumSlaveRAM;
	}

	@Override
	public Result getResult() {
		if (_result != null) {
			return _result;
		}

		if (!isCompleted()) {
			return null;
		}

		String jenkinsResult = getResultFromJenkins();

		if (JenkinsResultsParserUtil.isNullOrEmpty(jenkinsResult)) {
			return null;
		}

		for (Result result : Result.values()) {
			if (jenkinsResult.equals(result.toString())) {
				_result = result;

				break;
			}
		}

		return _result;
	}

	@Override
	public Status getStatus() {
		return _status;
	}

	public boolean isCompleted() {
		if (getStatus() == Status.COMPLETED) {
			return true;
		}

		return false;
	}

	public boolean isFailing() {
		if (!isCompleted() || (getResult() == Result.SUCCESS)) {
			return false;
		}

		return true;
	}

	@Override
	public void setBuildNumber(int buildNumber) {
		_buildNumber = buildNumber;
	}

	@Override
	public void setInvokedBatchSize(int invokedBatchSize) {
		_invokedBatchSize = invokedBatchSize;
	}

	@Override
	public void setJenkinsCohort(JenkinsCohort jenkinsCohort) {
		_jenkinsCohort = jenkinsCohort;
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
	public void setMaximumSlavesPerHost(int maximumSlavesPerHost) {
		_maximumSlavesPerHost = maximumSlavesPerHost;
	}

	@Override
	public void setMinimumSlaveRAM(int minimumSlaveRAM) {
		_minimumSlaveRAM = minimumSlaveRAM;
	}

	@Override
	public void setStatus(Status status) {
		_status = status;
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

	protected String getResultFromJenkins() {
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

	protected abstract boolean isJenkinsBuildCompleted();

	protected abstract boolean isJenkinsBuildQueued();

	protected abstract boolean isJenkinsBuildRunning();

	protected void runCompleted() {
		setStatus(Status.COMPLETED);
	}

	protected void runMissing() {
		setStatus(Status.MISSING);

		_build.setStatus(Status.MISSING.getKey());

		if (isJenkinsBuildQueued()) {
			runQueued();

			return;
		}

		if (isJenkinsBuildRunning()) {
			runRunning();

			return;
		}

		runCompleted();
	}

	protected void runQueued() {
		setStatus(Status.QUEUED);

		_build.setStatus(Status.QUEUED.getKey());

		if (isJenkinsBuildQueued()) {
			return;
		}

		if (isJenkinsBuildRunning()) {
			runRunning();

			return;
		}

		setStatus(Status.MISSING);

		_build.setStatus(Status.MISSING.getKey());
	}

	protected void runRunning() {
		setStatus(Status.RUNNING);

		_build.setStatus(Status.RUNNING.getKey());

		if (!isJenkinsBuildCompleted()) {
			return;
		}

		runCompleted();
	}

	protected void runStarting() {
		setStatus(Status.STARTING);

		_build.setStatus(Status.STARTING.getKey());

		_build.reset();

		runQueued();
	}

	private final Build _build;
	private int _buildNumber;
	private int _invokedBatchSize;
	private JenkinsCohort _jenkinsCohort;
	private JenkinsMaster _jenkinsMaster;
	private long _jenkinsQueueId;
	private int _maximumSlavesPerHost;
	private int _minimumSlaveRAM;
	private Result _result;
	private Status _status;

}