/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.monitor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

/**
 * @author Brittney Nguyen
 */
public class MonitorSchedulerTest
	extends com.liferay.jenkins.results.parser.Test {

	@Test
	public void testGetDueMonitors() {
		MonitorResultStore monitorResultStore = new MonitorResultStore();

		MonitorScheduler monitorScheduler = new MonitorScheduler(
			monitorResultStore);

		List<Monitor> monitors = Arrays.<Monitor>asList(
			new TestMonitor(_newMonitorConfig(900, "a")));

		testEquals(
			monitors, monitorScheduler.getDueMonitors(monitors, 1000000L));

		monitorResultStore.store("a", _newMonitorResult(1000000L));

		testEquals(
			Collections.emptyList(),
			monitorScheduler.getDueMonitors(monitors, 1899000L));
		testEquals(
			monitors, monitorScheduler.getDueMonitors(monitors, 1900000L));
	}

	@Test
	public void testGetDueMonitorsZeroCadence() {
		MonitorResultStore monitorResultStore = new MonitorResultStore();

		MonitorScheduler monitorScheduler = new MonitorScheduler(
			monitorResultStore);

		List<Monitor> monitors = Arrays.<Monitor>asList(
			new TestMonitor(_newMonitorConfig(0, "a")));

		monitorResultStore.store("a", _newMonitorResult(1000000L));

		testEquals(
			monitors, monitorScheduler.getDueMonitors(monitors, 1000000L));
	}

	private MonitorConfig _newMonitorConfig(long cadence, String id) {
		return new MonitorConfig(
			cadence, id, null, MonitorConfig.Severity.MEDIUM, null, 60, "test");
	}

	private MonitorResult _newMonitorResult(long timestamp) {
		return new MonitorResult(
			"ok", null, MonitorResult.Status.OK, timestamp);
	}

}