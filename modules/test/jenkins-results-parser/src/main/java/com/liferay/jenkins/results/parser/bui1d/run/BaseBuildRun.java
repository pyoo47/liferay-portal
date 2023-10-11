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
	public Status getStatus() {
		return _status;
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

	protected abstract boolean isJenkinsBuildCompleted();

	protected abstract boolean isJenkinsBuildQueued();

	protected abstract boolean isJenkinsBuildRunning();

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

		if (isJenkinsBuildQueued()) {
			runQueued();

			return;
		}

		if (isJenkinsBuildRunning()) {
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

		if (isJenkinsBuildQueued()) {
			return;
		}

		if (isJenkinsBuildRunning()) {
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

		if (!isJenkinsBuildCompleted()) {
			return;
		}

		runReporting();
	}

	protected void runStarting() {
		setStatus(Status.STARTING);

		_build.reset();

		runQueued();
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

	private final Build _build;
	private int _buildNumber;
	private int _invokedBatchSize;
	private JenkinsCohort _jenkinsCohort;
	private JenkinsMaster _jenkinsMaster;
	private long _jenkinsQueueId;
	private int _maximumSlavesPerHost;
	private int _minimumSlaveRAM;
	private Status _status;

}