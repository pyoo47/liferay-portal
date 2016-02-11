/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.jenkins.results.parser;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Kevin Yen
 */
public class BaseJobTest {

	@Test
	public void testFormatBuildURL() {
		String expected =
			"https://test-1-1.liferay.com/job/" +
				"test-portal-acceptance-pullrequest(master)/100";

		String actual = BaseJob.formatBuildURL(
			"https://test-1-1.liferay.com" +
				"//job///test-portal-acceptance-pullrequest(master)/100/");

		Assert.assertEquals(expected, actual);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFormatBuildURLException() {
		BaseJob.formatBuildURL(
			"https://test-1-1.liferay.com//" +
				"job///test-portal-acceptance-pullrequest(master)");
	}

	@Test
	public void testGetBuildURL() {
		BaseJob baseJob = new BaseJob(_JOB_NAME, _MASTER_URL, _BUILD_NUMBER);

		Assert.assertEquals(baseJob.getBuildURL(), _BUILD_URL);
	}

	@Test(expected = IllegalStateException.class)
	public void testGetBuildURLException() {
		BaseJob baseJob = new BaseJob(_JOB_NAME, _MASTER_URL);

		baseJob.getBuildURL();
	}

	@Test
	public void testGetJobURL() {
		BaseJob baseJob = new BaseJob(_JOB_NAME, _MASTER_URL);

		Assert.assertEquals(baseJob.getJobURL(), _JOB_URL);

		baseJob = new BaseJob(_JOB_NAME, _MASTER_URL, _BUILD_NUMBER);

		Assert.assertEquals(baseJob.getJobURL(), _JOB_URL);
	}

	@Test
	public void testGetStatus() {
		BaseJob baseJob = new BaseJob(_JOB_NAME, _MASTER_URL);

		Assert.assertTrue(baseJob.getStatus().equals("starting"));

		baseJob = new BaseJob(_JOB_NAME, _MASTER_URL, _BUILD_NUMBER);

		Assert.assertTrue(baseJob.getStatus().equals("running"));

		baseJob.setCompleted("SUCCESS");

		Assert.assertTrue(baseJob.getStatus().equals("completed"));
	}

	@Test
	public void testSetBuildURL() {
		BaseJob baseJob = new BaseJob(_JOB_NAME, _MASTER_URL);

		baseJob.setBuildURL(_BUILD_URL);

		Assert.assertTrue(baseJob.getStatus().equals("running"));
		Assert.assertEquals(baseJob.getBuildURL(), _BUILD_URL);
	}

	private static final int _BUILD_NUMBER = 100;

	private static final String _BUILD_URL =
		"https://test-1-1.liferay.com/job/" +
			"test-portal-acceptance-pullrequest(master)/100";

	private static final String _JOB_NAME =
		"test-portal-acceptance-pullrequest(master)";

	private static final String _JOB_URL =
		"https://test-1-1.liferay.com/job/" +
			"test-portal-acceptance-pullrequest(master)";

	private static final String _MASTER_URL = "https://test-1-1.liferay.com";

}