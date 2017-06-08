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

package com.liferay.jenkins.results.parser.failure.message.generator;

import com.liferay.jenkins.results.parser.Build;
import com.liferay.jenkins.results.parser.Dom4JUtil;

import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Element;

/**
 * @author Kevin Yen
 */
public class PoshiValidationFailureMessageGenerator
	extends BaseFailureMessageGenerator {

	@Override
	public String getMessage(
		String buildURL, String consoleOutput, Hashtable<?, ?> properties) {

		Matcher poshiFailureMatcher = _poshiFailurePattern.matcher(
			consoleOutput);

		if (!poshiFailureMatcher.find()) {
			return null;
		}

		String failedPoshiTaskToken = poshiFailureMatcher.group(1);

		int end = consoleOutput.indexOf(failedPoshiTaskToken);

		end = consoleOutput.indexOf(_TOKEN_TRY, end);

		end = consoleOutput.lastIndexOf("\n", end);

		int start = consoleOutput.lastIndexOf(_TOKEN_JAVA_LANG_EXCEPTION, end);

		start = consoleOutput.lastIndexOf("\n", start);

		StringBuilder sb = new StringBuilder();

		sb.append("<p>POSHI Validation Failure: </p><strong>");

		sb.append(failedPoshiTaskToken);

		sb.append("</strong><pre><code>");

		sb.append(getConsoleOutputSnippet(consoleOutput, true, start, end));

		sb.append("</code></pre>");

		return sb.toString();
	}

	@Override
	public Element getMessageElement(Build build) {
		String consoleText = build.getConsoleText();

		Matcher poshiFailureMatcher = _poshiFailurePattern.matcher(consoleText);

		if (!poshiFailureMatcher.find()) {
			return null;
		}

		String failedPoshiTaskToken = poshiFailureMatcher.group(1);

		int end = consoleText.indexOf(failedPoshiTaskToken);

		end = consoleText.indexOf(_TOKEN_TRY, end);

		end = consoleText.lastIndexOf("\n", end);

		int start = consoleText.lastIndexOf(_TOKEN_JAVA_LANG_EXCEPTION, end);

		start = consoleText.lastIndexOf("\n", start);

		return Dom4JUtil.getNewElement(
				"div", null,
				Dom4JUtil.getNewElement(
						"p", null, "POSHI validation error: ",
						Dom4JUtil.getNewElement(
								"strong", null, failedPoshiTaskToken)),
				getConsoleOutputSnippetElement(consoleText, true, start, end));
	}

	private static final String _TOKEN_JAVA_LANG_EXCEPTION =
			"java.lang.Exception";

	private static final String _TOKEN_TRY = "Try:";

	private static final Pattern _poshiFailurePattern = Pattern.compile(
		"(?:\\n.*)(Execution failed for task.*Poshi.*)\\n");

}