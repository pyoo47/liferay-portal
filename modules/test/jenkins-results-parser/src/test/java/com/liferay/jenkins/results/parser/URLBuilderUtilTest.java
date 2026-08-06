/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

/**
 * @author Calum Ragan
 */
public class URLBuilderUtilTest
	extends com.liferay.jenkins.results.parser.Test {

	@Test
	public void testAppendParameter() {
		testEquals(
			"http://test-1-1/job/x?tree=result",
			URLBuilderUtil.appendParameter(
				"http://test-1-1/job/x", "tree", "result"));
	}

	@Test
	public void testAppendParameterWithExistingQueryString() {
		testEquals(
			"http://test-1-1/job/x?a=1&b=2",
			URLBuilderUtil.appendParameter(
				"http://test-1-1/job/x?a=1", "b", "2"));
	}

	@Test
	public void testAppendParameterWithFragment() {
		testEquals(
			"http://test-1-1/job/x?a=1&b=2#summary",
			URLBuilderUtil.appendParameter(
				"http://test-1-1/job/x?a=1#summary", "b", "2"));
	}

	@Test
	public void testBuildFormContent() {
		testEquals(
			"client_id=a%26b&grant_type=client_credentials",
			URLBuilderUtil.buildFormContent(
				_newParameters(
					"grant_type", "client_credentials", "client_id", "a&b")));
	}

	@Test
	public void testBuildFormContentWithoutParameters() {
		testEquals("", URLBuilderUtil.buildFormContent(Collections.emptyMap()));
	}

	@Test
	public void testBuildURL() {
		testEquals(
			JenkinsResultsParserUtil.combine(
				"http://test-1-1/job/x/buildWithParameters?",
				"JOB_VARIANT=functional&PARENT_BUILD_URL=",
				"http%3A%2F%2Ftest-1-1%2Fjob%2Fy%2F1%2F"),
			URLBuilderUtil.buildURL(
				"http://test-1-1/job/x/buildWithParameters",
				_newParameters(
					"PARENT_BUILD_URL", "http://test-1-1/job/y/1/",
					"JOB_VARIANT", "functional")));
	}

	@Test
	public void testBuildURLPreservesPath() {
		testEquals(
			JenkinsResultsParserUtil.combine(
				"http://test-1-1/job/test-portal-acceptance-pullrequest",
				"(master)/buildWithParameters?a=1"),
			URLBuilderUtil.buildURL(
				JenkinsResultsParserUtil.combine(
					"http://test-1-1/job/test-portal-acceptance-pullrequest",
					"(master)/buildWithParameters"),
				"a", "1"));
	}

	@Test
	public void testBuildURLSortsParameters() {
		testEquals(
			"http://test-1-1/job/x?alpha=1&beta=2&gamma=3",
			URLBuilderUtil.buildURL(
				"http://test-1-1/job/x",
				_newParameters("gamma", "3", "beta", "2", "alpha", "1")));
	}

	@Test
	public void testBuildURLWithEmptyValue() {
		testEquals(
			"http://test-1-1/job/x?AXIS_VARIABLE=",
			URLBuilderUtil.buildURL(
				"http://test-1-1/job/x", "AXIS_VARIABLE", ""));
	}

	@Test
	public void testBuildURLWithNullValue() {
		testEquals(
			"http://test-1-1/job/x/api/json?pretty",
			URLBuilderUtil.buildURL(
				"http://test-1-1/job/x/api/json", "pretty", null));
	}

	@Test
	public void testBuildURLWithoutParameters() {
		testEquals(
			"http://test-1-1/job/x/buildWithParameters",
			URLBuilderUtil.buildURL(
				"http://test-1-1/job/x/buildWithParameters",
				Collections.emptyMap()));
	}

	@Test
	public void testBuildURLWithReservedCharacters() {
		testEquals(
			"http://test-1-1/job/x?V=PortalSmoke%23Smoke",
			URLBuilderUtil.buildURL(
				"http://test-1-1/job/x", "V", "PortalSmoke#Smoke"));
		testEquals(
			"http://test-1-1/job/x?V=AWS+%26+CI",
			URLBuilderUtil.buildURL("http://test-1-1/job/x", "V", "AWS & CI"));
		testEquals(
			"http://test-1-1/job/x?V=100%25+pass",
			URLBuilderUtil.buildURL("http://test-1-1/job/x", "V", "100% pass"));
		testEquals(
			"http://test-1-1/job/x?V=a%3Db",
			URLBuilderUtil.buildURL("http://test-1-1/job/x", "V", "a=b"));
		testEquals(
			"http://test-1-1/job/x?V=a%2Bb",
			URLBuilderUtil.buildURL("http://test-1-1/job/x", "V", "a+b"));
		testEquals(
			"http://test-1-1/job/x?V=a+b",
			URLBuilderUtil.buildURL("http://test-1-1/job/x", "V", "a b"));
	}

	@Test
	public void testGetParameters() {
		Map<String, String> parameters = URLBuilderUtil.getParameters(
			JenkinsResultsParserUtil.combine(
				"http://test-1-1/job/x/buildWithParameters?",
				"JOB_VARIANT=functional&PARENT_BUILD_URL=",
				"http%3A%2F%2Ftest-1-1%2Fjob%2Fy%2F1%2F"));

		testEquals(2, parameters.size());
		testEquals("functional", parameters.get("JOB_VARIANT"));
		testEquals(
			"http://test-1-1/job/y/1/", parameters.get("PARENT_BUILD_URL"));
	}

	@Test
	public void testGetParametersWithInvalidEscape() {
		Map<String, String> parameters = URLBuilderUtil.getParameters(
			"http://test-1-1/job/x?PORTAL_BUILD_NOTES=100% pass");

		testEquals(1, parameters.size());
		testEquals("100% pass", parameters.get("PORTAL_BUILD_NOTES"));
	}

	@Test
	public void testGetParametersWithoutQueryString() {
		Map<String, String> parameters = URLBuilderUtil.getParameters(
			"http://test-1-1/job/x/1/");

		testEquals(0, parameters.size());
	}

	@Test
	public void testNormalizeURLIsIdempotent() {
		String normalizedURL = URLBuilderUtil.normalizeURL(
			"http://test-1-1/job/x?tree=actions[parameters[name,value]]");

		testEquals(normalizedURL, URLBuilderUtil.normalizeURL(normalizedURL));
	}

	@Test
	public void testNormalizeURLPreservesFragment() {
		testEquals(
			"http://test-1-1/job/x?a=1#summary",
			URLBuilderUtil.normalizeURL("http://test-1-1/job/x?a=1#summary"));
	}

	@Test
	public void testNormalizeURLPreservesLegalURL() {
		String url = JenkinsResultsParserUtil.combine(
			"http://test-1-1/job/test-portal-acceptance-pullrequest(master)/",
			"buildWithParameters?V=a%2Bb&W=AWS%20%26%20CI");

		testSame(url, URLBuilderUtil.normalizeURL(url));
	}

	@Test
	public void testNormalizeURLWithBarePercent() {
		testEquals(
			"http://test-1-1/job/x?V=100%25%20pass",
			URLBuilderUtil.normalizeURL("http://test-1-1/job/x?V=100% pass"));
	}

	@Test
	public void testNormalizeURLWithIllegalPathCharacters() {
		testEquals(
			"http://test-1-1/job/a%20b/1/",
			URLBuilderUtil.normalizeURL("http://test-1-1/job/a b/1/"));
		testEquals(
			"http://test-1-1/job/x?tree=result%5B0%5D",
			URLBuilderUtil.normalizeURL(
				"http://test-1-1/job/x?tree=result[0]"));
	}

	@Test
	public void testParseQueryStringWithEmptyAndValuelessParameters() {
		Map<String, String> parameters = URLBuilderUtil.parseQueryString(
			"AXIS_VARIABLE=&pretty");

		testEquals(2, parameters.size());
		testEquals("", parameters.get("AXIS_VARIABLE"));
		testEquals(null, parameters.get("pretty"));
	}

	@Test
	public void testParseQueryStringWithRepeatedName() {
		Map<String, String> parameters = URLBuilderUtil.parseQueryString(
			"V=1&V=2");

		testEquals(1, parameters.size());
		testEquals("1", parameters.get("V"));
	}

	@Test
	public void testParseQueryStringWithReservedCharacters() {
		Map<String, String> parameters = URLBuilderUtil.parseQueryString(
			JenkinsResultsParserUtil.combine(
				"A=PortalSmoke%23Smoke&B=AWS%20%26%20CI&C=a%3Db&D=a%2Bb&",
				"E=a+b"));

		testEquals(5, parameters.size());
		testEquals("PortalSmoke#Smoke", parameters.get("A"));
		testEquals("AWS & CI", parameters.get("B"));
		testEquals("a=b", parameters.get("C"));
		testEquals("a+b", parameters.get("D"));
		testEquals("a b", parameters.get("E"));
	}

	private Map<String, String> _newParameters(String... namesAndValues) {
		Map<String, String> parameters = new LinkedHashMap<>();

		for (int i = 0; i < namesAndValues.length; i = i + 2) {
			parameters.put(namesAndValues[i], namesAndValues[i + 1]);
		}

		return parameters;
	}

}