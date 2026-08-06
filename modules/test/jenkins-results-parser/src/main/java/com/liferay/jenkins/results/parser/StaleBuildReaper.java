/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Calum Ragan
 */
public class StaleBuildReaper {

	public StaleBuildReaper(JenkinsCohort jenkinsCohort, boolean dryRun) {
		_jenkinsCohort = jenkinsCohort;
		_dryRun = dryRun;
	}

	public int getReapedBuildCount() {
		int reapedBuildCount = 0;

		for (ReapAction reapAction : _reapActions) {
			if (reapAction.isExecuted()) {
				reapedBuildCount++;
			}
		}

		return reapedBuildCount;
	}

	public int getStaleBuildCount() {
		return _reapActions.size();
	}

	public String getSummary() {
		StringBuilder sb = new StringBuilder();

		int staleBuildCount = getStaleBuildCount();

		String nounForm = JenkinsResultsParserUtil.getNounForm(
			staleBuildCount, "stale builds", "stale build");

		if (_dryRun) {
			sb.append("Found ");
			sb.append(staleBuildCount);
			sb.append(" ");
			sb.append(nounForm);
			sb.append(". No build was aborted because DRY_RUN is enabled.");
		}
		else {
			sb.append("Reaped ");
			sb.append(getReapedBuildCount());
			sb.append(" of ");
			sb.append(staleBuildCount);
			sb.append(" ");
			sb.append(nounForm);
			sb.append(".");
		}

		for (ReapAction reapAction : _reapActions) {
			sb.append("\n");
			sb.append(reapAction.getSummary());
		}

		return sb.toString();
	}

	public void reap() {
		_generateReapActions();

		_executeReapActions();

		String summary = getSummary();

		System.out.println(summary);

		_sendSlackNotification(summary);
	}

	public static enum Reason {

		JENKINS_SLAVE_BEING_REMOVED("its node is being removed"),
		JENKINS_SLAVE_OFFLINE("its node is offline"),
		LIKELY_STUCK("its executor reports likelyStuck"),
		PAST_ESTIMATED_DURATION("it is running far past its estimate");

		public String getDescription() {
			return _description;
		}

		private Reason(String description) {
			_description = description;
		}

		private final String _description;

	}

	private void _executeReapActions() {
		if (_dryRun) {
			return;
		}

		for (ReapAction reapAction : _reapActions) {
			reapAction.execute();
		}
	}

	private void _generateReapActions() {
		long currentTimeMillis =
			JenkinsResultsParserUtil.getCurrentTimeMillis();

		for (JenkinsMaster jenkinsMaster : _getJenkinsMasters()) {
			jenkinsMaster.update(false);

			List<JenkinsMaster.RunningBuild> runningBuilds =
				jenkinsMaster.getRunningBuilds();

			int runningBuildCount = jenkinsMaster.getRunningBuildCount();

			System.out.println(
				JenkinsResultsParserUtil.combine(
					jenkinsMaster.getName(), " has ",
					String.valueOf(runningBuildCount), " ",
					JenkinsResultsParserUtil.getNounForm(
						runningBuildCount, "running builds", "running build"),
					". Enumerated ", String.valueOf(runningBuilds.size()),
					"."));

			for (JenkinsMaster.RunningBuild runningBuild : runningBuilds) {
				long duration = runningBuild.getDuration(currentTimeMillis);

				List<Reason> reasons = _getReasons(duration, runningBuild);

				if (reasons.isEmpty()) {
					continue;
				}

				_reapActions.add(
					new ReapAction(duration, reasons, runningBuild));
			}
		}
	}

	private List<Reason> _getDurationReasons(
		long duration, JenkinsMaster.RunningBuild runningBuild) {

		List<Reason> reasons = new ArrayList<>();

		// Jenkins derives likelyStuck as ten times the estimate when an
		// estimate exists, so reporting both it and our own multiplier would
		// count one signal twice. The multiplier must track Jenkins' factor.

		if (runningBuild.isLikelyStuck()) {
			reasons.add(Reason.LIKELY_STUCK);

			return reasons;
		}

		long estimatedDuration = runningBuild.getEstimatedDuration();

		if (estimatedDuration <= 0) {
			return reasons;
		}

		if (duration > (estimatedDuration * _ESTIMATED_DURATION_MULTIPLIER)) {
			reasons.add(Reason.PAST_ESTIMATED_DURATION);
		}

		return reasons;
	}

