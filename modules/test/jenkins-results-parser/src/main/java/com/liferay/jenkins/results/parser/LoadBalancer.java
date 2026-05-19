/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser;

import java.io.StringReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Brittney Nguyen
 */
public class LoadBalancer {

	public List<JenkinsMaster> getEligibleJenkinsMasters(
		String masterPrefix, String blacklistString, Properties properties,
		boolean verbose) {

		List<JenkinsMaster> allJenkinsMasters =
			_jenkinsMastersMap.computeIfAbsent(
				masterPrefix,
				key -> JenkinsResultsParserUtil.getJenkinsMasters(
					properties, JenkinsMaster.getSlaveRAMMinimumDefault(),
					JenkinsMaster.getSlavesPerHostDefault(), key));

		List<String> blacklist = _getBlacklist(
			properties, blacklistString, verbose);

		List<JenkinsMaster> eligibleJenkinsMasters = new ArrayList<>(
			allJenkinsMasters.size());

		for (JenkinsMaster jenkinsMaster : allJenkinsMasters) {
			if (blacklist.contains(jenkinsMaster.getName())) {
				continue;
			}

			eligibleJenkinsMasters.add(jenkinsMaster);
		}

		return eligibleJenkinsMasters;
	}

	public String selectMasterURL(Properties properties, boolean verbose) {
		String baseInvocationURL = JenkinsResultsParserUtil.getProperty(
			properties, "base.invocation.url");

		String masterPrefix = LoadBalancerUtil.getMasterPrefix(
			baseInvocationURL);

		if (masterPrefix.equals(baseInvocationURL)) {
			return baseInvocationURL;
		}

		String blacklistString = JenkinsResultsParserUtil.getProperty(
			properties, "blacklist");

		List<JenkinsMaster> eligibleJenkinsMasters = getEligibleJenkinsMasters(
			masterPrefix, blacklistString, properties, verbose);

		if (eligibleJenkinsMasters.isEmpty()) {
			return null;
		}

		AtomicInteger counter = _roundRobinCounters.computeIfAbsent(
			masterPrefix, key -> new AtomicInteger());

		int index = Math.floorMod(
			counter.getAndIncrement(), eligibleJenkinsMasters.size());

		JenkinsMaster selectedJenkinsMaster = eligibleJenkinsMasters.get(index);

		if (verbose) {
			StringBuilder sb = new StringBuilder();

			sb.append("Selected master ");
			sb.append(selectedJenkinsMaster.getName());
			sb.append(" via round-robin (");
			sb.append(eligibleJenkinsMasters.size());
			sb.append(" eligible masters under prefix ");
			sb.append(masterPrefix);
			sb.append(")");

			System.out.println(sb.toString());
		}

		return "http://" + selectedJenkinsMaster.getName();
	}

	public String selectMasterURL(String... overridePropertiesArray)
		throws Exception {

		return selectMasterURL(null, overridePropertiesArray, true);
	}

	public String selectMasterURL(
			String propertiesURL, String[] overridePropertiesArray,
			boolean verbose)
		throws Exception {

		Properties properties;

		if (propertiesURL == null) {
			properties = JenkinsResultsParserUtil.getBuildProperties(false);
		}
		else {
			properties = new Properties();

			String propertiesString = JenkinsResultsParserUtil.toString(
				JenkinsResultsParserUtil.getLocalURL(propertiesURL), false,
				true);

			properties.load(new StringReader(propertiesString));
		}

		if ((overridePropertiesArray != null) &&
			(overridePropertiesArray.length > 0) &&
			((overridePropertiesArray.length % 2) == 0)) {

			for (int i = 0; i < overridePropertiesArray.length; i += 2) {
				String overridePropertyValue = overridePropertiesArray[i + 1];

				if (overridePropertyValue == null) {
					continue;
				}

				String overridePropertyName = overridePropertiesArray[i];

				properties.setProperty(
					overridePropertyName, overridePropertyValue);
			}
		}

		return selectMasterURL(properties, verbose);
	}

	private List<String> _getBlacklist(
		Properties properties, String requestBlacklistString, boolean verbose) {

		List<String> blacklist = new ArrayList<>();

		String propertyBlacklistString = properties.getProperty(
			"jenkins.load.balancer.blacklist", "");

		for (String blacklistItem : propertyBlacklistString.split(",")) {
			blacklistItem = blacklistItem.trim();

			if (!blacklistItem.isEmpty()) {
				blacklist.add(blacklistItem);
			}
		}

		if ((requestBlacklistString != null) &&
			!requestBlacklistString.isEmpty()) {

			String[] requestBlacklistItems = requestBlacklistString.toLowerCase(
			).split(
				"\\s*,\\s*"
			);

			for (String blacklistItem : requestBlacklistItems) {
				if (!blacklist.contains(blacklistItem)) {
					blacklist.add(blacklistItem);
				}
			}
		}

		if (verbose) {
			System.out.println("Blacklist: " + blacklist);
		}

		return blacklist;
	}

	private final Map<String, List<JenkinsMaster>> _jenkinsMastersMap =
		new ConcurrentHashMap<>();
	private final Map<String, AtomicInteger> _roundRobinCounters =
		new ConcurrentHashMap<>();

}