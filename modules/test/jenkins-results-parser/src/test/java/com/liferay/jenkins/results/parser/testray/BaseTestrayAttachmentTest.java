/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.testray;

import com.liferay.jenkins.results.parser.JenkinsResultsParserUtil;

import java.net.URL;

import org.json.JSONObject;

import org.junit.Test;

/**
 * @author Calum Ragan
 */
public class BaseTestrayAttachmentTest
	extends com.liferay.jenkins.results.parser.Test {

	@Test
	public void testGetJSONObject() throws Exception {
		String key = "testray-results/production/1/poshi-log.txt.gz";

		String url = JenkinsResultsParserUtil.combine(
			"https://storage.cloud.google.com/", key, "?authuser=0");

		TestrayAttachment testrayAttachment = new TestTestrayAttachment(
			key, new URL(url));

		JSONObject jsonObject = testrayAttachment.getJSONObject();

		testEquals(key, jsonObject.getString("value"));
		testEquals(url, jsonObject.getString("url"));
	}

	@Test
	public void testGetJSONObjectWithKeyHoldingReservedCharacters()
		throws Exception {

		String key = "testray-results/production/1/PortalSmoke#Smoke/a b.gz";

		TestrayAttachment testrayAttachment = new TestTestrayAttachment(
			key, new URL("https://storage.cloud.google.com/testray-results"));

		JSONObject jsonObject = testrayAttachment.getJSONObject();

		testEquals(key, jsonObject.getString("value"));
	}

	private static class TestTestrayAttachment extends BaseTestrayAttachment {

		private TestTestrayAttachment(String key, URL url) {
			super(null, "Poshi Log", key, url);
		}

	}

}