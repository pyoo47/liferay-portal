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

		_setUpJenkinsMasterUrlReaderOutputs(
			_getStaleBuildsComputerAPIJSONObject(), "test-9-1");
		_setUpJenkinsMasterUrlReaderOutputs(
			_getJenkinsSlaveOfflineComputerAPIJSONObject(), "test-9-2");

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

		Assert.assertEquals(3, staleBuildReaper.getStaleBuildCount());

		Assert.assertTrue(summary, summary.contains(_LIKELY_STUCK_BUILD_URL));
		Assert.assertTrue(summary, summary.contains(_NODE_REMOVED_BUILD_URL));
		Assert.assertTrue(summary, summary.contains(_OFFLINE_BUILD_URL));

		Assert.assertFalse(summary, summary.contains(_HEALTHY_BUILD_URL));
		Assert.assertFalse(summary, summary.contains(_RECONNECTING_BUILD_URL));
		Assert.assertFalse(
			summary, summary.contains(_TEMPORARILY_OFFLINE_BUILD_URL));

		Assert.assertTrue(
			summary, summary.contains("its executor reports likelyStuck"));
		Assert.assertTrue(
			summary, summary.contains("its node is being removed"));
		Assert.assertTrue(
			summary, summary.contains("its node has been offline"));
	}

	@Test
	public void testReap() {
		List<String> stoppedBuildURLs = new ArrayList<>();

		StaleBuildReaper staleBuildReaper = _reap(false, stoppedBuildURLs);

		Assert.assertEquals(3, staleBuildReaper.getReapedBuildCount());

		Assert.assertEquals(3, staleBuildReaper.getStaleBuildCount());

		List<String> expectedBuildURLs = new ArrayList<>();

		expectedBuildURLs.add(_LIKELY_STUCK_BUILD_URL);
		expectedBuildURLs.add(_NODE_REMOVED_BUILD_URL);
		expectedBuildURLs.add(_OFFLINE_BUILD_URL);

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
			stoppedBuildURLs.contains(_OFFLINE_BUILD_URL));
	}

	@Test
	public void testReapDryRun() {
		List<String> stoppedBuildURLs = new ArrayList<>();

		StaleBuildReaper staleBuildReaper = _reap(true, stoppedBuildURLs);

		Assert.assertEquals(0, staleBuildReaper.getReapedBuildCount());

		Assert.assertTrue(
			stoppedBuildURLs.toString(), stoppedBuildURLs.isEmpty());
	}

	private JSONObject _getJenkinsSlaveOfflineComputerAPIJSONObject() {
		return JenkinsMasterTestUtil.getComputerAPIJSONObject(
			2,
			JenkinsMasterTestUtil.getOfflineComputerJSONObject(
				"test-9-2-1", _getStartTime(30 * _MINUTE),
				"Connection was broken", false,
				JenkinsMasterTestUtil.getExecutorJSONObject(
					_OFFLINE_BUILD_URL, 4 * _HOUR,
					RandomTestUtil.randomString(), false,
					_getStartTime(46 * _HOUR))),
			JenkinsMasterTestUtil.getOfflineComputerJSONObject(
				"test-9-2-2", _getStartTime(2 * _MINUTE),
				"Connection was broken", false,
				JenkinsMasterTestUtil.getExecutorJSONObject(
					_RECONNECTING_BUILD_URL, 4 * _HOUR,
					RandomTestUtil.randomString(), false,
					_getStartTime(46 * _HOUR))),
			JenkinsMasterTestUtil.getOfflineComputerJSONObject(
				"test-9-2-3", _getStartTime(30 * _HOUR), "Disk is full", true,
				JenkinsMasterTestUtil.getExecutorJSONObject(
					_TEMPORARILY_OFFLINE_BUILD_URL, 4 * _HOUR,
					RandomTestUtil.randomString(), false,
					_getStartTime(46 * _HOUR))));
	}

	private JSONObject _getStaleBuildsComputerAPIJSONObject() {
		return JenkinsMasterTestUtil.getComputerAPIJSONObject(
			7,
			JenkinsMasterTestUtil.getBuiltInComputerJSONObject(
				JenkinsMasterTestUtil.getExecutorJSONObject(
					_HEALTHY_BUILD_URL, 30 * _HOUR,
					RandomTestUtil.randomString(), false,
					_getStartTime(20 * _HOUR))),
			JenkinsMasterTestUtil.getComputerJSONObject(
				"test-9-1-1",
				JenkinsMasterTestUtil.getExecutorJSONObject(
					_LIKELY_STUCK_BUILD_URL, 100 * _MINUTE,
					RandomTestUtil.randomString(), true,
					_getStartTime(20 * _HOUR))),
			JenkinsMasterTestUtil.getOfflineComputerJSONObject(
				"test-9-1-2", _getStartTime(12 * 24 * _HOUR),
				"Node is being removed", false,
				JenkinsMasterTestUtil.getExecutorJSONObject(
					_NODE_REMOVED_BUILD_URL, 8 * _HOUR,
					RandomTestUtil.randomString(), false,
					_getStartTime(12 * 24 * _HOUR))));
	}

	private long _getStartTime(long duration) {
		return JenkinsResultsParserUtil.getCurrentTimeMillis() - duration;
	}

	private StaleBuildReaper _reap(
		boolean dryRun, List<String> stoppedBuildURLs) {

		try (MockedStatic<JenkinsStopBuildUtil> stopBuildMockedStatic =
				Mockito.mockStatic(JenkinsStopBuildUtil.class);
			MockedStatic<NotificationUtil> notificationMockedStatic =
				Mockito.mockStatic(NotificationUtil.class)) {

			stopBuildMockedStatic.when(
				() -> JenkinsStopBuildUtil.abortBuild(Mockito.anyString())
			).thenAnswer(
				invocation -> {
					stoppedBuildURLs.add(invocation.getArgument(0));

					return null;
				}
			);

			StaleBuildReaper staleBuildReaper = new StaleBuildReaper(
				dryRun, _jenkinsCohort);

			staleBuildReaper.reap();

			return staleBuildReaper;
		}
	}

	private void _setUpJenkinsMasterUrlReaderOutputs(
			JSONObject computerAPIJSONObject, String masterName)
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

	private static final String _HEALTHY_BUILD_URL =
		"http://test-9-1/job/test-portal-release-downstream/1/";

	private static final long _HOUR = 60 * 60 * 1000L;

	private static final String _LIKELY_STUCK_BUILD_URL =
		"http://test-9-1/job/test-portal-acceptance-pullrequest(master)/1580/";

	private static final long _MINUTE = 60 * 1000L;

	private static final String _NODE_REMOVED_BUILD_URL =
		"http://test-9-1/job/test-portal-release-downstream/22649/";

	private static final String _OFFLINE_BUILD_URL =
		"http://test-9-2/job/test-portal-testsuite-downstream/166636/";

	private static final String _RECONNECTING_BUILD_URL =
		"http://test-9-2/job/test-portal-source-format(master)/99/";

	private static final String _TEMPORARILY_OFFLINE_BUILD_URL =
		"http://test-9-2/job/test-portal-acceptance-pullrequest(master)/42/";

	private JenkinsCohort _jenkinsCohort;
	private UrlReader _urlReader;

}