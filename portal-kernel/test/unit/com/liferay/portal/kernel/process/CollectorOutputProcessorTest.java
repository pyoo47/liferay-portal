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

package com.liferay.portal.kernel.process;

import com.liferay.portal.kernel.io.unsync.UnsyncByteArrayInputStream;
import com.liferay.portal.kernel.test.rule.CodeCoverageAssertor;

import java.util.Random;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import org.junit.Rule;
import org.junit.rules.TestRule;
import com.liferay.portal.kernel.test.rule.TimeoutTestRule;


/**
 * @author Shuyang Zhou
 */
public class CollectorOutputProcessorTest extends BaseOutputProcessorTestCase {

	@ClassRule
	public static final CodeCoverageAssertor codeCoverageAssertor =
		CodeCoverageAssertor.INSTANCE;

	@Rule
	public final TestRule testRule = TimeoutTestRule.INSTANCE;

	@Test
	public void testCollectFail() {
		try {
			Thread.sleep(11000);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		testFailToRead(new CollectorOutputProcessor());
	}

	@Test
	public void testCollectSuccess() throws ProcessException {
		try {
			Thread.sleep(11000);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		CollectorOutputProcessor collectorOutputProcessor =
			new CollectorOutputProcessor();

		Random random = new Random();

		byte[] stdErrData = new byte[1024];

		random.nextBytes(stdErrData);

		Assert.assertArrayEquals(
			stdErrData,
			collectorOutputProcessor.processStdErr(
				new UnsyncByteArrayInputStream(stdErrData)));

		byte[] stdOutData = new byte[1024];

		random.nextBytes(stdOutData);

		Assert.assertArrayEquals(
			stdOutData,
			collectorOutputProcessor.processStdErr(
				new UnsyncByteArrayInputStream(stdOutData)));
	}

}