/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser;

import java.net.URI;
import java.net.URISyntaxException;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

/**
 * Builds and decomposes URLs so that a value is encoded exactly once, when the
 * URL is assembled, and is held decoded everywhere else. This is the only
 * class here that references <code>org.apache.http</code>, which keeps a
 * mutable <code>URIBuilder</code> from being shared across the threads
 * <code>ParallelExecutor</code> runs.
 *
 * @author Calum Ragan
 */
public class URLBuilderUtil {

	/**
	 * Returns an <code>application/x-www-form-urlencoded</code> request body,
	 * which unlike a URL carries no leading question mark.
	 */
	public static String buildFormContent(Map<String, String> parameters) {
		if ((parameters == null) || parameters.isEmpty()) {
			return "";
		}

		Map<String, String> sortedParameters = new TreeMap<>(parameters);

		List<NameValuePair> nameValuePairs = new ArrayList<>(
			sortedParameters.size());

		for (Map.Entry<String, String> entry : sortedParameters.entrySet()) {
			nameValuePairs.add(
				new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}

		return URLEncodedUtils.format(nameValuePairs, StandardCharsets.UTF_8);
	}

	/**
	 * Returns the base URL with the parameters appended, each name and value
	 * percent encoded exactly once and emitted in ascending name order so that
	 * the same map always produces the same URL. The path is passed through
	 * untouched. A base URL that is not a legal URI reference is repaired with
	 * {@link #normalizeURL(String)} first, and one that already carries a
	 * query string has it re-emitted in canonical form. A null or empty map
	 * returns the base URL unchanged, with no trailing question mark.
	 */
	public static String buildURL(
		String baseURL, Map<String, String> parameters) {

		if ((parameters == null) || parameters.isEmpty()) {
			return baseURL;
		}

		URIBuilder uriBuilder = _newURIBuilder(baseURL);

		Map<String, String> sortedParameters = new TreeMap<>(parameters);

		for (Map.Entry<String, String> entry : sortedParameters.entrySet()) {
			uriBuilder.addParameter(entry.getKey(), entry.getValue());
		}

		URI uri = _build(uriBuilder, baseURL);

		return uri.toString();
	}

	/**
	 * @see #buildURL(String, Map)
	 */
	public static String buildURL(String baseURL, String name, String value) {
		URIBuilder uriBuilder = _newURIBuilder(baseURL);

		uriBuilder.addParameter(name, value);

		URI uri = _build(uriBuilder, baseURL);

		return uri.toString();
	}

	/**
	 * Returns a URL that <code>HttpURLConnection</code> can use verbatim. One
	 * that already parses as a legal URI reference is returned unchanged, so
	 * nothing this class builds is altered. Otherwise each octet that is
	 * illegal where it sits is percent encoded in place, leaving a valid
	 * escape alone, which makes this idempotent. A URL that cannot be repaired
	 * is returned unchanged after a warning. This never throws.
	 */
	public static String normalizeURL(String url) {
		if (JenkinsResultsParserUtil.isNullOrEmpty(url) ||
			(_isLegalURI(url) && !_hasRawSquareBracket(url))) {

			return url;
		}

		String remainder = url;

		String fragment = null;

		int index = remainder.indexOf('#');

		if (index != -1) {
			fragment = remainder.substring(index + 1);

			remainder = remainder.substring(0, index);
		}

		String queryString = null;

		index = remainder.indexOf('?');

		if (index != -1) {
			queryString = remainder.substring(index + 1);

			remainder = remainder.substring(0, index);
		}

		StringBuilder sb = new StringBuilder();

		sb.append(_escape(remainder, _LEGAL_PATH_CHARACTERS));

		if (queryString != null) {
			sb.append("?");
			sb.append(_escape(queryString, _LEGAL_QUERY_CHARACTERS));
		}

		if (fragment != null) {
			sb.append("#");
			sb.append(_escape(fragment, _LEGAL_QUERY_CHARACTERS));
		}

		String normalizedURL = sb.toString();

		if (!_isLegalURI(normalizedURL)) {
			System.out.println("WARNING: Unable to normalize the URL " + url);

			return url;
		}

		return normalizedURL;
	}

	/**
	 * Returns the query string decomposed into decoded name and value pairs. A
	 * parameter carrying an invalid escape is kept as raw text rather than
	 * failing. When a name repeats the first value wins and the duplicate is
	 * reported, and a valueless parameter yields a null value.
	 */
	public static Map<String, String> parseQueryString(String queryString) {
		if (JenkinsResultsParserUtil.isNullOrEmpty(queryString)) {
			return Collections.emptyMap();
		}

		return _toMap(
			URLEncodedUtils.parse(queryString, StandardCharsets.UTF_8));
	}

	private static URI _build(URIBuilder uriBuilder, String url) {
		try {
			return uriBuilder.build();
		}
		catch (URISyntaxException uriSyntaxException) {
			throw new RuntimeException(
				"Unable to build the URL " + url, uriSyntaxException);
		}
	}

	private static String _escape(String value, String legalCharacters) {
		StringBuilder sb = new StringBuilder(value.length());

		int i = 0;

		while (i < value.length()) {
			char c = value.charAt(i);

			if ((c == '%') && _isEscapeSequence(value, i)) {
				sb.append(value, i, i + 3);

				i = i + 3;
			}
			else if (_isLegalCharacter(c, legalCharacters)) {
				sb.append(c);

				i++;
			}
			else {
				int codePoint = value.codePointAt(i);

				_escapeCodePoint(sb, codePoint);

				i = i + Character.charCount(codePoint);
			}
		}

		return sb.toString();
	}

	private static void _escapeCodePoint(StringBuilder sb, int codePoint) {
		String codePointString = new String(Character.toChars(codePoint));

		byte[] bytes = codePointString.getBytes(StandardCharsets.UTF_8);

		for (byte b : bytes) {
			int octet = b & 0xff;

			sb.append("%");
			sb.append(_HEXADECIMAL_CHARACTERS.charAt(octet >> 4));
			sb.append(_HEXADECIMAL_CHARACTERS.charAt(octet & 0x0f));
		}
	}

	/**
	 * Returns <code>true</code> when the URL carries a raw square bracket in
	 * its query string or fragment. RFC 3986 does not permit one outside the
	 * authority but <code>URI</code> accepts it there, so this is the one case
	 * where a URL <code>URI</code> accepts still needs repair.
	 */
	private static boolean _hasRawSquareBracket(String url) {
		int index = url.indexOf('?');

		if (index == -1) {
			return false;
		}

		String remainder = url.substring(index + 1);

		if ((remainder.indexOf('[') != -1) || (remainder.indexOf(']') != -1)) {
			return true;
		}

		return false;
	}

	private static boolean _isEscapeSequence(String value, int index) {
		if ((index + 2) >= value.length()) {
			return false;
		}

		for (int i = index + 1; i <= (index + 2); i++) {
			char c = value.charAt(i);

			if (_HEXADECIMAL_CHARACTERS.indexOf(c) == -1) {
				return false;
			}
		}

		return true;
	}

	private static boolean _isLegalCharacter(char c, String legalCharacters) {
		if (((c >= 'A') && (c <= 'Z')) || ((c >= 'a') && (c <= 'z')) ||
			((c >= '0') && (c <= '9'))) {

			return true;
		}

		if (legalCharacters.indexOf(c) != -1) {
			return true;
		}

		return false;
	}

	private static boolean _isLegalURI(String url) {
		try {
			new URI(url);

			return true;
		}
		catch (URISyntaxException uriSyntaxException) {
			return false;
		}
	}

	/**
	 * Returns a builder over the URL, repairing it first when it does not
	 * parse, so that a URL assembled elsewhere with a raw space does not throw
	 * where plain concatenation used to succeed.
	 */
	private static URIBuilder _newURIBuilder(String url) {
		try {
			return new URIBuilder(url);
		}
		catch (URISyntaxException uriSyntaxException1) {
			String normalizedURL = normalizeURL(url);

			try {
				return new URIBuilder(normalizedURL);
			}
			catch (URISyntaxException uriSyntaxException2) {
				throw new RuntimeException(
					"Unable to parse the URL " + url, uriSyntaxException2);
			}
		}
	}

	private static Map<String, String> _toMap(
		List<NameValuePair> nameValuePairs) {

		Map<String, String> parameters = new LinkedHashMap<>(
			nameValuePairs.size());

		for (NameValuePair nameValuePair : nameValuePairs) {
			String name = nameValuePair.getName();

			if (parameters.containsKey(name)) {
				System.out.println(
					"WARNING: Ignoring the repeated query string parameter " +
						name);

				continue;
			}

			parameters.put(name, nameValuePair.getValue());
		}

		return parameters;
	}

	private static final String _HEXADECIMAL_CHARACTERS =
		"0123456789ABCDEFabcdef";

	private static final String _LEGAL_PATH_CHARACTERS = "-._~!$&'()*+,;=:@/";

	private static final String _LEGAL_QUERY_CHARACTERS =
		_LEGAL_PATH_CHARACTERS + "?";

}