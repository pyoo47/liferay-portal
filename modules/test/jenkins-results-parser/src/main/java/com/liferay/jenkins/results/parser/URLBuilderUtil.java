/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
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
 * Builds and decomposes URLs so that a value is encoded exactly once, at the
 * point the URL is assembled, and is held decoded everywhere else.
 *
 * <p>
 * This is the only class in the module that references
 * <code>org.apache.http</code>. Confining the dependency here keeps a
 * <code>URIBuilder</code> from being held as a field or a static, which
 * matters because downstream work runs concurrently through
 * <code>ParallelExecutor</code> and <code>URIBuilder</code> is mutable and not
 * thread safe. Every method below constructs its own instance and discards it.
 * </p>
 *
 * @author Calum Ragan
 */
public class URLBuilderUtil {

	/**
	 * Returns the parameters encoded as an
	 * <code>application/x-www-form-urlencoded</code> request body.
	 *
	 * <p>
	 * This is for a POST body, not a URL. A body carries no leading question
	 * mark.
	 * </p>
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
	 * Returns the base URL with the parameters appended as a query string,
	 * each name and value percent encoded exactly once.
	 *
	 * <p>
	 * The base URL's path is passed through untouched, so a job name such as
	 * <code>test-portal-acceptance-pullrequest(master)</code> is preserved
	 * verbatim. A base URL that is not a legal URI reference is repaired with
	 * {@link #normalizeURL(String)} first, which percent encodes an octet that
	 * is illegal where it sits, so a raw space in a path becomes
	 * <code>%20</code>.
	 * </p>
	 *
	 * <p>
	 * The base URL may already carry a query string and a fragment. Parameters
	 * are inserted before the fragment, and a pre-existing query string is
	 * re-emitted in canonical form, so a space already spelled
	 * <code>%20</code> comes back as <code>+</code>. The two are equivalent
	 * under form decoding but are not byte identical.
	 * </p>
	 *
	 * <p>
	 * Parameters are emitted in ascending name order so that the same map
	 * always produces the same URL. A null value emits a valueless parameter,
	 * an empty value emits a name followed by an equals sign, and both round
	 * trip through {@link #parseQueryString(String)}. A null or empty map
	 * returns the base URL unchanged, without a trailing question mark.
	 * </p>
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
	 * Returns the base URL with a single query parameter appended.
	 *
	 * @see #buildURL(String, Map)
	 */
	public static String buildURL(String baseURL, String name, String value) {
		URIBuilder uriBuilder = _newURIBuilder(baseURL);

		uriBuilder.addParameter(name, value);

		URI uri = _build(uriBuilder, baseURL);

		return uri.toString();
	}

	/**
	 * Returns a URL that <code>java.net.URL</code> and
	 * <code>HttpURLConnection</code> can use verbatim.
	 *
	 * <p>
	 * A URL that already parses as a legal URI reference is returned
	 * unchanged. Nothing is re-encoded, no escape is reversed, and the
	 * fragment is preserved, so every URL built by this class passes through
	 * untouched.
	 * </p>
	 *
	 * <p>
	 * Otherwise the URL arrived already assembled from an external source and
	 * carries an octet that is illegal where it sits, most often a raw space,
	 * a raw square bracket, or a bare percent sign. Each illegal octet is
	 * percent encoded in place. A valid escape is left alone, so the method is
	 * idempotent.
	 * </p>
	 *
	 * <p>
	 * A URL that cannot be repaired is returned unchanged after a warning.
	 * This method never throws.
	 * </p>
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
	 * Returns the query string decomposed into decoded name and value pairs.
	 *
	 * <p>
	 * A parameter that carries an invalid escape is kept as raw text rather
	 * than failing, matching how a query string that was never encoded is
	 * still readable.
	 * </p>
	 *
	 * <p>
	 * When a name repeats, the first value wins and the duplicate is reported.
	 * A valueless parameter yields a null value.
	 * </p>
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

				// A percent sign is in neither legal character set, so a bare
				// one that starts no escape sequence lands here and is encoded
				// as %25

				int codePoint = value.codePointAt(i);

				_escapeCodePoint(sb, codePoint);

				i = i + Character.charCount(codePoint);
			}
		}

		return sb.toString();
	}

	private static void _escapeCodePoint(StringBuilder sb, int codePoint) {
		String value = new String(Character.toChars(codePoint));

		byte[] bytes = value.getBytes(StandardCharsets.UTF_8);

		for (byte b : bytes) {
			int octet = b & 0xff;

			sb.append("%");
			sb.append(_HEXADECIMAL_CHARACTERS.charAt(octet >> 4));
			sb.append(_HEXADECIMAL_CHARACTERS.charAt(octet & 0x0f));
		}
	}

	/**
	 * Returns <code>true</code> when the URL carries a raw square bracket in
	 * its query string or fragment.
	 *
	 * <p>
	 * RFC 3986 does not permit a square bracket outside the authority, but
	 * <code>URI</code> accepts one in a query string and in a fragment.
	 * Escaping it is therefore the conformant reading, and it is also what
	 * this repository has always put on the wire for a Jenkins
	 * <code>tree</code> expression, so a converted call site is not observable
	 * by a server. This is the one place where a URL that <code>URI</code>
	 * accepts is still treated as needing repair.
	 * </p>
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
	 * Returns a builder over the URL, repairing the URL first when it does not
	 * parse.
	 *
	 * <p>
	 * A URL assembled outside this class may carry a raw space or square
	 * bracket, most often from a test name or a cloud storage object key.
	 * Repairing it keeps appending a parameter to such a URL from throwing
	 * where plain concatenation would have quietly succeeded.
	 * </p>
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