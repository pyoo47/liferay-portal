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
public class TopLevelJobTest {

	@Test
	public void testGetBuildURL() {
		TopLevelJob topLevelJob = new TopLevelJob(_BUILD_URL);

		Assert.assertEquals(topLevelJob.getBuildURL(), _BUILD_URL);
	}

	@Test
	public void testGetJobURL() {
		TopLevelJob topLevelJob = new TopLevelJob(_BUILD_URL);

		Assert.assertEquals(topLevelJob.getJobURL(), _JOB_URL);
	}

	@Test
	public void testGetStatus() {
		TopLevelJob topLevelJob = new TopLevelJob(_BUILD_URL);

		Assert.assertTrue(topLevelJob.getStatus().equals("running"));

		topLevelJob.setCompleted("SUCCESS");

		Assert.assertTrue(topLevelJob.getStatus().equals("completed"));
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