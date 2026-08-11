/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser;

import java.net.URL;

import org.junit.Test;

/**
 * @author Calum Ragan
 */
public class URLCompareUtilTest
	extends com.liferay.jenkins.results.parser.Test {

	@Test
	public void testMatches() throws Exception {
		testEquals(
			true,
			URLCompareUtil.matches(
				new URL("http://test-1-1/job/x/1/"),
				new URL("http://test-1-1/job/x/1")));
		testEquals(
			false,
			URLCompareUtil.matches(
				new URL("http://test-1-1/job/x/1/"),
				new URL("http://test-1-1/job/y/1/")));
	}

	@Test
	public void testMatchesWithEmptyParameterValue() throws Exception {
		testEquals(
			true,
			URLCompareUtil.matches(
				new URL("http://test-1-1/job/x?AXIS_VARIABLE=&JOB=y"),
				new URL("http://test-1-1/job/x?AXIS_VARIABLE=&JOB=y")));
		testEquals(
			false,
			URLCompareUtil.matches(
				new URL("http://test-1-1/job/x?AXIS_VARIABLE=&JOB=y"),
				new URL("http://test-1-1/job/x?AXIS_VARIABLE=1&JOB=y")));
	}

	@Test
	public void testMatchesWithEquivalentParameterSpellings() throws Exception {
		testEquals(
			true,
			URLCompareUtil.matches(
				new URL("http://test-1-1/job/x?V=AWS%20%26%20CI"),
				new URL("http://test-1-1/job/x?V=AWS+%26+CI")));
	}

	@Test
	public void testMatchesWithReorderedParameters() throws Exception {
		testEquals(
			true,
			URLCompareUtil.matches(
				new URL("http://test-1-1/job/x?A=1&B=2"),
				new URL("http://test-1-1/job/x?B=2&A=1")));
	}

}