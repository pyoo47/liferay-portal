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
public class DownstreamJobTest {

	@Test
	public void testSetCompleted() {
		DownstreamJob downstreamJob = new DownstreamJob(
			_JOB_NAME, _JOB_VARIANT, _MASTER_URL, _INVOCATION_URL, _BUILD_URL);

		Assert.assertFalse(downstreamJob.completed());

		downstreamJob.setCompleted(true);

		Assert.assertTrue(downstreamJob.completed());
	}

	private static final String _BUILD_URL =
		"https://test-1-1.liferay.com/job/" +
			"test-portal-acceptance-pullrequest(master)/100";

	private static final String _INVOCATION_URL =
		"https://test-1-1.liferay.com/job/" +
			"test-portal-acceptance-pullrequest-batch(master)/" +
				"buildWithParameters";

	private static final String _JOB_NAME =
		"test-portal-acceptance-pullrequest(master)";

	private static final String _JOB_URL =
		"https://test-1-1.liferay.com/job/" +
			"test-portal-acceptance-pullrequest(master)";

	private static final String _JOB_VARIANT = "functional-tomcat-mysql";

	private static final String _MASTER_URL = "https://test-1-1.liferay.com";

}