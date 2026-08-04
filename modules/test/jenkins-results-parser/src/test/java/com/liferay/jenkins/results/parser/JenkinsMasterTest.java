/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser;

import java.io.File;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Calum Ragan
 */
public class JenkinsMasterTest extends com.liferay.jenkins.results.parser.Test {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		Environment.setInstance(Mockito.mock(Environment.class));

		UrlReader urlReader = mockUrlReader();

		setUrlReaderOutput(
			new JSONObject(
			).put(
				"items", new JSONArray()
			).toString(),
			"http://test-9-1/queue/api/json", urlReader);
		setUrlReaderOutput(
			new JSONObject(
			).put(
				"mode", "NORMAL"
			).toString(),
			"http://test-9-1/api/json?tree=mode", urlReader);
		setUrlReaderOutput(
			read(new File(dependenciesDirs.get(0), "computer-api.json")),
			"http://test-9-1/computer/api/json", urlReader);

		setUrlReaderOutput(
			new JSONObject(
			).put(
				"items", new JSONArray()
			).toString(),
			"http://test-9-2/queue/api/json", urlReader);
		setUrlReaderOutput(
			new JSONObject(
			).put(
				"mode", "NORMAL"
			).toString(),
			"http://test-9-2/api/json?tree=mode", urlReader);
		setUrlReaderOutput(
			_getRunningBuildsComputerAPIJSONObject().toString(),
			"http://test-9-2/computer/api/json", urlReader);

		_jenkinsMaster = JenkinsMasterTestUtil.getJenkinsMaster(
			"test-9-1", "http://test-9-1");
	}

	@After
	@Override
	public void tearDown() {
		super.tearDown();

		JenkinsMaster.maxRecentBatchAge = 120 * 1000;

		JenkinsMasterTestUtil.resetCaches();
	}

	@Test
	public void testGetAvailableSlavesCount() {
		int availableSlavesCount = _jenkinsMaster.getAvailableSlavesCount(null);

		JenkinsMaster.maxRecentBatchAge = -1;

		String label = RandomTestUtil.randomString();

		_jenkinsMaster.addRecentBatch(7, label);

		Map<String, Map<Long, Integer>> labelBatchSizes =
			JenkinsMasterTestUtil.getLabelBatchSizes(_jenkinsMaster);

		Map<Long, Integer> batchSizes = labelBatchSizes.get(label);

		Assert.assertEquals(batchSizes.toString(), 1, batchSizes.size());

		String otherLabel = RandomTestUtil.randomString();

		_jenkinsMaster.getAvailableSlavesCount(otherLabel);

		Assert.assertTrue(batchSizes.isEmpty());

		Assert.assertEquals(
			availableSlavesCount, _jenkinsMaster.getAvailableSlavesCount(null));
	}

	@Test
	public void testGetRunningBuilds() {
		JenkinsMaster jenkinsMaster = JenkinsMasterTestUtil.getJenkinsMaster(
			"test-9-2", "http://test-9-2");

		jenkinsMaster.update();

		List<JenkinsMaster.RunningBuild> runningBuilds =
			jenkinsMaster.getRunningBuilds();

		Assert.assertEquals(runningBuilds.toString(), 3, runningBuilds.size());

		Assert.assertEquals(3, jenkinsMaster.getMaximumRunningBuildCount());

		JenkinsMaster.RunningBuild flyweightRunningBuild = _getRunningBuild(
			runningBuilds, _FLYWEIGHT_BUILD_URL);

		Assert.assertEquals(
			"Built-In Node", flyweightRunningBuild.getJenkinsSlaveName());
		Assert.assertFalse(flyweightRunningBuild.isLikelyStuck());
		Assert.assertFalse(flyweightRunningBuild.isJenkinsSlaveOffline());

		JenkinsMaster.RunningBuild likelyStuckRunningBuild = _getRunningBuild(
			runningBuilds, _LIKELY_STUCK_BUILD_URL);

		Assert.assertTrue(likelyStuckRunningBuild.isLikelyStuck());
		Assert.assertFalse(likelyStuckRunningBuild.isJenkinsSlaveOffline());
		Assert.assertEquals(1580, likelyStuckRunningBuild.getNumber());
		Assert.assertEquals(
			60 * 60 * 1000L, likelyStuckRunningBuild.getEstimatedDuration());

		JenkinsMaster.RunningBuild offlineRunningBuild = _getRunningBuild(
			runningBuilds, _OFFLINE_NODE_BUILD_URL);

		Assert.assertTrue(offlineRunningBuild.isJenkinsSlaveOffline());
		Assert.assertEquals(
			"Node is being removed",
			offlineRunningBuild.getOfflineCauseReason());
	}

	@Test
	public void testUpdate() {
		_jenkinsMaster.update();

		int availableSlavesCount = _jenkinsMaster.getAvailableSlavesCount(null);

		_jenkinsMaster.addRecentBatch(5, null);

		Assert.assertEquals(
			availableSlavesCount - 5,
			_jenkinsMaster.getAvailableSlavesCount(null));

		ReflectionTestUtil.setFieldValue(
			_jenkinsMaster, "_updateTimestamp", -1L);

		_jenkinsMaster.update();

		Assert.assertEquals(
			availableSlavesCount - 5,
			_jenkinsMaster.getAvailableSlavesCount(null));

		Map<String, Map<Long, Integer>> labelBatchSizes =
			JenkinsMasterTestUtil.getLabelBatchSizes(_jenkinsMaster);

		Assert.assertFalse(labelBatchSizes.isEmpty());
	}

	private JenkinsMaster.RunningBuild _getRunningBuild(
		List<JenkinsMaster.RunningBuild> runningBuilds, String buildURL) {

		for (JenkinsMaster.RunningBuild runningBuild : runningBuilds) {
			String runningBuildURL = runningBuild.getURL();

			if (runningBuildURL.equals(buildURL)) {
				return runningBuild;
			}
		}

		throw new AssertionError(
			"Unable to find " + buildURL + " in " + runningBuilds);
	}

	private JSONObject _getRunningBuildsComputerAPIJSONObject() {
		return JenkinsMasterTestUtil.getComputerAPIJSONObject(
			1,
			JenkinsMasterTestUtil.getBuiltInComputerJSONObject(
				JenkinsMasterTestUtil.getExecutorJSONObject(
					_FLYWEIGHT_BUILD_URL, "publish-testray-report #7", 7, false,
					1700000000000L, 5 * 60 * 1000L)),
			JenkinsMasterTestUtil.getComputerJSONObject(
				"test-9-2-1",
				JenkinsMasterTestUtil.getExecutorJSONObject(
					_LIKELY_STUCK_BUILD_URL,
					"test-portal-acceptance-pullrequest(master) #1580", 1580,
					true, 1700000000000L, 60 * 60 * 1000L)),
			JenkinsMasterTestUtil.getOfflineComputerJSONObject(
				"test-9-2-2", "Node is being removed",
				JenkinsMasterTestUtil.getExecutorJSONObject(
					_OFFLINE_NODE_BUILD_URL,
					"test-portal-release-downstream #22649", 22649, false,
					1700000000000L, 8 * 60 * 60 * 1000L)));
	}

	private static final String _FLYWEIGHT_BUILD_URL =
		"http://test-9-2/job/publish-testray-report/7/";

	private static final String _LIKELY_STUCK_BUILD_URL =
		"http://test-9-2/job/test-portal-acceptance-pullrequest(master)/1580/";

	private static final String _OFFLINE_NODE_BUILD_URL =
		"http://test-9-2/job/test-portal-release-downstream/22649/";

	private JenkinsMaster _jenkinsMaster;

}