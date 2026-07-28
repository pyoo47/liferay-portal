/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser;

import java.net.URL;

import java.util.Date;

import org.json.JSONObject;

import org.junit.Assert;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Calum Ragan
 */
public class ClientCredentialsHTTPAuthorizationTest
	extends com.liferay.jenkins.results.parser.Test {

	@Test
	public void testInvalidateToken() throws Exception {
		try (MockedStatic<JenkinsResultsParserUtil>
				jenkinsResultsParserUtilMockedStatic = _mockTokenRequest()) {

			JenkinsResultsParserUtil.ClientCredentialsHTTPAuthorization
				clientCredentialsHTTPAuthorization =
					_newClientCredentialsHTTPAuthorization();

			clientCredentialsHTTPAuthorization.toString();

			clientCredentialsHTTPAuthorization.invalidateToken();

			clientCredentialsHTTPAuthorization.toString();

			jenkinsResultsParserUtilMockedStatic.verify(
				() -> JenkinsResultsParserUtil.toJSONObject(
					Mockito.anyString(), Mockito.anyString()),
				Mockito.times(2));
		}
	}

	@Test
	public void testToStringCachesToken() throws Exception {
		try (MockedStatic<JenkinsResultsParserUtil>
				jenkinsResultsParserUtilMockedStatic = _mockTokenRequest()) {

			JenkinsResultsParserUtil.ClientCredentialsHTTPAuthorization
				clientCredentialsHTTPAuthorization =
					_newClientCredentialsHTTPAuthorization();

			Assert.assertEquals(
				"Bearer test-token",
				clientCredentialsHTTPAuthorization.toString());
			Assert.assertEquals(
				"Bearer test-token",
				clientCredentialsHTTPAuthorization.toString());

			jenkinsResultsParserUtilMockedStatic.verify(
				() -> JenkinsResultsParserUtil.toJSONObject(
					Mockito.anyString(), Mockito.anyString()));
		}
	}

	@Test
	public void testToStringRefreshesExpiredToken() throws Exception {
		try (MockedStatic<JenkinsResultsParserUtil>
				jenkinsResultsParserUtilMockedStatic = _mockTokenRequest()) {

			JenkinsResultsParserUtil.ClientCredentialsHTTPAuthorization
				clientCredentialsHTTPAuthorization =
					_newClientCredentialsHTTPAuthorization();

			clientCredentialsHTTPAuthorization.toString();

			ReflectionTestUtil.setFieldValue(
				clientCredentialsHTTPAuthorization, "_tokenExpirationDate",
				new Date(System.currentTimeMillis() - 1000L));

			clientCredentialsHTTPAuthorization.toString();

			jenkinsResultsParserUtilMockedStatic.verify(
				() -> JenkinsResultsParserUtil.toJSONObject(
					Mockito.anyString(), Mockito.anyString()),
				Mockito.times(2));
		}
	}

	private MockedStatic<JenkinsResultsParserUtil> _mockTokenRequest() {
		MockedStatic<JenkinsResultsParserUtil>
			jenkinsResultsParserUtilMockedStatic = Mockito.mockStatic(
				JenkinsResultsParserUtil.class);

		JSONObject jsonObject = new JSONObject();

		jsonObject.put(
			"access_token", "test-token"
		).put(
			"expires_in", 600
		).put(
			"token_type", "Bearer"
		);

		jenkinsResultsParserUtilMockedStatic.when(
			() -> JenkinsResultsParserUtil.toJSONObject(
				Mockito.anyString(), Mockito.anyString())
		).thenReturn(
			jsonObject
		);

		return jenkinsResultsParserUtilMockedStatic;
	}

	private JenkinsResultsParserUtil.ClientCredentialsHTTPAuthorization
			_newClientCredentialsHTTPAuthorization()
		throws Exception {

		return new JenkinsResultsParserUtil.ClientCredentialsHTTPAuthorization(
			"test-client-id", "test-client-secret",
			new URL("https://testray.liferay.com/o/oauth2/token"));
	}

}