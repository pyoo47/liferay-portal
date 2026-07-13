/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.monitor;

import com.liferay.jenkins.results.parser.JenkinsResultsParserUtil;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Brittney Nguyen
 */
public class MonitorRunner {

	public MonitorRunner() {
		this(60 * 1000, 4);
	}

	public MonitorRunner(long defaultTimeoutMillis, int threadCount) {
		if (defaultTimeoutMillis < 1) {
			throw new IllegalArgumentException(
				"Invalid default timeout: " + defaultTimeoutMillis);
		}

		if (threadCount < 1) {
			throw new IllegalArgumentException(
				"Invalid thread count: " + threadCount);
		}

		_defaultTimeoutMillis = defaultTimeoutMillis;
		_threadCount = threadCount;
	}

	public Map<Monitor, MonitorResult> run(
		Collection<Monitor> monitors, long timestamp) {

		Map<Monitor, MonitorResult> monitorResultsMap = new LinkedHashMap<>();

		if ((monitors == null) || monitors.isEmpty()) {
			return monitorResultsMap;
		}

		ExecutorService executorService = _newExecutorService();

		try {
			long startTimestamp = System.currentTimeMillis();

			Map<Monitor, Future<MonitorResult>> futuresMap =
				new LinkedHashMap<>();

			for (final Monitor monitor : monitors) {
				futuresMap.put(
					monitor,
					executorService.submit(
						new Callable<MonitorResult>() {

							@Override
							public MonitorResult call() {
								return monitor.execute();
							}

						}));
			}

			for (Map.Entry<Monitor, Future<MonitorResult>> entry :
					futuresMap.entrySet()) {

				Monitor monitor = entry.getKey();

				monitorResultsMap.put(
					monitor,
					_getMonitorResult(
						entry.getValue(), monitor, startTimestamp, timestamp));
			}
		}
		finally {
			executorService.shutdownNow();
		}

		return monitorResultsMap;
	}

	private MonitorResult _getMonitorResult(
		Future<MonitorResult> future, Monitor monitor, long startTimestamp,
		long timestamp) {

		long timeoutMillis = _getTimeoutMillis(monitor);

		long remainingMillis =
			startTimestamp + timeoutMillis - System.currentTimeMillis();

		if (remainingMillis < 0) {
			remainingMillis = 0;
		}

		try {
			MonitorResult monitorResult = future.get(
				remainingMillis, TimeUnit.MILLISECONDS);

			if (monitorResult == null) {
				return _newUnknownMonitorResult(
					JenkinsResultsParserUtil.combine(
						"Monitor ", monitor.getId(), " returned no result"),
					timestamp);
			}

			return new MonitorResult(
				monitorResult.getMessage(), monitorResult.getMetrics(),
				monitorResult.getStatus(), timestamp);
		}
		catch (ExecutionException executionException) {
			Throwable throwable = executionException.getCause();

			String message = throwable.getMessage();

			if (message == null) {
				Class<?> clazz = throwable.getClass();

				message = clazz.getName();
			}

			return _newUnknownMonitorResult(
				JenkinsResultsParserUtil.combine(
					"Monitor ", monitor.getId(), " failed: ", message),
				timestamp);
		}
		catch (InterruptedException interruptedException) {
			Thread thread = Thread.currentThread();

			thread.interrupt();

			future.cancel(true);

			return _newUnknownMonitorResult(
				JenkinsResultsParserUtil.combine(
					"Monitor ", monitor.getId(), " was interrupted"),
				timestamp);
		}
		catch (TimeoutException timeoutException) {
			future.cancel(true);

			return _newUnknownMonitorResult(
				JenkinsResultsParserUtil.combine(
					"Monitor ", monitor.getId(), " timed out after ",
					String.valueOf(timeoutMillis), " ms"),
				timestamp);
		}
	}

	private long _getTimeoutMillis(Monitor monitor) {
		MonitorConfig monitorConfig = monitor.getMonitorConfig();

		long timeout = monitorConfig.getTimeout();

		if (timeout <= 0) {
			return _defaultTimeoutMillis;
		}

		return timeout * 1000;
	}

	private ExecutorService _newExecutorService() {
		return Executors.newFixedThreadPool(
			_threadCount,
			new ThreadFactory() {

				@Override
				public Thread newThread(Runnable runnable) {
					Thread thread = new Thread(
						runnable,
						"monitor-runner-" + _threadNumber.getAndIncrement());

					thread.setDaemon(true);

					return thread;
				}

				private final AtomicInteger _threadNumber = new AtomicInteger(
					1);

			});
	}

	private MonitorResult _newUnknownMonitorResult(
		String message, long timestamp) {

		return new MonitorResult(
			message, null, MonitorResult.Status.UNKNOWN, timestamp);
	}

	private final long _defaultTimeoutMillis;
	private final int _threadCount;

}