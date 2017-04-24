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

import org.dom4j.Element;

/**
 * @author Peter Yoo
 */
public class LocalGitMirrorFailureMessageGenerator
	extends BaseFailureMessageGenerator {

	@Override
	public Element getMessageElement(Build build) {
		return getMessageElement(build.getConsoleText());
	}

	@Override
	public Element getMessageElement(String consoleText) {
		if (!consoleText.contains(_LOCAL_GIT_FAILURE_END_STRING) ||
			!consoleText.contains(_LOCAL_GIT_FAILURE_START_STRING)) {

			return null;
		}

		Element messageElement = Dom4JUtil.getNewElement("div");

		Dom4JUtil.getNewElement(
			"p", messageElement, "Unable to synchronize with ",
			Dom4JUtil.getNewElement("strong", null, "local Git mirror"), ".");

		int end = consoleText.indexOf(_LOCAL_GIT_FAILURE_END_STRING);

		int start = consoleText.lastIndexOf(
			_LOCAL_GIT_FAILURE_START_STRING, end);

		consoleText = consoleText.substring(start, end);

		int minIndex = consoleText.length();

		for (String string : new String[] {"error: ", "fatal: "}) {
			int index = consoleText.indexOf(string);

			if (index != -1) {
				if (index < minIndex) {
					minIndex = index;
				}
			}
		}

		int gitCommandIndex = consoleText.lastIndexOf("+ git", minIndex);

		if (gitCommandIndex != -1) {
			start = gitCommandIndex;
		}

		start = consoleText.lastIndexOf("\n", start);

		end = consoleText.lastIndexOf("\n");

		messageElement.add(
			getConsoleOutputSnippetElement(consoleText, false, start, end));

		return messageElement;
	}

	private static final String _LOCAL_GIT_FAILURE_END_STRING = "BUILD FAILED";

	private static final String _LOCAL_GIT_FAILURE_START_STRING =
		"Too many retries while synchronizing GitHub pull request.";

}