	private List<JenkinsMaster> _getJenkinsMasters() {
		List<JenkinsMaster> jenkinsMasters = new ArrayList<>(
			_jenkinsCohort.getAvailableJenkinsMasters());

		jenkinsMasters.addAll(_jenkinsCohort.getBlacklistedJenkinsMasters());

		return jenkinsMasters;
	}

	private List<Reason> _getJenkinsSlaveReasons(
		JenkinsMaster.RunningBuild runningBuild) {

		List<Reason> reasons = new ArrayList<>();

		if (runningBuild.isJenkinsSlaveBeingRemoved()) {
			reasons.add(Reason.JENKINS_SLAVE_BEING_REMOVED);
		}
		else if (runningBuild.isJenkinsSlaveOffline()) {
			reasons.add(Reason.JENKINS_SLAVE_OFFLINE);
		}

		return reasons;
	}

	private List<Reason> _getReasons(
		long duration, JenkinsMaster.RunningBuild runningBuild) {

		List<Reason> reasons = new ArrayList<>();

		if (!runningBuild.isBuilding()) {
			return reasons;
		}

		// An agent that drops offline for a moment while reconnecting is
		// ordinary, so the slave reasons still wait, but only for minutes.
		// Once an agent is genuinely gone the build is already dead and
		// holding its executor, which is the leak this job exists to close.

		if (duration >= _MINIMUM_DURATION_JENKINS_SLAVE) {
			reasons.addAll(_getJenkinsSlaveReasons(runningBuild));
		}

		if (duration >= _MINIMUM_DURATION_BUILD) {
			reasons.addAll(_getDurationReasons(duration, runningBuild));
		}

		return reasons;
	}

	private void _sendSlackNotification(String summary) {
		if (_reapActions.isEmpty()) {
			return;
		}

		String subject = "Stale builds reaped";

		if (_dryRun) {
			subject = "Stale builds detected";
		}

		NotificationUtil.sendSlackNotification(
			summary, "ci-notifications", subject);
	}

	private static final int _ESTIMATED_DURATION_MULTIPLIER = 10;

	private static final long _MINIMUM_DURATION_BUILD = 4 * 60 * 60 * 1000L;

	private static final long _MINIMUM_DURATION_JENKINS_SLAVE = 15 * 60 * 1000L;

	private final boolean _dryRun;
	private final JenkinsCohort _jenkinsCohort;
	private final List<ReapAction> _reapActions = new ArrayList<>();

	private class ReapAction {

		public void execute() {
			String buildURL = _runningBuild.getURL();

			try {
				JenkinsStopBuildUtil.stopBuild(buildURL, false);

				_executed = true;
			}
			catch (Exception exception) {
				System.out.println("Unable to reap " + buildURL);

				exception.printStackTrace();
			}
		}

		public String getSummary() {
			StringBuilder sb = new StringBuilder();

			JenkinsMaster jenkinsMaster = _runningBuild.getJenkinsMaster();

			sb.append(jenkinsMaster.getName());

			sb.append(" ");
			sb.append(_runningBuild.getFullDisplayName());
			sb.append(" on ");
			sb.append(_runningBuild.getJenkinsSlaveName());
			sb.append(" has been running for ");
			sb.append(JenkinsResultsParserUtil.toDurationString(_duration));

			long estimatedDuration = _runningBuild.getEstimatedDuration();

			if (estimatedDuration > 0) {
				sb.append(" against an estimate of ");
				sb.append(
					JenkinsResultsParserUtil.toDurationString(
						estimatedDuration));
			}

			sb.append(". Flagged because ");

			List<String> descriptions = new ArrayList<>();

			for (Reason reason : _reasons) {
				descriptions.add(reason.getDescription());
			}

			sb.append(JenkinsResultsParserUtil.join(", and ", descriptions));

			sb.append(". ");
			sb.append(_getOutcome());
			sb.append(" ");
			sb.append(_runningBuild.getURL());

			return sb.toString();
		}

		public boolean isExecuted() {
			return _executed;
		}

		private ReapAction(
			long duration, List<Reason> reasons,
			JenkinsMaster.RunningBuild runningBuild) {

			_duration = duration;
			_reasons = reasons;
			_runningBuild = runningBuild;
		}

		private String _getOutcome() {
			if (_dryRun) {
				return "Not aborted (DRY_RUN).";
			}

			if (_executed) {
				return "Aborted.";
			}

			return "Abort failed.";
		}

		private final long _duration;
		private boolean _executed;
		private final List<Reason> _reasons;
		private final JenkinsMaster.RunningBuild _runningBuild;

	}

}