/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser;

import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Peter Yoo
 */
public class LoadBalancerUtil {

	public static List<JenkinsMaster> getAvailableJenkinsMasters(
		String masterPrefix, String blacklistString, Properties properties) {

		return _sharedLoadBalancer.getEligibleJenkinsMasters(
			masterPrefix, blacklistString, properties, true);
	}

	public static List<JenkinsMaster> getAvailableJenkinsMasters(
		String masterPrefix, String blacklistString, Properties properties,
		boolean verbose) {

		return _sharedLoadBalancer.getEligibleJenkinsMasters(
			masterPrefix, blacklistString, properties, verbose);
	}

	public static String getMasterPrefix(String baseInvocationURL) {
		Matcher matcher = _urlPattern.matcher(baseInvocationURL);

		if (!matcher.find()) {
			return baseInvocationURL;
		}

		return matcher.group("masterPrefix");
	}

	public static String getMostAvailableMasterURL(
			boolean verbose, String... overridePropertiesArray)
		throws Exception {

		return _sharedLoadBalancer.selectMasterURL(
			null, overridePropertiesArray, verbose);
	}

	public static String getMostAvailableMasterURL(Properties properties) {
		return _sharedLoadBalancer.selectMasterURL(properties, true);
	}

	public static String getMostAvailableMasterURL(
		Properties properties, boolean verbose) {

		return _sharedLoadBalancer.selectMasterURL(properties, verbose);
	}

	public static String getMostAvailableMasterURL(
			String... overridePropertiesArray)
		throws Exception {

		return _sharedLoadBalancer.selectMasterURL(
			null, overridePropertiesArray, true);
	}

	public static String getMostAvailableMasterURL(
			String propertiesURL, String[] overridePropertiesArray)
		throws Exception {

		return _sharedLoadBalancer.selectMasterURL(
			propertiesURL, overridePropertiesArray, true);
	}

	public static String getMostAvailableMasterURL(
			String propertiesURL, String[] overridePropertiesArray,
			boolean verbose)
		throws Exception {

		return _sharedLoadBalancer.selectMasterURL(
			propertiesURL, overridePropertiesArray, verbose);
	}

	private static final LoadBalancer _sharedLoadBalancer = new LoadBalancer();
	private static final Pattern _urlPattern = Pattern.compile(
		"http://(?<masterPrefix>.+-\\d?).liferay.com");

}