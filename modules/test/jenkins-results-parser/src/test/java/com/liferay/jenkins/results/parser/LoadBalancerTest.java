/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Brittney Nguyen
 */
public class LoadBalancerTest {

	@Before
	public void setUp() {
		Hashtable<Object, Object> buildProperties = new Hashtable<>();

		for (int i = 1; i <= 3; i++) {
			String masterName = _COHORT_NAME + "-" + i;

			buildProperties.put(
				"master.property(" + masterName + "/executors.size)", "10");
			buildProperties.put(
				"jenkins.local.url[" + masterName + "]",
				"http://" + masterName + ".example.invalid");
			buildProperties.put(
				"jenkins.remote.url[" + masterName + "]",
				"https://" + masterName + ".example.invalid");
		}

		buildProperties.put("slave.ram.minimum.default", "1");
		buildProperties.put("slaves.per.host.default", "20");

		JenkinsResultsParserUtil.setBuildProperties(buildProperties);

		_buildProperties = new Properties();

		_buildProperties.putAll(buildProperties);

		_buildProperties.setProperty(
			"base.invocation.url", "http://" + _COHORT_NAME + ".liferay.com");
	}

	@Test
	public void testAllBlacklisted() {
		_buildProperties.setProperty(
			"jenkins.load.balancer.blacklist",
			_COHORT_NAME + "-1," + _COHORT_NAME + "-2," + _COHORT_NAME + "-3");

		LoadBalancer loadBalancer = new LoadBalancer();

		Assert.assertNull(
			loadBalancer.selectMasterURL(_buildProperties, false));
	}

	@Test
	public void testInstancesAreIsolated() {
		LoadBalancer loadBalancerA = new LoadBalancer();
		LoadBalancer loadBalancerB = new LoadBalancer();

		String firstFromA = loadBalancerA.selectMasterURL(
			_buildProperties, false);

		loadBalancerA.selectMasterURL(_buildProperties, false);
		loadBalancerA.selectMasterURL(_buildProperties, false);

		String firstFromB = loadBalancerB.selectMasterURL(
			_buildProperties, false);

		Assert.assertEquals(firstFromA, firstFromB);
	}

	@Test
	public void testPropertyBlacklistExcludesMaster() {
		_buildProperties.setProperty(
			"jenkins.load.balancer.blacklist", _COHORT_NAME + "-2");

		Set<String> selectedURLs = _collectURLs(new LoadBalancer(), 6);

		Assert.assertEquals(selectedURLs.toString(), 2, selectedURLs.size());
		Assert.assertFalse(
			selectedURLs.contains("http://" + _COHORT_NAME + "-2"));
	}

	@Test
	public void testRequestBlacklistExcludesMaster() {
		_buildProperties.setProperty("blacklist", _COHORT_NAME + "-1");

		Set<String> selectedURLs = _collectURLs(new LoadBalancer(), 6);

		Assert.assertEquals(selectedURLs.toString(), 2, selectedURLs.size());
		Assert.assertFalse(
			selectedURLs.contains("http://" + _COHORT_NAME + "-1"));
	}

	@Test
	public void testRoundRobinCyclesThroughAllEligibleMasters() {
		Set<String> selectedURLs = _collectURLs(new LoadBalancer(), 9);

		Assert.assertEquals(selectedURLs.toString(), 3, selectedURLs.size());
		Assert.assertTrue(
			selectedURLs.contains("http://" + _COHORT_NAME + "-1"));
		Assert.assertTrue(
			selectedURLs.contains("http://" + _COHORT_NAME + "-2"));
		Assert.assertTrue(
			selectedURLs.contains("http://" + _COHORT_NAME + "-3"));
	}

	@Test
	public void testRoundRobinDoesNotRepeatBeforeCycleEnds() {
		LoadBalancer loadBalancer = new LoadBalancer();

		Set<String> firstCycle = new HashSet<>();

		for (int i = 0; i < 3; i++) {
			firstCycle.add(
				loadBalancer.selectMasterURL(_buildProperties, false));
		}

		Assert.assertEquals(
			"Expected 3 distinct masters in the first cycle", 3,
			firstCycle.size());
	}

	private Set<String> _collectURLs(LoadBalancer loadBalancer, int count) {
		Set<String> selectedURLs = new HashSet<>();

		for (int i = 0; i < count; i++) {
			selectedURLs.add(
				loadBalancer.selectMasterURL(_buildProperties, false));
		}

		return selectedURLs;
	}

	private static final String _COHORT_NAME = "lrci7532-0";

	private Properties _buildProperties;

}