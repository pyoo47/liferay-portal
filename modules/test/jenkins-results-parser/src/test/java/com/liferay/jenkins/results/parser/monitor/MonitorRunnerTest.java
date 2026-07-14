/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.monitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Brittney Nguyen
 */
public class MonitorRunnerTest extends com.liferay.jenkins.results.parser.Test {

	@Test(timeout = 5000)
	public void testRun() {
		MonitorRunner monitorRunner = new MonitorRunner();

		TestMonitor testMonitor1 = new TestMonitor(_newMonitorConfig("a"));
		TestMonitor testMonitor2 = new TestMonitor(_newMonitorConfig("b"));

		List<Monitor> monitors = Arrays.<Monitor>asList(
			testMonitor1, testMonitor2);

		Map<Monitor, MonitorResult> monitorResultsMap = monitorRunner.run(
			1L, monitors);

		testEquals(monitors, new ArrayList<>(monitorResultsMap.keySet()));

		MonitorResult monitorResult = monitorResultsMap.get(testMonitor1);

		testEquals(MonitorResult.Status.OK, monitorResult.getStatus());
		testEquals(1L, monitorResult.getTimestamp());

		monitorResult = monitorResultsMap.get(testMonitor2);

		testEquals(MonitorResult.Status.OK, monitorResult.getStatus());
		testEquals(1L, monitorResult.getTimestamp());
	}

	@Test(timeout = 5000)
	public void testRunConcurrentMonitors() {
		MonitorRunner monitorRunner = new MonitorRunner(60 * 1000, 2);

		CountDownLatch countDownLatch = new CountDownLatch(2);

		TestMonitor testMonitor1 = new ConcurrentTestMonitor(
			countDownLatch, _newMonitorConfig("a"));
		TestMonitor testMonitor2 = new ConcurrentTestMonitor(
			countDownLatch, _newMonitorConfig("b"));

		Map<Monitor, MonitorResult> monitorResultsMap = monitorRunner.run(
			1L, Arrays.<Monitor>asList(testMonitor1, testMonitor2));

		MonitorResult monitorResult = monitorResultsMap.get(testMonitor1);

		testEquals(MonitorResult.Status.OK, monitorResult.getStatus());

		monitorResult = monitorResultsMap.get(testMonitor2);

		testEquals(MonitorResult.Status.OK, monitorResult.getStatus());
	}

	@Test(timeout = 5000)
	public void testRunDuplicateIds() {
		MonitorRunner monitorRunner = new MonitorRunner();

		Map<Monitor, MonitorResult> monitorResultsMap = monitorRunner.run(
			1L,
			Arrays.<Monitor>asList(
				new TestMonitor(_newMonitorConfig("a")),
				new TestMonitor(_newMonitorConfig("a"))));

		testEquals(2, monitorResultsMap.size());
	}

	@Test(timeout = 5000)
	public void testRunEmpty() {
		MonitorRunner monitorRunner = new MonitorRunner();

		Map<Monitor, MonitorResult> monitorResultsMap = monitorRunner.run(
			1L, Collections.<Monitor>emptyList());

		Assert.assertTrue(monitorResultsMap.isEmpty());

		monitorResultsMap = monitorRunner.run(1L, null);

		Assert.assertTrue(monitorResultsMap.isEmpty());
	}

	@Test(timeout = 5000)
	public void testRunHangingMonitor() {
		MonitorRunner monitorRunner = new MonitorRunner(100, 2);

		TestMonitor testMonitor = new HangingTestMonitor(
			null, _newMonitorConfig("a"));

		Map<Monitor, MonitorResult> monitorResultsMap = monitorRunner.run(
			1L, Collections.<Monitor>singletonList(testMonitor));

		MonitorResult monitorResult = monitorResultsMap.get(testMonitor);

		testEquals(MonitorResult.Status.UNKNOWN, monitorResult.getStatus());
		testEquals(
			"Monitor a timed out after 100 ms", monitorResult.getMessage());
	}

