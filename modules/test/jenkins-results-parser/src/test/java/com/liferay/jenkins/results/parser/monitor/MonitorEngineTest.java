/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.monitor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * @author Brittney Nguyen
 */
public class MonitorEngineTest extends com.liferay.jenkins.results.parser.Test {

	@Test(timeout = 10000)
	public void testRunCycle() {
		MonitorResultStore monitorResultStore = new MonitorResultStore();

		TestMonitor hangingTestMonitor = new TestMonitor(
			_newMonitorConfig("a", 1)) {

			@Override
			public MonitorResult execute() {
				try {
					Thread.sleep(10000);
				}
				catch (InterruptedException interruptedException) {
					Thread thread = Thread.currentThread();

					thread.interrupt();
				}

				return null;
			}

		};

		TestMonitor okTestMonitor = new TestMonitor(_newMonitorConfig("b", 0));

		TestMonitor throwingTestMonitor = new TestMonitor(
			_newMonitorConfig("c", 0)) {

			@Override
			public MonitorResult execute() {
				throw new RuntimeException("boom");
			}

		};

		MonitorEngine monitorEngine = new MonitorEngine(
			monitorResultStore,
			Arrays.<Monitor>asList(
				hangingTestMonitor, okTestMonitor, throwingTestMonitor));

		Map<Monitor, MonitorResult> monitorResultsMap = monitorEngine.runCycle(
			1000000L);

		testEquals(3, monitorResultsMap.size());

		MonitorResult latestMonitorResult =
			monitorResultStore.getLatestMonitorResult("a");

		testEquals(
			MonitorResult.Status.UNKNOWN, latestMonitorResult.getStatus());
		testEquals(
			"Monitor a timed out after 1000 ms",
			latestMonitorResult.getMessage());
		testEquals(1000000L, latestMonitorResult.getTimestamp());

		latestMonitorResult = monitorResultStore.getLatestMonitorResult("b");

		testEquals(MonitorResult.Status.OK, latestMonitorResult.getStatus());
		testEquals(1000000L, latestMonitorResult.getTimestamp());

		latestMonitorResult = monitorResultStore.getLatestMonitorResult("c");

		testEquals(
			MonitorResult.Status.UNKNOWN, latestMonitorResult.getStatus());
		testEquals("Monitor c failed: boom", latestMonitorResult.getMessage());

		monitorResultsMap = monitorEngine.runCycle(1000000L);

		testEquals(0, monitorResultsMap.size());

		List<MonitorResult> monitorResults =
			monitorResultStore.getMonitorResults("b");

		testEquals(1, monitorResults.size());

		monitorResultsMap = monitorEngine.runCycle(1900000L);

		testEquals(3, monitorResultsMap.size());

		monitorResults = monitorResultStore.getMonitorResults("b");

		testEquals(2, monitorResults.size());
	}

	private MonitorConfig _newMonitorConfig(String id, long timeout) {
		return new MonitorConfig(
			900, id, null, MonitorConfig.Severity.MEDIUM, null, timeout,
			"test");
	}

}