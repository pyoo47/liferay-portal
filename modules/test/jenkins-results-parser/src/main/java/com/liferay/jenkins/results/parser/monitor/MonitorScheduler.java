/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.monitor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Brittney Nguyen
 */
public class MonitorScheduler {

	public MonitorScheduler(MonitorResultStore monitorResultStore) {
		_monitorResultStore = monitorResultStore;
	}

	public List<Monitor> getDueMonitors(
		List<Monitor> monitors, long timestamp) {

		List<Monitor> dueMonitors = new ArrayList<>();

		for (Monitor monitor : monitors) {
			if (_isDue(monitor, timestamp)) {
				dueMonitors.add(monitor);
			}
		}

		return dueMonitors;
	}

	private boolean _isDue(Monitor monitor, long timestamp) {
		MonitorResult latestMonitorResult =
			_monitorResultStore.getLatestMonitorResult(monitor.getId());

		if (latestMonitorResult == null) {
			return true;
		}

		MonitorConfig monitorConfig = monitor.getMonitorConfig();

		if ((timestamp - latestMonitorResult.getTimestamp()) >=
				(monitorConfig.getCadence() * 1000)) {

			return true;
		}

		return false;
	}

	private final MonitorResultStore _monitorResultStore;

}