	@Test(timeout = 5000)
	public void testRunHangingMonitorCancellation() throws Exception {
		MonitorRunner monitorRunner = new MonitorRunner(100, 1);

		CountDownLatch countDownLatch = new CountDownLatch(1);

		monitorRunner.run(
			1L,
			Collections.<Monitor>singletonList(
				new HangingTestMonitor(
					countDownLatch, _newMonitorConfig("a"))));

		Assert.assertTrue(countDownLatch.await(1, TimeUnit.SECONDS));
	}

	@Test(timeout = 5000)
	public void testRunHangingMonitorWithPassingMonitors() {
		MonitorRunner monitorRunner = new MonitorRunner(100, 2);

		TestMonitor testMonitor1 = new HangingTestMonitor(
			null, _newMonitorConfig("a"));
		TestMonitor testMonitor2 = new TestMonitor(_newMonitorConfig("b"));
		TestMonitor testMonitor3 = new TestMonitor(_newMonitorConfig("c"));

		long startTimestamp = System.currentTimeMillis();

		Map<Monitor, MonitorResult> monitorResultsMap = monitorRunner.run(
			1L,
			Arrays.<Monitor>asList(testMonitor1, testMonitor2, testMonitor3));

		Assert.assertTrue((System.currentTimeMillis() - startTimestamp) < 2000);

		MonitorResult monitorResult = monitorResultsMap.get(testMonitor1);

		testEquals(MonitorResult.Status.UNKNOWN, monitorResult.getStatus());

		monitorResult = monitorResultsMap.get(testMonitor2);

		testEquals(MonitorResult.Status.OK, monitorResult.getStatus());

		monitorResult = monitorResultsMap.get(testMonitor3);

		testEquals(MonitorResult.Status.OK, monitorResult.getStatus());
	}

	@Test(timeout = 5000)
	public void testRunMonitorConfigTimeout() {
		MonitorRunner monitorRunner = new MonitorRunner();

		TestMonitor testMonitor = new HangingTestMonitor(
			null, _newMonitorConfig("a", 1));

		long startTimestamp = System.currentTimeMillis();

		Map<Monitor, MonitorResult> monitorResultsMap = monitorRunner.run(
			1L, Collections.<Monitor>singletonList(testMonitor));

		Assert.assertTrue((System.currentTimeMillis() - startTimestamp) < 3000);

		MonitorResult monitorResult = monitorResultsMap.get(testMonitor);

		testEquals(MonitorResult.Status.UNKNOWN, monitorResult.getStatus());
		testEquals(
			"Monitor a timed out after 1000 ms", monitorResult.getMessage());
	}

	@Test(timeout = 5000)
	public void testRunMultipleHangingMonitors() {
		MonitorRunner monitorRunner = new MonitorRunner(200, 4);

		List<Monitor> monitors = Arrays.<Monitor>asList(
			new HangingTestMonitor(null, _newMonitorConfig("a")),
			new HangingTestMonitor(null, _newMonitorConfig("b")),
			new HangingTestMonitor(null, _newMonitorConfig("c")),
			new HangingTestMonitor(null, _newMonitorConfig("d")));

		long startTimestamp = System.currentTimeMillis();

		Map<Monitor, MonitorResult> monitorResultsMap = monitorRunner.run(
			1L, monitors);

		Assert.assertTrue((System.currentTimeMillis() - startTimestamp) < 600);

		for (MonitorResult monitorResult : monitorResultsMap.values()) {
			testEquals(MonitorResult.Status.UNKNOWN, monitorResult.getStatus());
		}
	}

