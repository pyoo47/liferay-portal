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

	public String getSummary() {
		StringBuilder sb = new StringBuilder();

		if (_dryRun) {
			sb.append("Found ");
			sb.append(_reapActions.size());
			sb.append(" stale build(s). No build was aborted because DRY_RUN ");
			sb.append("is enabled.");
		}
		else {
			sb.append("Reaped ");
			sb.append(getReapedBuildCount());
			sb.append(" of ");
			sb.append(_reapActions.size());
			sb.append(" stale build(s).");
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

		System.out.println(getSummary());

		_sendSlackNotification();
	}

	public static enum Reason {

		LIKELY_STUCK("its executor reports likelyStuck"),
		NODE_BEING_REMOVED("its node is being removed"),
		NODE_OFFLINE("its node is offline"),
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
		for (JenkinsMaster jenkinsMaster : _getJenkinsMasters()) {
			jenkinsMaster.update(false);

			List<JenkinsMaster.RunningBuild> runningBuilds =
				jenkinsMaster.getRunningBuilds();

			System.out.println(
				JenkinsResultsParserUtil.combine(
					jenkinsMaster.getName(), " has at most ",
					String.valueOf(jenkinsMaster.getMaximumRunningBuildCount()),
					" running build(s). Enumerated ",
					String.valueOf(runningBuilds.size()), "."));

			for (JenkinsMaster.RunningBuild runningBuild : runningBuilds) {
				List<Reason> reasons = _getReasons(runningBuild);

				if (reasons.isEmpty()) {
					continue;
				}

				_reapActions.add(new ReapAction(runningBuild, reasons));
			}
		}
	}

	private List<JenkinsMaster> _getJenkinsMasters() {
		List<JenkinsMaster> jenkinsMasters = new ArrayList<>(
			_jenkinsCohort.getAvailableJenkinsMasters());

		for (JenkinsMaster jenkinsMaster :
				_jenkinsCohort.getBlacklistedJenkinsMasters()) {

			if (!jenkinsMasters.contains(jenkinsMaster)) {
				jenkinsMasters.add(jenkinsMaster);
			}
		}

		return jenkinsMasters;
	}

	private List<Reason> _getReasons(JenkinsMaster.RunningBuild runningBuild) {
		List<Reason> reasons = new ArrayList<>();

		if (!runningBuild.isBuilding()) {
			return reasons;
		}

		long elapsedDuration = runningBuild.getElapsedDuration();

		if (elapsedDuration < _MINIMUM_ELAPSED_DURATION) {
			return reasons;
		}

		if (runningBuild.isLikelyStuck()) {
			reasons.add(Reason.LIKELY_STUCK);
		}

		if (runningBuild.isJenkinsSlaveOffline()) {
			String offlineCauseReason = runningBuild.getOfflineCauseReason();

			if ((offlineCauseReason != null) &&
				offlineCauseReason.contains(_OFFLINE_CAUSE_REASON_REMOVED)) {

				reasons.add(Reason.NODE_BEING_REMOVED);
			}
			else {
				reasons.add(Reason.NODE_OFFLINE);
			}
		}

		long estimatedDuration = runningBuild.getEstimatedDuration();

		if (estimatedDuration > 0) {
			long thresholdDuration =
				estimatedDuration * _ESTIMATED_DURATION_MULTIPLIER;

			if (elapsedDuration > thresholdDuration) {
				reasons.add(Reason.PAST_ESTIMATED_DURATION);
			}
		}

		return reasons;
	}

	private void _sendSlackNotification() {
		if (_reapActions.isEmpty()) {
			return;
		}

		String subject = "Stale builds reaped";

		if (_dryRun) {
			subject = "Stale builds detected";
		}

		NotificationUtil.sendSlackNotification(
			getSummary(), _SLACK_CHANNEL_NAME, subject);
	}

	private static final int _ESTIMATED_DURATION_MULTIPLIER = 10;

	private static final long _MINIMUM_ELAPSED_DURATION = 4 * 60 * 60 * 1000L;

	private static final String _OFFLINE_CAUSE_REASON_REMOVED =
		"is being removed";

	private static final String _SLACK_CHANNEL_NAME = "ci-notifications";

	private final boolean _dryRun;
	private final JenkinsCohort _jenkinsCohort;
	private final List<ReapAction> _reapActions = new ArrayList<>();

	private class ReapAction {

		public void execute() {
			String buildURL = _runningBuild.getURL();

			try {
				JenkinsStopBuildUtil.stopBuild(buildURL);

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
			sb.append(
				JenkinsResultsParserUtil.toDurationString(
					_runningBuild.getElapsedDuration()));

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

			if (_dryRun) {
				sb.append("Not aborted (DRY_RUN). ");
			}
			else if (_executed) {
				sb.append("Aborted. ");
			}
			else {
				sb.append("Abort failed. ");
			}

			sb.append(_runningBuild.getURL());

			return sb.toString();
		}

		public boolean isExecuted() {
			return _executed;
		}

		private ReapAction(
			JenkinsMaster.RunningBuild runningBuild, List<Reason> reasons) {

			_runningBuild = runningBuild;
			_reasons = reasons;
		}

		private boolean _executed;
		private final List<Reason> _reasons;
		private final JenkinsMaster.RunningBuild _runningBuild;

	}

}