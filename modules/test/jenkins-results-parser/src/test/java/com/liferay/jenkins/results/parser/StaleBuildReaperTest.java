/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Calum Ragan
 */
public class StaleBuildReaperTest
	extends com.liferay.jenkins.results.parser.Test {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		Environment.setInstance(Mockito.mock(Environment.class));

		_urlReader = mockUrlReader();

		JenkinsMasterTestUtil.getJenkinsCohortProperties("test-9", 2);

		_setUpJenkinsMaster("test-9-1", _getTest91ComputerAPIJSONObject());
		_setUpJenkinsMaster("test-9-2", _getTest92ComputerAPIJSONObject());

		_jenkinsCohort = JenkinsCohort.getInstance("test-9");
	}

	@After
	@Override
	public void tearDown() {
		super.tearDown();

		JenkinsMasterTestUtil.resetCaches();

		JenkinsResultsParserUtil.setBuildProperties(new Properties());
	}

	@Test
	public void testGetSummary() {
		StaleBuildReaper staleBuildReaper = _reap(
			true, new ArrayList<String>());

		String summary = staleBuildReaper.getSummary();

		Assert.assertTrue(
			summary, summary.startsWith("Found 3 stale build(s)"));

		Assert.assertTrue(summary, summary.contains(_LIKELY_STUCK_BUILD_URL));
		Assert.assertTrue(summary, summary.contains(_NODE_REMOVED_BUILD_URL));
		Assert.assertTrue(summary, summary.contains(_PAST_ESTIMATE_BUILD_URL));

		Assert.assertFalse(summary, summary.contains(_HEALTHY_BUILD_URL));
		Assert.assertFalse(summary, summary.contains(_RECENT_BUILD_URL));

		Assert.assertTrue(
			summary, summary.contains("its executor reports likelyStuck"));
		Assert.assertTrue(
			summary, summary.contains("its node is being removed"));
		Assert.assertTrue(
			summary, summary.contains("it is running far past its estimate"));
	}

	@Test
	public void testReap() {
		List<String> stoppedBuildURLs = new ArrayList<>();

		StaleBuildReaper staleBuildReaper = _reap(false, stoppedBuildURLs);

		Assert.assertEquals(3, staleBuildReaper.getReapedBuildCount());

		String summary = staleBuildReaper.getSummary();

		Assert.assertTrue(
			summary, summary.startsWith("Reaped 3 of 3 stale build(s)"));

		List<String> expectedBuildURLs = new ArrayList<>();

		expectedBuildURLs.add(_LIKELY_STUCK_BUILD_URL);
		expectedBuildURLs.add(_NODE_REMOVED_BUILD_URL);
		expectedBuildURLs.add(_PAST_ESTIMATE_BUILD_URL);

		Collections.sort(expectedBuildURLs);

		Collections.sort(stoppedBuildURLs);

		Assert.assertEquals(expectedBuildURLs, stoppedBuildURLs);
	}

	@Test
	public void testReapBlacklistedJenkinsMaster() {
		ReflectionTestUtil.setFieldValue(
			JenkinsMaster.getInstance("test-9-2"), "_blacklisted", true);

		List<JenkinsMaster> availableJenkinsMasters =
			_jenkinsCohort.getAvailableJenkinsMasters();

		Assert.assertEquals(
			availableJenkinsMasters.toString(), 1,
			availableJenkinsMasters.size());

		List<JenkinsMaster> blacklistedJenkinsMasters =
			_jenkinsCohort.getBlacklistedJenkinsMasters();

		Assert.assertEquals(
			blacklistedJenkinsMasters.toString(), 1,
			blacklistedJenkinsMasters.size());

		List<String> stoppedBuildURLs = new ArrayList<>();

		StaleBuildReaper staleBuildReaper = _reap(false, stoppedBuildURLs);

		Assert.assertEquals(3, staleBuildReaper.getReapedBuildCount());

		Assert.assertTrue(
			stoppedBuildURLs.toString(),
			stoppedBuildURLs.contains(_PAST_ESTIMATE_BUILD_URL));
	}

	@Test
	public void testReapDryRun() {
		List<String> stoppedBuildURLs = new ArrayList<>();

		StaleBuildReaper staleBuildReaper = _reap(true, stoppedBuildURLs);

		Assert.assertEquals(0, staleBuildReaper.getReapedBuildCount());

		Assert.assertTrue(
			stoppedBuildURLs.toString(), stoppedBuildURLs.isEmpty());
	}

	private JSONObject _getTest91ComputerAPIJSONObject() {
		return JenkinsMasterTestUtil.getComputerAPIJSONObject(
			1,
			JenkinsMasterTestUtil.getBuiltInComputerJSONObject(
				JenkinsMasterTestUtil.getExecutorJSONObject(
					_HEALTHY_BUILD_URL, "test-portal-release-downstream #1", 1,
					false, _timestampAgo(20 * _HOUR), 30 * _HOUR)),
			JenkinsMasterTestUtil.getComputerJSONObject(
				"test-9-1-1",
				JenkinsMasterTestUtil.getExecutorJSONObject(
					_LIKELY_STUCK_BUILD_URL,
					"test-portal-acceptance-pullrequest(master) #1580", 1580,
					true, _timestampAgo(20 * _HOUR), 100 * _MINUTE)),
			JenkinsMasterTestUtil.getOfflineComputerJSONObject(
				"test-9-1-2", "Node is being removed",
				JenkinsMasterTestUtil.getExecutorJSONObject(
					_NODE_REMOVED_BUILD_URL,
					"test-portal-release-downstream #22649", 22649, false,
					_timestampAgo(12 * 24 * _HOUR), 8 * _HOUR)));
	}

	private JSONObject _getTest92ComputerAPIJSONObject() {
		return JenkinsMasterTestUtil.getComputerAPIJSONObject(
			2,
			JenkinsMasterTestUtil.getComputerJSONObject(
				"test-9-2-1",
				JenkinsMasterTestUtil.getExecutorJSONObject(
					_PAST_ESTIMATE_BUILD_URL,
					"test-portal-testsuite-downstream #166636", 166636, false,
					_timestampAgo(46 * _HOUR), 4 * _HOUR),
				JenkinsMasterTestUtil.getExecutorJSONObject(
					_RECENT_BUILD_URL, "test-portal-source-format(master) #99",
					99, false, _timestampAgo(2 * _HOUR), 5 * _MINUTE)));
	}

	private StaleBuildReaper _reap(
		boolean dryRun, List<String> stoppedBuildURLs) {

		try (MockedStatic<JenkinsStopBuildUtil> stopBuildMockedStatic =
				Mockito.mockStatic(JenkinsStopBuildUtil.class);
			MockedStatic<NotificationUtil> notificationMockedStatic =
				Mockito.mockStatic(NotificationUtil.class)) {

			stopBuildMockedStatic.when(
				() -> JenkinsStopBuildUtil.stopBuild(Mockito.anyString())
			).thenAnswer(
				invocation -> {
					stoppedBuildURLs.add(invocation.getArgument(0));

					return null;
				}
			);

			StaleBuildReaper staleBuildReaper = new StaleBuildReaper(
				_jenkinsCohort, dryRun);

			staleBuildReaper.reap();

			return staleBuildReaper;
		}
	}

	private void _setUpJenkinsMaster(
			String masterName, JSONObject computerAPIJSONObject)
		throws Exception {

		String masterURL = "http://" + masterName;

		JSONObject queueJSONObject = new JSONObject();

		queueJSONObject.put("items", new JSONArray());

		setUrlReaderOutput(
			queueJSONObject.toString(), masterURL + "/queue/api/json",
			_urlReader);

		JSONObject modeJSONObject = new JSONObject();

		modeJSONObject.put("mode", "NORMAL");

		setUrlReaderOutput(
			modeJSONObject.toString(), masterURL + "/api/json?tree=mode",
			_urlReader);

		setUrlReaderOutput(
			computerAPIJSONObject.toString(), masterURL + "/computer/api/json",
			_urlReader);
	}

	private long _timestampAgo(long duration) {
		return System.currentTimeMillis() - duration;
	}

	private static final String _HEALTHY_BUILD_URL =
		"http://test-9-1/job/test-portal-release-downstream/1/";

	private static final long _HOUR = 60 * 60 * 1000L;

	private static final String _LIKELY_STUCK_BUILD_URL =
		"http://test-9-1/job/test-portal-acceptance-pullrequest(master)/1580/";

	private static final long _MINUTE = 60 * 1000L;

	private static final String _NODE_REMOVED_BUILD_URL =
		"http://test-9-1/job/test-portal-release-downstream/22649/";

	private static final String _PAST_ESTIMATE_BUILD_URL =
		"http://test-9-2/job/test-portal-testsuite-downstream/166636/";

	private static final String _RECENT_BUILD_URL =
		"http://test-9-2/job/test-portal-source-format(master)/99/";

	private JenkinsCohort _jenkinsCohort;
	private UrlReader _urlReader;

}