	@Test(timeout = 5000)
	public void testRunNullResultMonitor() {
		MonitorRunner monitorRunner = new MonitorRunner();

		TestMonitor testMonitor = new TestMonitor(_newMonitorConfig("a")) {

			@Override
			public MonitorResult execute() {
				return null;
			}

		};

		Map<Monitor, MonitorResult> monitorResultsMap = monitorRunner.run(
			1L, Collections.<Monitor>singletonList(testMonitor));

		MonitorResult monitorResult = monitorResultsMap.get(testMonitor);

		testEquals(MonitorResult.Status.UNKNOWN, monitorResult.getStatus());
		testEquals("Monitor a returned no result", monitorResult.getMessage());
	}

	@Test(timeout = 5000)
	public void testRunThrowingMonitor() {
		MonitorRunner monitorRunner = new MonitorRunner();

		TestMonitor testMonitor1 = new TestMonitor(_newMonitorConfig("a")) {

			@Override
			public MonitorResult execute() {
				throw new RuntimeException("Unable to execute the monitor");
			}

		};

		TestMonitor testMonitor2 = new TestMonitor(_newMonitorConfig("b"));

		Map<Monitor, MonitorResult> monitorResultsMap = monitorRunner.run(
			1L, Arrays.<Monitor>asList(testMonitor1, testMonitor2));

		MonitorResult monitorResult = monitorResultsMap.get(testMonitor1);

		testEquals(MonitorResult.Status.UNKNOWN, monitorResult.getStatus());
		testEquals(
			"Monitor a failed: Unable to execute the monitor",
			monitorResult.getMessage());

		monitorResult = monitorResultsMap.get(testMonitor2);

		testEquals(MonitorResult.Status.OK, monitorResult.getStatus());
	}

	@Test(timeout = 5000)
	public void testRunThrowingMonitorWithoutMessage() {
		MonitorRunner monitorRunner = new MonitorRunner();

		TestMonitor testMonitor = new TestMonitor(_newMonitorConfig("a")) {

			@Override
			public MonitorResult execute() {
				throw new RuntimeException();
			}

		};

		Map<Monitor, MonitorResult> monitorResultsMap = monitorRunner.run(
			1L, Collections.<Monitor>singletonList(testMonitor));

		MonitorResult monitorResult = monitorResultsMap.get(testMonitor);

		testEquals(MonitorResult.Status.UNKNOWN, monitorResult.getStatus());
		testEquals(
			"Monitor a failed: java.lang.RuntimeException",
			monitorResult.getMessage());
	}

	private MonitorConfig _newMonitorConfig(String id) {
		return _newMonitorConfig(id, 0);
	}

	private MonitorConfig _newMonitorConfig(String id, long timeout) {
		return new MonitorConfig(
			0, id, null, MonitorConfig.Severity.MEDIUM, null, timeout, "test");
	}

	private static class ConcurrentTestMonitor extends TestMonitor {

		public ConcurrentTestMonitor(
			CountDownLatch countDownLatch, MonitorConfig monitorConfig) {

			super(monitorConfig);

			_countDownLatch = countDownLatch;
		}

		@Override
		public MonitorResult execute() {
			_countDownLatch.countDown();

			try {
				if (!_countDownLatch.await(2, TimeUnit.SECONDS)) {
					throw new IllegalStateException(
						"Monitors did not run concurrently");
				}
			}
			catch (InterruptedException interruptedException) {
				throw new RuntimeException(interruptedException);
			}

			return super.execute();
		}

		private final CountDownLatch _countDownLatch;

	}

	private static class HangingTestMonitor extends TestMonitor {

		public HangingTestMonitor(
			CountDownLatch countDownLatch, MonitorConfig monitorConfig) {

			super(monitorConfig);

			_countDownLatch = countDownLatch;
		}

		@Override
		public MonitorResult execute() {
			try {
				Thread.sleep(10000);
			}
			catch (InterruptedException interruptedException) {
				if (_countDownLatch != null) {
					_countDownLatch.countDown();
				}

				Thread thread = Thread.currentThread();

				thread.interrupt();
			}

			return null;
		}

		private final CountDownLatch _countDownLatch;

	}

}