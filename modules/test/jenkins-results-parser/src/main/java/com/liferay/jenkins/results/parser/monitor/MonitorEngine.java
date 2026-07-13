/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.monitor;

import java.util.List;
import java.util.Map;

/**
 * @author Brittney Nguyen
 */
public class MonitorEngine {

	public MonitorEngine(
		MonitorResultStore monitorResultStore, List<Monitor> monitors) {

		_monitorResultStore = monitorResultStore;
		_monitors = monitors;

		_monitorScheduler = new MonitorScheduler(monitorResultStore);
	}

	public Map<Monitor, MonitorResult> runCycle(long timestamp) {
		Map<Monitor, MonitorResult> monitorResultsMap = _monitorRunner.run(
			_monitorScheduler.getDueMonitors(_monitors, timestamp), timestamp);

		for (Map.Entry<Monitor, MonitorResult> entry :
				monitorResultsMap.entrySet()) {

			Monitor monitor = entry.getKey();

			_monitorResultStore.store(monitor.getId(), entry.getValue());
		}

		return monitorResultsMap;
	}

	private final MonitorResultStore _monitorResultStore;
	private final MonitorRunner _monitorRunner = new MonitorRunner();
	private final List<Monitor> _monitors;
	private final MonitorScheduler _monitorScheduler;

}