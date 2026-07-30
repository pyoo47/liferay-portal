/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.monitor;

import com.liferay.jenkins.results.parser.JenkinsResultsParserUtil;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Brittney Nguyen
 */
public class MonitorMetricsWriter {

	public MonitorMetricsWriter(
		File metricsFile, MonitorResultStore monitorResultStore,
		List<Monitor> monitors) {

		_metricsFile = metricsFile;
		_monitorResultStore = monitorResultStore;
		_monitors = monitors;
	}

	public void write() throws IOException {
		File temporaryFile = new File(
			_metricsFile.getParentFile(), _metricsFile.getName() + ".tmp");

		JenkinsResultsParserUtil.write(temporaryFile, _getContent());

		Files.move(
			temporaryFile.toPath(), _metricsFile.toPath(),
			StandardCopyOption.ATOMIC_MOVE);
	}

	private String _escapeLabelValue(String labelValue) {
		labelValue = labelValue.replace("\\", "\\\\");

		labelValue = labelValue.replace("\"", "\\\"");

		return labelValue.replace("\n", "\\n");
	}

	private String _getCheckLastRunTimestampLine(
		Monitor monitor, MonitorResult monitorResult) {

		return JenkinsResultsParserUtil.combine(
			"monitor_check_last_run_timestamp_seconds{", _getLabels(monitor),
			"} ", String.valueOf(_getLastRunTimestamp(monitorResult)));
	}

	private String _getCheckStatusLine(
		Monitor monitor, MonitorResult monitorResult) {

		return JenkinsResultsParserUtil.combine(
			"monitor_check_status{", _getLabels(monitor), "} ",
			String.valueOf(_getSeverityRank(monitorResult)));
	}

	private String _getContent() {
		Map<Monitor, MonitorResult> monitorResultsMap = new LinkedHashMap<>();

		for (Monitor monitor : _monitors) {
			monitorResultsMap.put(
				monitor,
				_monitorResultStore.getLatestMonitorResult(monitor.getId()));
		}

		StringBuilder sb = new StringBuilder();

		sb.append(
			"# HELP monitor_check_status Monitor status severity rank, 0 OK, " +
				"1 UNKNOWN, 2 WARN, 3 CRITICAL\n");
		sb.append("# TYPE monitor_check_status gauge\n");

		for (Map.Entry<Monitor, MonitorResult> entry :
				monitorResultsMap.entrySet()) {

			sb.append(_getCheckStatusLine(entry.getKey(), entry.getValue()));
			sb.append("\n");
		}

		sb.append(
			"# HELP monitor_check_last_run_timestamp_seconds Unix timestamp " +
				"of the last check run, 0 if never run\n");
		sb.append("# TYPE monitor_check_last_run_timestamp_seconds gauge\n");

		for (Map.Entry<Monitor, MonitorResult> entry :
				monitorResultsMap.entrySet()) {

			sb.append(
				_getCheckLastRunTimestampLine(
					entry.getKey(), entry.getValue()));
			sb.append("\n");
		}

		sb.append(
			"# HELP monitor_heartbeat_timestamp_seconds Unix timestamp of " +
				"the last completed monitor cycle\n");
		sb.append("# TYPE monitor_heartbeat_timestamp_seconds gauge\n");
		sb.append("monitor_heartbeat_timestamp_seconds ");
		sb.append(JenkinsResultsParserUtil.getCurrentTimeMillis() / 1000);
		sb.append("\n");

		return sb.toString();
	}

	private String _getLabels(Monitor monitor) {
		MonitorConfig monitorConfig = monitor.getMonitorConfig();

		MonitorConfig.Severity severity = monitorConfig.getSeverity();

		String severityName = severity.name();

		return JenkinsResultsParserUtil.combine(
			"check=\"", _escapeLabelValue(monitor.getId()), "\",severity=\"",
			severityName.toLowerCase(Locale.ENGLISH), "\",type=\"",
			_escapeLabelValue(monitorConfig.getType()), "\"");
	}

	private long _getLastRunTimestamp(MonitorResult monitorResult) {
		if (monitorResult == null) {
			return 0;
		}

		return monitorResult.getTimestamp() / 1000;
	}

	private int _getSeverityRank(MonitorResult monitorResult) {
		if (monitorResult == null) {
			return MonitorResult.Status.UNKNOWN.getSeverityRank();
		}

		MonitorResult.Status status = monitorResult.getStatus();

		if (status == null) {
			return MonitorResult.Status.UNKNOWN.getSeverityRank();
		}

		return status.getSeverityRank();
	}

	private final File _metricsFile;
	private final MonitorResultStore _monitorResultStore;
	private final List<Monitor> _monitors;

}